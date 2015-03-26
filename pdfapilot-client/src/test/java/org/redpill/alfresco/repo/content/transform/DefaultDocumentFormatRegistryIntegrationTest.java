package org.redpill.alfresco.repo.content.transform;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.alfresco.repo.content.MimetypeMap;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultDocumentFormatRegistryIntegrationTest extends AbstractRepoIntegrationTest {

  @Autowired
  private DocumentFormatRegistry _documentFormatRegistry;

  @Test
  public void testGetFormatByExtension() {
    DocumentFormat pdfDocumentFormat = _documentFormatRegistry.getFormatByExtension("pdf");
    DocumentFormat docDocumentFormat = _documentFormatRegistry.getFormatByExtension("doc");
    DocumentFormat xlsDocumentFormat = _documentFormatRegistry.getFormatByExtension("xls");
    DocumentFormat xlsmDocumentFormat = _documentFormatRegistry.getFormatByExtension("xlsm");
    DocumentFormat pptDocumentFormat = _documentFormatRegistry.getFormatByExtension("ppt");
    DocumentFormat odtDocumentFormat = _documentFormatRegistry.getFormatByExtension("odt");

    assertNotNull(pdfDocumentFormat);
    assertNotNull(docDocumentFormat);
    assertNotNull(xlsDocumentFormat);
    assertNotNull(xlsmDocumentFormat);
    assertNotNull(pptDocumentFormat);
    assertNotNull(odtDocumentFormat);
  }

  @Test
  public void testGetFormatByMediaType() {
    DocumentFormat pdfDocumentFormat = _documentFormatRegistry.getFormatByMediaType(MimetypeMap.MIMETYPE_PDF);
    DocumentFormat docDocumentFormat = _documentFormatRegistry.getFormatByMediaType(MimetypeMap.MIMETYPE_WORD);
    DocumentFormat xlsDocumentFormat = _documentFormatRegistry.getFormatByMediaType(MimetypeMap.MIMETYPE_EXCEL);
    DocumentFormat pptDocumentFormat = _documentFormatRegistry.getFormatByMediaType(MimetypeMap.MIMETYPE_PPT);

    assertNotNull(pdfDocumentFormat);
    assertNotNull(docDocumentFormat);
    assertNotNull(xlsDocumentFormat);
    assertNotNull(pptDocumentFormat);
  }

  @Test
  public void testGetOutputFormats() {
    Set<DocumentFormat> outputFormats = _documentFormatRegistry.getOutputFormats(DocumentFamily.TEXT);

    assertTrue(outputFormats.size() == 1);

    outputFormats = _documentFormatRegistry.getOutputFormats(DocumentFamily.PRESENTATION);

    assertTrue(outputFormats.size() == 1);

    outputFormats = _documentFormatRegistry.getOutputFormats(DocumentFamily.SPREADSHEET);

    assertTrue(outputFormats.size() == 1);
  }

}
