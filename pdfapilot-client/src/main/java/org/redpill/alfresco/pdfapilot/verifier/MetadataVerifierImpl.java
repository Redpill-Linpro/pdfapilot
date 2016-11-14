package org.redpill.alfresco.pdfapilot.verifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.MD5;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.redpill.alfresco.module.metadatawriter.InvalidFormatException;
import org.redpill.alfresco.module.metadatawriter.factories.MetadataContentFactory;
import org.redpill.alfresco.module.metadatawriter.factories.UnsupportedMimetypeException;
import org.redpill.alfresco.module.metadatawriter.services.ContentFacade;
import org.redpill.alfresco.module.metadatawriter.services.ContentFacade.ContentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("ppc.metadataVerifier")
public class MetadataVerifierImpl implements MetadataVerifier {

  private static final Logger LOG = Logger.getLogger(MetadataVerifierImpl.class);

  @Autowired
  private MetadataContentFactory _metadataContentFactory;

  @Autowired
  private MetadataExtracterRegistry _metadataExtracterRegistry;

  @Autowired
  @Qualifier("global-properties")
  private Properties globalProperties;

  @Value("${pdfapilot.metadatawriter.enabled}")
  protected boolean enabled = true;

  protected static final String SIZE_LIMIT_PROPERTY_PREFIX = "content.extracter.pdfaPilot.extensions.";
  protected static final String SIZE_LIMIT_PROPERTY_SUFFIX = ".maxSourceSizeKBytes";
  protected static final String SIZE_LIMIT_PROPERTY_DEFAULT = SIZE_LIMIT_PROPERTY_PREFIX + "default" + SIZE_LIMIT_PROPERTY_SUFFIX;

  /**
   * Check against the properties if metadata operations are allowed or not
   * 
   * @param file
   * @param node
   * @return
   */
  protected boolean allowMetadataOperations(File file, NodeRef node) {

    if (!enabled) {
      LOG.debug("Metadata writing disabled by configuration (pdfapilot.metadatawriter.enabled). Aborting");
      return false;
    }
    
    //Check if node ref is supplied
    if (node==null) {
      return false;
    }

    String extension = FilenameUtils.getExtension(file.getName());
    String sizeLimitProperty = SIZE_LIMIT_PROPERTY_PREFIX + extension + SIZE_LIMIT_PROPERTY_SUFFIX;
    String sizeLimit = globalProperties.getProperty(sizeLimitProperty);
    if (sizeLimit == null || sizeLimit.length() == 0) {
      sizeLimit = globalProperties.getProperty(SIZE_LIMIT_PROPERTY_DEFAULT);
    }

    if (sizeLimit == null || sizeLimit.length() == 0) {
      LOG.trace("No size limit found. Allowing. File name: " + file.getName());
    } else {
      long sizeLimitLong = Long.valueOf(sizeLimit) * 1024;

      if (file.length() > sizeLimitLong) {
        LOG.warn("File length is above the limit for node " + node + ". File name: " + file.getName() + " Denying.");
        return false;
      } else if (LOG.isDebugEnabled()) {
        LOG.debug("Allowing, size is below limit for node " + node + ". File name: " + file.getName());
      }
    }
    return true;
  }

  @Override
  public String changeMetadataTitle(File file, NodeRef node, String basename, String mimetype) {
    String title = null;
    if (allowMetadataOperations(file, node)) {
      try {
        title = extractMetadataTitle(file, mimetype);

        String hash = MD5.Digest(node.toString().getBytes());

        writeMetadataTitle(file, basename, mimetype, hash);
      } catch (UnsupportedMimetypeException ex) {
        if (LOG.isTraceEnabled()) {
          LOG.trace(ex.getMessage(), ex);
        }

        LOG.error(ex.getMessage());

        title = null;
      } catch (InvalidFormatException ex) {
        if (LOG.isTraceEnabled()) {
          LOG.trace(ex.getMessage(), ex);
        }

        LOG.error(ex.getMessage());

        title = null;
      }
    }
    return title;
  }

  @Override
  public void writeMetadataTitle(File file, String basename, String mimetype, String title) throws UnsupportedMimetypeException {
    InputStream inputStream = null;
    OutputStream outputStream = null;

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

  @Override
  public String extractMetadataTitle(File file, String mimetype) {
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

  @Override
  public boolean verifyMetadata(NodeRef node, File file, String basename, String title) throws UnsupportedMimetypeException {
    boolean result = true;

    if (allowMetadataOperations(file, node)) {
      String extractedHash = extractMetadataTitle(file, "application/pdf");

      String hash = MD5.Digest(node.toString().getBytes());

      if (!hash.equals(extractedHash)) {
        LOG.warn("The converted file for nodeRef '" + node + "' is not the same as the source file!");

        result = false;
      } else {
        result = true;
      }

      // write back the original title
      writeMetadataTitle(file, basename, "application/pdf", title);
    }

    return result;
  }

}
