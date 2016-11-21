package org.redpill.alfresco.pdfapilot.worker;

import java.io.File;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.ContentTransformerHelper;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.redpill.alfresco.pdfapilot.client.PdfaPilotClient;
import org.redpill.alfresco.pdfapilot.client.PdfaPilotClientImpl.CreateResult;
import org.redpill.alfresco.pdfapilot.verifier.MetadataVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("ppc.pdfaPilotWorker")
public class PdfaPilotWorker extends ContentTransformerHelper implements ContentTransformerWorker {

  private static final Logger LOG = Logger.getLogger(PdfaPilotWorker.class);

  @Autowired
  @Qualifier("MimetypeService")
  private MimetypeService _mimetypeService;

  @Autowired
  private PdfaPilotClient _pdfaPilotClient;

  @Autowired
  @Qualifier("ppc.documentFormatRegistry")
  private DocumentFormatRegistry _documentFormatRegistry;

  @Autowired
  @Qualifier("NodeService")
  private NodeService _nodeService;

  @Autowired
  @Qualifier("policyBehaviourFilter")
  private BehaviourFilter _behaviourFilter;

  private boolean _available;

  @Autowired
  @Value("${pdfapilot.client.enabled}")
  private boolean enabled;

  private boolean _licensed;

  private ExecutorService _executorService;

  @Autowired
  private MetadataVerifier _metadataVerifier;

  @Override
  public String getVersionString() {
    JSONObject version = _pdfaPilotClient.getVersion();

    try {
      return _pdfaPilotClient.getVersion().getString("version");
    } catch (JSONException ex) {
      return version.toString();
    }
  }

  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    if (!isAvailable()) {
      return false;
    }

    if (!isLicensed()) {
      return false;
    }

    String sourceExtension = _mimetypeService.getExtension(sourceMimetype);
    String targetExtension = _mimetypeService.getExtension(targetMimetype);

    // query the registry for the source format
    DocumentFormat sourceFormat = _documentFormatRegistry.getFormatByExtension(sourceExtension);

    if (sourceFormat == null) {
      // no document format
      return false;
    }

    // query the registry for the target format
    DocumentFormat targetFormat = _documentFormatRegistry.getFormatByExtension(targetExtension);

    if (targetFormat == null) {
      // no document format
      return false;
    }

    Map<String, ?> properties = targetFormat.getStoreProperties(sourceFormat.getInputFamily());

    return properties != null ? properties.size() > 0 : false;
  }

  @Override
  public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception {
    if (!isAvailable()) {
      throw new ContentIOException("Content conversion failed (unavailable): \n" + "   reader: " + reader + "\n" + "   writer: " + writer);
    }

    if (!isLicensed()) {
      throw new ContentIOException("Content conversion failed (not licensed): \n" + "   reader: " + reader + "\n" + "   writer: " + writer);
    }

    // get mime types
    String sourceMimetype = getMimetype(reader);
    String targetMimetype = getMimetype(writer);

    // get the extensions to use
    String sourceExtension = _mimetypeService.getExtension(sourceMimetype);
    String targetExtension = _mimetypeService.getExtension(targetMimetype);

    if (StringUtils.isBlank(sourceExtension) || StringUtils.isBlank(targetExtension)) {
      throw new AlfrescoRuntimeException("Unknown extensions for mimetypes: \n" + "   source mimetype: " + sourceMimetype + "\n" + "   source extension: " + sourceExtension + "\n"
              + "   target mimetype: " + targetMimetype + "\n" + "   target extension: " + targetExtension);
    }

    NodeRef sourceNodeRef = options.getSourceNodeRef();
    String basename = getBasename(sourceNodeRef);
    String sourceFilename = basename + "." + sourceExtension;

    File sourceFile = TempFileProvider.createTempFile(reader.getContentInputStream(), basename, "." + sourceExtension);
    File targetFile = null;

    try {
      // write a unique hash of the nodeRef to the document to be converted
      String title = _metadataVerifier.changeMetadataTitle(sourceFile, sourceNodeRef, basename, sourceMimetype);

      // transformDoc the source temp file to the target temp file
      CreateResult result = transformInternal(sourceFilename, sourceFile, options);
      targetFile = result.file;

      if (title != null) {
        boolean verified = _metadataVerifier.verifyMetadata(sourceNodeRef, targetFile, basename, title);

        _pdfaPilotClient.auditCreationResult(result.id, verified);
      }

      writer.putContent(targetFile);
    } finally {
      sourceFile.delete();

      if (targetFile != null) {
        targetFile.delete();
      }
    }
  }

  protected CreateResult transformInternal(String filename, File sourceFile, TransformationOptions options) throws Exception {
    PdfaPilotTransformationOptions pdfaPilotOptions = null;

    // set properties
    if (options instanceof PdfaPilotTransformationOptions) {
      pdfaPilotOptions = (PdfaPilotTransformationOptions) options;
    }

    try {
      return _pdfaPilotClient.create(filename, sourceFile, pdfaPilotOptions);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private String getBasename(NodeRef node) {
    if (node == null || !_nodeService.exists(node)) {
      return "PPCTW_" + GUID.generate();
    }

    try {
      String filename = (String) _nodeService.getProperty(node, ContentModel.PROP_NAME);

      String basename = FilenameUtils.getBaseName(filename);

      // TODO: Investigate if this is really needed
      // callas currently has a bug that makes it crash if the whole filepath is
      // longer than 260 characters
      basename = StringUtils.substring(basename, 0, 100);

      // 0x2013 is the long hyphen, not allowed here...
      char c = 0x2013;
      if (StringUtils.contains(basename, c)) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Long hyphen replaced with short one");
        }

        basename = StringUtils.replaceChars(basename, c, '-');
      }

      filename = basename;

      if (LOG.isTraceEnabled()) {
        LOG.trace("Filename before normalization");

        for (char character : filename.toCharArray()) {
          LOG.trace(character + " : " + (int) character);
        }
      }

      filename = Normalizer.normalize(filename, Form.NFKC);

      if (LOG.isTraceEnabled()) {
        LOG.trace("Filename after normalization");

        for (char character : filename.toCharArray()) {
          LOG.trace(character + " : " + (int) character);
        }
      }

      // pad the string with _ until it's at lest 3 characters long
      if (basename.length() < 3) {
        basename = StringUtils.rightPad(basename, 3, "_");
      }

      return basename;
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public boolean isAvailable() {
    return _available && enabled;
  }

  public void setAvailable(boolean available) {
    _available = available;
  }

  public boolean isLicensed() {
    return _licensed;
  }

  public void setLicensed(boolean licensed) {
    _licensed = licensed;
  }

  @PostConstruct
  public void postConstruct() {
    _executorService = Executors.newCachedThreadPool();
  }

  @PreDestroy
  public void destroy() {
    _executorService.shutdown();
  }

}
