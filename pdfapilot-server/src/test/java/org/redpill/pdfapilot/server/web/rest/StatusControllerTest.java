package org.redpill.pdfapilot.server.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redpill.pdfapilot.server.Application;
import org.redpill.pdfapilot.server.domain.Status;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
public class StatusControllerTest extends AbstractControllerTest {
  
  @Test
  public void testRequest() throws Exception {
    Status status = createRestTemplate().getForObject("http://" + _hostname + ":" + _port + "/bapi/v1/status", Status.class);

    assertFalse(status.isExpired());
    assertTrue(status.getTime() > 10);
    assertEquals(9, status.getStatus().size());
  }

}
