package org.redpill.pdfapilot.server.web.rest;

import org.apache.http.HttpHost;
import org.junit.runner.RunWith;
import org.redpill.pdfapilot.server.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
public abstract class AbstractControllerTest {

  @Value("${local.server.port}")
  protected int _port;

  protected String _hostname = "localhost";

  protected RestTemplate createRestTemplate() {
    HttpHost host = new HttpHost(_hostname, _port);
    
    String username = "niklas";
    String password = "niklas";
    
    RestTemplate template = new TestRestTemplate();

    AuthHttpComponentsClientHttpRequestFactory requestFactory = new AuthHttpComponentsClientHttpRequestFactory(host, username, password);

    template.setRequestFactory(requestFactory);

    return template;
  }

}
