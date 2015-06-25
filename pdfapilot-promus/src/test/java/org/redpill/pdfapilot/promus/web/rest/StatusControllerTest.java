package org.redpill.pdfapilot.promus.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.redpill.pdfapilot.promus.domain.Status;

public class StatusControllerTest extends AbstractControllerTest {
  
  @Test
  public void testRequest() throws Exception {
    Status status = _restTemplate.getForObject("http://localhost:" + _port + "/bapi/v1/status", Status.class);
    
    assertFalse(status.isExpired());
    assertTrue(status.getTime() > 10);
    assertEquals(9, status.getStatus().size());
  }

}
