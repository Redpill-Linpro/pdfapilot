package org.redpill.pdfapilot.promus.web.rest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redpill.pdfapilot.promus.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
public class VersionResourceTest extends AbstractControllerTest {

  @Value("${local.server.port}")
  private int _port;

  @Test
  public void testRequest() throws Exception {
    String version = _restTemplate.getForObject("http://localhost:" + _port + "/bapi/v1/version", String.class);

    System.out.println(version);

    assertTrue(version.indexOf("callas pdfaPilot CLI") >= 0);
  }

}
