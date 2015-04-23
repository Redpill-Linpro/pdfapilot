package org.redpill.pdfapilot.promus.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
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
    createPdf(false);
  }

  @Test
  public void testCreatePdfa() throws Exception {
    createPdf(true);
  }

  @Test
  public void testAudit() {
    String url = "http://localhost:" + _port + "/bapi/v1/create/pdf";

    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();

    parts.add("level", "2b");
    parts.add("filename", "test.doc");
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

  public void createPdf(boolean pdfa) throws Exception {
    String url = "http://localhost:" + _port + "/bapi/v1/create/" + (pdfa ? "pdfa" : "pdf");
    String filename = "teståäöÅÄÖ.doc";

    HttpHeaders textHeaders = new HttpHeaders();
    textHeaders.setContentType(new MediaType("text", "plain", Charset.forName("UTF-8")));

    HttpHeaders wordHeaders = new HttpHeaders();
    wordHeaders.setContentType(new MediaType("application", "msword", Charset.forName("UTF-8")));

    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();

    parts.add("level", new HttpEntity<String>("2b", textHeaders));
    parts.add("filename", new HttpEntity<String>(filename, textHeaders));
    // parts.add("file", new HttpEntity<ClassPathResource>(new ClassPathResource(filename), wordHeaders));
    parts.add("file", new ClassPathResource(filename));

    ClientHttpRequestInterceptor acceptHeaderPdf = new AcceptHeaderHttpRequestInterceptor(APPLICATION_PDF);
    _restTemplate.setInterceptors(Arrays.asList(acceptHeaderPdf));

    // ResponseEntity<byte[]> response = _restTemplate.postForEntity(url, parts,
    // byte[].class);
    ResponseEntity<byte[]> response = _restTemplate.postForEntity(url, parts, byte[].class);

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
