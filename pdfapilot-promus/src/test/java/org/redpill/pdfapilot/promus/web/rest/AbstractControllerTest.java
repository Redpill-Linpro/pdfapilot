package org.redpill.pdfapilot.promus.web.rest;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.redpill.pdfapilot.promus.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port:0")
public abstract class AbstractControllerTest {

  @Value("${local.server.port}")
  protected int _port;

  protected RestTemplate _restTemplate;

  @Before
  public void init() {
    HttpHost host = new HttpHost("localhost", _port);

    String username = "admin";
    String password = "admin";

    _restTemplate = new TestRestTemplate();

    AuthHttpComponentsClientHttpRequestFactory requestFactory = new AuthHttpComponentsClientHttpRequestFactory(host, username, password);

    _restTemplate.setRequestFactory(requestFactory);

    for (HttpMessageConverter<?> converter : _restTemplate.getMessageConverters()) {
      if (converter instanceof FormHttpMessageConverter) {
        FormHttpMessageConverter formConverter = (FormHttpMessageConverter) converter;
        formConverter.setMultipartCharset(Charset.forName("UTF-8"));
        formConverter.setCharset(Charset.forName("UTF-8"));
      }
    }
  }

}
