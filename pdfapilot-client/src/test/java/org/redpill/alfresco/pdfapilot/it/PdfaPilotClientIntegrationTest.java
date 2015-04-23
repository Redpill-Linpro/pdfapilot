package org.redpill.alfresco.pdfapilot.it;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.redpill.alfresco.repo.content.transform.PdfaPilotClient;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PdfaPilotClientIntegrationTest extends AbstractRepoIntegrationTest {

  @Autowired
  @Qualifier("ppc.pdfaPilotClient")
  private PdfaPilotClient _client;

  @Test
  public void testGetVersion() throws JSONException {
    JSONObject version = _client.getVersion();

    assertTrue(version.getString("version").indexOf("callas pdfaPilot CLI") >= 0);
  }

  @Test
  public void testGetStatus() throws JSONException {
    JSONObject status = _client.getStatus();

    assertFalse(status.getBoolean("expired"));
  }

  @Test
  public void testCreatePdf() throws IOException {
    String filename = "test.doc";

    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    _client.createPdf(filename, inputStream, outputStream);

    assertTrue(outputStream.size() > 72000);
  }

  @Test
  public void testCreatePdfa() {
    String filename = "test.doc";

    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    _client.createPdfa(filename, inputStream, outputStream);

    assertTrue(outputStream.size() < 64000);
  }

}
