package org.redpill.pdfapilot.promus.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redpill.pdfapilot.promus.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
    createPdf("teståäöÅÄÖ.doc", false, APPLICATION_PDF);
  }

  @Test
  public void testCreatePdf2() throws Exception {
    createPdf("test2.doc", false, APPLICATION_PDF);
  }

  @Test
  public void testCreatePdfa() throws Exception {
    createPdf("teståäöÅÄÖ.doc", true, APPLICATION_PDF);
  }

  @Test
  public void testCreatePdfa2() throws Exception {
    createPdf("test2.pdf", true, "application/json");
  }

  @Test
  public void testCreatePdfa3() throws Exception {
    createPdf("test3.pdf", true, "application/json");
  }

  @Test
  public void testAudit() {
    String url = "http://localhost:" + _port + "/bapi/v1/create/pdf";

    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();

    parts.add("level", "2b");
    parts.add("filename", " test.doc");
    parts.add("file", new ClassPathResource("test.doc"));

    ClientHttpRequestInterceptor acceptHeaderPdf = new AcceptHeaderHttpRequestInterceptor(APPLICATION_PDF);
    _restTemplate.setInterceptors(Arrays.asList(acceptHeaderPdf));

    ResponseEntity<byte[]> response = _restTemplate.postForEntity(url, parts, byte[].class);

    assertTrue(response.getStatusCode().is2xxSuccessful());

    url = "http://localhost:" + _port + "/bapi/v1/create/audit";

    String id = response.getHeaders().getFirst("X-CreatePDF-ID");

    MultiValueMap<String, String> request = new LinkedMultiValueMap<String, String>();
    request.add("id", id);
    request.add("verified", "true");

    ResponseEntity<Void> response2 = _restTemplate.postForEntity(url, request, Void.class);

    assertTrue(response2.getStatusCode().is2xxSuccessful());
  }

  public void createPdf(String filename, boolean pdfa, String resultContentType) throws Exception {
    String url = "http://localhost:" + _port + "/bapi/v1/create/" + (pdfa ? "pdfa" : "pdf");

    HttpHeaders textHeaders = new HttpHeaders();
    textHeaders.setContentType(new MediaType("text", "plain", Charset.forName("UTF-8")));

    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();

    if (pdfa) {
      parts.add("level", new HttpEntity<String>("2b", textHeaders));
    }

    parts.add("filename", new HttpEntity<String>(" " + filename + " ", textHeaders));
    parts.add("file", new ClassPathResource(filename));
    parts.add("data", "{\"nodeRef\": \"workspace:SpacesStore/custom_node_ref\"}");

    ClientHttpRequestInterceptor acceptHeaderPdf = new AcceptHeaderHttpRequestInterceptor(resultContentType);
    _restTemplate.setInterceptors(Arrays.asList(acceptHeaderPdf));

    ResponseEntity<byte[]> response = _restTemplate.postForEntity(url, parts, byte[].class);
    
    if (!response.getStatusCode().is2xxSuccessful()) {
      assertEquals(400, response.getStatusCode().value());
      assertEquals("application/json", resultContentType);
    } else {
      assertEquals("application/pdf", resultContentType);
      
      PdfReader pdfReader = new PdfReader(response.getBody());

      assertEquals(6, pdfReader.getNumberOfPages());
    }
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
