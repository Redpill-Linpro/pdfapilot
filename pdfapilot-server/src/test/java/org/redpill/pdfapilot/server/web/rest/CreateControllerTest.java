package org.redpill.pdfapilot.server.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redpill.pdfapilot.server.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.itextpdf.text.pdf.PdfReader;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
public class CreateControllerTest extends AbstractControllerTest {

  private static final String APPLICATION_PDF = "application/pdf";

  @Value("${local.server.port}")
  private int _port;

  @Test
  public void testCreatePdf() throws Exception {
    createPdf(false);
  }

  @Test
  public void testCreatePdfa() throws Exception {
    createPdf(true);
  }

  public void createPdf(boolean pdfa) throws Exception {
    RestTemplate template = createRestTemplate();
    
    String url = "http://" + _hostname + ":" + _port + "/bapi/v1/create/" + (pdfa ? "pdfa" : "pdf");

    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();

    parts.add("level", "2b");
    parts.add("filename", "test.doc");
    parts.add("file", new ClassPathResource("test.doc"));

    ClientHttpRequestInterceptor acceptHeaderPdf = new AcceptHeaderHttpRequestInterceptor(APPLICATION_PDF);
    template.setInterceptors(Arrays.asList(acceptHeaderPdf));

    ResponseEntity<byte[]> response = template.postForEntity(url, parts, byte[].class);

    assertTrue(response.getStatusCode().is2xxSuccessful());

    PdfReader pdfReader = new PdfReader(response.getBody());

    assertEquals(6, pdfReader.getNumberOfPages());
  }

  class AcceptHeaderHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final String _headerValue;

    public AcceptHeaderHttpRequestInterceptor(String headerValue) {
      _headerValue = headerValue;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

      HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);
      requestWrapper.getHeaders().setAccept(Arrays.asList(MediaType.valueOf(_headerValue)));

      return execution.execute(requestWrapper, body);
    }
  }
}
