package org.redpill.alfresco.pdfapilot.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.redpill.alfresco.module.metadatawriter.factories.UnsupportedMimetypeException;
import org.redpill.alfresco.pdfapilot.verifier.MetadataVerifier;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MetadataVerifierIntegrationTest extends AbstractRepoIntegrationTest {

  @Autowired
  @Qualifier("ppc.metadataVerifier")
  private MetadataVerifier _metadataVerifier;

  @Test
  public void testDocx() throws IOException {
    testExtractMetadataTitle("test.docx", MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING);
  }

  @Test
  public void testDocx2() throws IOException {
    String title = testExtractMetadataTitle("test2.docx", MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING);

    assertTrue(StringUtils.isBlank(title));
  }

  @Test
  public void testDoc() throws IOException {
    testExtractMetadataTitle("test.doc", MimetypeMap.MIMETYPE_WORD);
  }

  @Test
  public void testDoc2() throws IOException {
    testExtractMetadataTitle("test2.doc", MimetypeMap.MIMETYPE_WORD);
  }

  @Test
  public void testPptx() throws IOException {
    testExtractMetadataTitle("test.pptx", MimetypeMap.MIMETYPE_OPENXML_PRESENTATION);
  }

  @Test
  public void testPptx2() throws IOException {
    testExtractMetadataTitle("test2.pptx", MimetypeMap.MIMETYPE_OPENXML_PRESENTATION);
  }

  @Test
  public void testChangeAndVerifyPptx() throws IOException, UnsupportedMimetypeException {
    String title = testChangeMetadata("test.pptx", MimetypeMap.MIMETYPE_OPENXML_PRESENTATION);

    assertEquals("Lorem Ipsum", title);
  }

  @Test
  public void testChangeAndVerifyPptx2() throws IOException, UnsupportedMimetypeException {
    String title = testChangeMetadata("test2.pptx", MimetypeMap.MIMETYPE_OPENXML_PRESENTATION);

    assertNull(title);
  }

  public String testExtractMetadataTitle(String filename, String mimetype) throws IOException {
    long start = System.currentTimeMillis();

    File file = File.createTempFile("test_", ".bin");

    try {
      InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
      OutputStream outputStream = new FileOutputStream(file);

      IOUtils.copy(inputStream, outputStream);

      String title = _metadataVerifier.extractMetadataTitle(file, mimetype);

      long total = System.currentTimeMillis() - start;

      System.out.println("Extracted metadata title for " + filename + ", time: " + (total / 1000) + " sec.");

      return title;
    } finally {
      file.delete();
    }
  }

  public String testChangeMetadata(String filename, String mimetype) throws IOException, UnsupportedMimetypeException {
    File file = File.createTempFile("test_", ".bin");

    try {
      InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
      OutputStream outputStream = new FileOutputStream(file);

      IOUtils.copy(inputStream, outputStream);

      String basename = FilenameUtils.getBaseName(filename);

      NodeRef node = new NodeRef("workspace://SpacesStore/" + GUID.generate());

      return _metadataVerifier.changeMetadataTitle(file, node, basename, mimetype);
    } finally {
      file.delete();
    }
  }

}
