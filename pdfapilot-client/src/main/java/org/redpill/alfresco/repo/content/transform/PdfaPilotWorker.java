package org.redpill.alfresco.repo.content.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.content.transform.ContentTransformerHelper;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.MD5;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.redpill.alfresco.module.metadatawriter.factories.MetadataContentFactory;
import org.redpill.alfresco.module.metadatawriter.factories.UnsupportedMimetypeException;
import org.redpill.alfresco.module.metadatawriter.services.ContentFacade;
import org.redpill.alfresco.module.metadatawriter.services.ContentFacade.ContentException;
import org.springframework.beans.factory.annotation.Autowired;

public class PdfaPilotWorker extends ContentTransformerHelper implements ContentTransformerWorker {

  private static final Logger LOG = Logger.getLogger(PdfaPilotWorker.class);

  @Resource(name = "MimetypeService")
  private MimetypeService _mimetypeService;

  @Autowired
  private PdfaPilotClient _pdfaPilotClient;

  private DocumentFormatRegistry _documentFormatRegistry;

  @Resource(name = "NodeService")
  private NodeService _nodeService;

  @Autowired
  private MetadataContentFactory _metadataContentFactory;

  @Autowired
  private MetadataExtracterRegistry _metadataExtracterRegistry;

  private boolean _available;

  @Override
  public boolean isAvailable() {
    return _available;
  }

  @Override
  public String getVersionString() {
    return _pdfaPilotClient.getVersion();
  }

  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    if (!isAvailable()) {
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
      String title = changeMetadataTitle(sourceFile, sourceNodeRef, sourceMimetype);

      // transformDoc the source temp file to the target temp file
      targetFile = transformInternal(sourceFilename, sourceFile, options);

      if (title != null) {
        verifyMetadata(sourceNodeRef, targetFile, title);
      }

      writer.putContent(targetFile);
    } finally {
      sourceFile.delete();

      if (targetFile != null) {
        targetFile.delete();
      }
    }
  }

  protected File transformInternal(String filename, File sourceFile, TransformationOptions options) throws Exception {
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

      return filename;
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Extracts the title from the document and returns it if found, otherwise
   * returns null. Writes a hash of the nodeRef as a new title.
   * 
   * @param file
   * @param node
   * @param mimetype
   * @return the title or null if no title found or the hash couldn't be
   *         written.
   */
  private String changeMetadataTitle(File file, NodeRef node, String mimetype) {
    String title = null;

    try {
      title = extractMetadataTitle(file, mimetype);

      String hash = MD5.Digest(node.toString().getBytes());

      writeMetadataTitle(file, node, mimetype, hash);
    } catch (UnsupportedMimetypeException ex) {
      if (LOG.isTraceEnabled()) {
        LOG.trace(ex.getMessage(), ex);
      }

      LOG.error(ex.getMessage());

      title = null;
    }

    return title;
  }

  /**
   * Writes a metadata title to a document that supports it. Throws an exception
   * if title can't be written.
   * 
   * @param file
   * @param node
   * @param mimetype
   * @param title
   * @throws UnsupportedMimetypeException
   */
  private void writeMetadataTitle(File file, NodeRef node, String mimetype, String title) throws UnsupportedMimetypeException {
    InputStream inputStream = null;
    OutputStream outputStream = null;

    String basename = getBasename(node);
    String extension = FilenameUtils.getExtension(file.getName());
    File tempFile = TempFileProvider.createTempFile(basename, "." + extension);

    try {
      inputStream = new FileInputStream(file);
      outputStream = new FileOutputStream(tempFile);

      ContentFacade contentFacade = _metadataContentFactory.createContent(inputStream, outputStream, mimetype);

      // write the nodeRef to the Title field and include the old title
      contentFacade.writeMetadata("Title", title);

      contentFacade.save();

      FileUtils.copyFile(tempFile, file);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    } catch (ContentException ex) {
      throw new RuntimeException(ex);
    } finally {
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(outputStream);
      tempFile.delete();
    }
  }

  /**
   * Extracts the metadata title from the file. If no title found it returns an
   * empty string (not null).
   * 
   * @param file
   * @param mimetype
   * @return the title or an empty string
   */
  private String extractMetadataTitle(File file, String mimetype) {
    MetadataExtracter extracter = _metadataExtracterRegistry.getExtracter(mimetype);

    Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

    FileContentReader contentReader = new FileContentReader(file);
    contentReader.setMimetype(mimetype);

    properties = extracter.extract(contentReader, properties);

    String title = DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_TITLE));

    if (LOG.isDebugEnabled()) {
      LOG.debug("Extracted title '" + title + "' from file '" + file.getAbsolutePath() + "'");
    }

    return StringUtils.isNotBlank(title) ? title : "";
  }

  private void verifyMetadata(NodeRef node, File file, String title) throws UnsupportedMimetypeException {
    String extractedHash = extractMetadataTitle(file, "application/pdf");

    String hash = MD5.Digest(node.toString().getBytes());

    if (!hash.equals(extractedHash)) {
      throw new RuntimeException("The converted file for nodeRef '" + node + "' is not the same as the source file!");
    }

    // write back the original title
    writeMetadataTitle(file, node, "application/pdf", title);
  }

  public void setAvailable(boolean available) {
    _available = available;
  }

}
