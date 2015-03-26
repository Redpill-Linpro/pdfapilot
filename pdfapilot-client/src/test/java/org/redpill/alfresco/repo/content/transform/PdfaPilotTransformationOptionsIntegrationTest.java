package org.redpill.alfresco.repo.content.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;

public class PdfaPilotTransformationOptionsIntegrationTest extends AbstractRepoIntegrationTest {

  @Resource(name = "pdfaPilot.pdfaOptions")
  private PdfaPilotTransformationOptions _pdfaOptions;

  @Resource(name = "pdfaPilot.pdfOptions")
  private PdfaPilotTransformationOptions _pdfOptions;

  @Test
  public void testPdfaOptions() {
    assertEquals("2b", _pdfaOptions.getLevel());
    assertFalse(_pdfaOptions.isOptimize());
  }

  @Test
  public void testPdfOptions() {
    assertTrue(StringUtils.isBlank(_pdfOptions.getLevel()));
    assertTrue(_pdfOptions.isOptimize());
  }

}
