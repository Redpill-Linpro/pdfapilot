package org.redpill.alfresco.pdfapilot.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotTransformationOptions;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Ignore
public class PdfaPilotTransformationOptionsIntegrationTest extends AbstractRepoIntegrationTest {

  @Autowired
  @Qualifier("pdfaPilot.pdfaOptions")
  private PdfaPilotTransformationOptions _pdfaOptions;

  @Qualifier( "pdfaPilot.pdfOptions")
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
