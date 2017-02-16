package org.redpill.alfresco.pdfapilot.client;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.redpill.alfresco.pdfapilot.worker.ContentReaderPartSource;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotTransformationOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("ppc.pdfaPilotClient")
public class PdfaPilotClientImpl implements PdfaPilotClient {

  private static final Logger LOG = Logger.getLogger(PdfaPilotClientImpl.class);

  @Value("${pdfapilot.transformationTimeout}")
  private int _transformationTimeout;

  @Value("${pdfapilot.maxConcurrentConnections}")
  private int _maxConcurrentConnections;

  @Value("${pdfapilot.serverUrl}")
  private String _serverUrl;

  @Value("${pdfapilot.username}")
  private String _username;

  @Value("${pdfapilot.password}")
  private String _password;

  @Value("${pdfapilot.test}")
  private boolean _test;

  private HttpClient _httpClient;

  @Value("${pdfapilot.connectionTimeoutInMillis}")
  private int _connectionTimeoutInMillis;

  @Value("${pdfapilot.aliveCheckTimeout}")
  private int _aliveCheckTimeout;

  @Value("${pdfapilot.maxTotalConnections}")
  private int _maxTotalConnections;

  @Override
  public JSONObject getVersion() {
    HttpClient client = getHttpClient();

    GetMethod method = new GetMethod(_serverUrl + "/bapi/v1/version");
    method.getParams().setSoTimeout(_aliveCheckTimeout * 1000);
    method.getHostAuthState().setPreemptive();
    method.getParams().setContentCharset("UTF-8");

    method.addRequestHeader("Accept-Charset", "UTF-8");
    method.addRequestHeader("Accept-Language", "en-ca,en;q=0.8");
    method.addRequestHeader("Accept-Content", "application/text");

    try {
      int status = client.executeMethod(method);

      String response = IOUtils.toString(method.getResponseBodyAsStream()).trim();

      if (status == 200) {
        return new JSONObject(response);
      }

      throw new RuntimeException("Version check failed: " + method.getStatusLine() + "\n" + response);
    } catch (Exception e) {
      throw new RuntimeException("Version check failed\n", e);
    } finally {
      method.releaseConnection();
    }
  }

  @Override
  public JSONObject getStatus() {
    GetMethod method = new GetMethod(_serverUrl + "/bapi/v1/status");

    try {
      if (_test) {
        JSONObject status = new JSONObject();
        status.put("expired", false);

        return status;
      }

      HttpClient client = getHttpClient();

      method.getParams().setSoTimeout(_aliveCheckTimeout * 1000);
      method.getHostAuthState().setPreemptive();
      method.getParams().setContentCharset("UTF-8");

      method.addRequestHeader("Accept-Charset", "UTF-8");
      method.addRequestHeader("Accept-Language", "en-ca,en;q=0.8");
      method.addRequestHeader("Accept-Content", "application/text");

      int status = client.executeMethod(method);

      String response = IOUtils.toString(method.getResponseBodyAsStream()).trim();

      if (status == 200) {
        return new JSONObject(response);
      }

      throw new RuntimeException("Status check failed: " + method.getStatusLine() + "\n" + response);
    } catch (Exception e) {
      throw new RuntimeException("Status check failed\n", e);
    } finally {
      method.releaseConnection();
    }
  }

  @Override
  public void createPdf(String filename, InputStream inputStream, OutputStream outputStream) {
    create(filename, inputStream, outputStream, null);
  }

  @Override
  public CreateResult createPdf(String filename, File sourceFile) {
    return create(filename, sourceFile, null);
  }

  @Override
  public void createPdfa(String filename, InputStream inputStream, OutputStream outputStream, PdfaPilotTransformationOptions options) {
    create(filename, inputStream, outputStream, options);
  }

  @Override
  public CreateResult createPdfa(String filename, File sourceFile, PdfaPilotTransformationOptions options) {
    return create(filename, sourceFile, options);
  }

  @Override
  public String createPdf(String sourceFilename, ContentReader contentReader, ContentWriter contentWriter) {
    return create(sourceFilename, contentReader, contentWriter, null);
  }

  @Override
  public String createPdfa(String sourceFilename, ContentReader contentReader, ContentWriter contentWriter, PdfaPilotTransformationOptions options) {
    return create(sourceFilename, contentReader, contentWriter, options);
  }

  @Override
  public void create(String filename, InputStream inputStream, OutputStream outputStream, PdfaPilotTransformationOptions options) {
    HttpClient client = getHttpClient();

    String pdfaLevel = options != null ? options.getLevel() : "";
    String variant = StringUtils.isBlank(pdfaLevel) ? "pdf" : "pdfa";
    String url = _serverUrl + "/bapi/v1/create/" + variant;

    if (LOG.isDebugEnabled()) {
      LOG.debug(url);
    }

    PostMethod method = new PostMethod(url);
    method.getParams().setSoTimeout(_aliveCheckTimeout * 1000);
    method.getHostAuthState().setPreemptive();
    method.getParams().setContentCharset("UTF-8");

    method.addRequestHeader("Accept-Charset", "UTF-8");
    method.addRequestHeader("Accept-Language", "en-ca,en;q=0.8");
    // method.addRequestHeader("Accept-Content", "application/text");

    try {
      JSONObject json = new JSONObject();
      json.put("nodeRef", options != null && options.getSourceNodeRef() != null ? options.getSourceNodeRef().toString() : null);

      // create a new array of Part objects
      List<Part> parts = new ArrayList<Part>();

      // add the file part, the actual binary data
      parts.add(new FilePart("file", new ByteArrayPartSource(FilenameUtils.getName(filename), IOUtils.toByteArray(inputStream)), null, "UTF-8"));

      // add the filename part
      parts.add(new StringPart("filename", FilenameUtils.getName(filename), "UTF-8"));

      // *if* there's a pdfaLavel part, and that
      if (StringUtils.isNotBlank(pdfaLevel)) {
        parts.add(new StringPart("level", pdfaLevel, "UTF-8"));
      }

      // and last, add the data part, this is a json object
      parts.add(new StringPart("data", json.toString(), "UTF-8"));

      method.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[0]), method.getParams()));

      int status = client.executeMethod(method);

      if (status != 200) {
        throw new RuntimeException("Create PDF failed: " + method.getStatusLine() + "\n" + method.getResponseBodyAsString().trim());
      }

      IOUtils.copy(method.getResponseBodyAsStream(), outputStream);
    } catch (Throwable e) {
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      method.releaseConnection();

      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(outputStream);
    }
  }

  @Override
  public CreateResult create(String filename, File sourceFile, PdfaPilotTransformationOptions options) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Creating PDF from '" + sourceFile.getAbsolutePath() + "' with filename '" + filename + "'.");
    }

    HttpClient client = getHttpClient();

    String pdfaLevel = options != null ? options.getLevel() : null;

    String variant = StringUtils.isNotBlank(pdfaLevel) ? "pdfa" : "pdf";

    PostMethod method = new PostMethod(_serverUrl + "/bapi/v1/create/" + variant);
    method.getParams().setSoTimeout(_aliveCheckTimeout * 1000);
    method.getHostAuthState().setPreemptive();
    method.getParams().setContentCharset("UTF-8");

    method.addRequestHeader("Accept-Charset", "UTF-8");
    method.addRequestHeader("Accept-Language", "en-ca,en;q=0.8");
    method.addRequestHeader("Accept-Content", "application/text");
    method.addRequestHeader("Accept", "application/json, */*");

    try {
      JSONObject json = new JSONObject();
      json.put("nodeRef", options != null && options.getSourceNodeRef() != null ? options.getSourceNodeRef().toString() : null);

      // create a new array of Part objects
      List<Part> parts = new ArrayList<Part>();

      parts.add(new FilePart("file", sourceFile, null, "UTF-8"));
      parts.add(new StringPart("filename", filename, "UTF-8"));

      // *if* there's a pdfaLavel part, and that
      if (StringUtils.isNotBlank(pdfaLevel)) {
        parts.add(new StringPart("level", pdfaLevel, "UTF-8"));
      }

      parts.add(new StringPart("data", json.toString(), "UTF-8"));

      method.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[0]), method.getParams()));

      int status = client.executeMethod(method);

      if (status != 200) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Create PDF failed: " + method.getStatusLine() + "\n" + method.getResponseBodyAsString().trim());
        }

        throw new RuntimeException("Create PDF failed: " + method.getStatusLine() + "\n" + method.getResponseBodyAsString().trim());
      }

      File destination = TempFileProvider.createTempFile("pdfaPilot", ".pdf");

      FileUtils.copyInputStreamToFile(method.getResponseBodyAsStream(), destination);
      String id = method.getResponseHeader(PdfaPilotClient.RESPONSE_ID_HEADER).getValue();

      return new CreateResult(destination, id);
    } catch (Exception e) {
      throw new RuntimeException("Create PDF failed\n", e);
    } finally {
      method.releaseConnection();
    }
  }

  @Override
  public String create(String filename, ContentReader contentReader, ContentWriter contentWriter, PdfaPilotTransformationOptions options) {
    HttpClient client = getHttpClient();

    String pdfaLevel = options != null ? options.getLevel() : null;

    String variant = StringUtils.isNotBlank(pdfaLevel) ? "pdfa" : "pdf";

    PostMethod method = new PostMethod(_serverUrl + "/bapi/v1/create/" + variant);

    method.getParams().setSoTimeout(_aliveCheckTimeout * 1000);
    method.getHostAuthState().setPreemptive();
    method.getParams().setContentCharset("UTF-8");

    method.addRequestHeader("Accept-Charset", "UTF-8");
    method.addRequestHeader("Accept-Language", "en-ca,en;q=0.8");
    method.addRequestHeader("Accept-Content", "application/text");

    try {
      JSONObject json = new JSONObject();
      json.put("nodeRef", options.getSourceNodeRef());

      Part[] parts = new Part[4];

      parts[0] = new FilePart("file", new ContentReaderPartSource(filename, contentReader), null, "UTF-8");
      parts[1] = new StringPart("filename", FilenameUtils.getName(filename), "UTF-8");
      parts[2] = new StringPart("level", pdfaLevel, "UTF-8");
      parts[3] = new StringPart("data", json.toString(), "UTF-8");

      method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));

      int status = client.executeMethod(method);

      if (status != 200) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Create PDF failed: " + method.getStatusLine() + "\n" + method.getResponseBodyAsString().trim());
        }

        throw new RuntimeException("Create PDF failed: " + method.getStatusLine() + "\n" + method.getResponseBodyAsString().trim());
      }

      contentWriter.putContent(method.getResponseBodyAsStream());

      return method.getRequestHeader(RESPONSE_ID_HEADER).getValue();
    } catch (Exception ex) {
      throw new RuntimeException("Create PDF failed\n", ex);
    } finally {
      method.releaseConnection();
    }
  }

  @Override
  public void auditCreationResult(String id, boolean verified) {
    HttpClient client = getHttpClient();

    PostMethod method = new PostMethod(_serverUrl + "/bapi/v1/create/audit");

    method.getParams().setSoTimeout(_aliveCheckTimeout * 1000);
    method.getHostAuthState().setPreemptive();
    method.getParams().setContentCharset("UTF-8");

    method.addRequestHeader("Accept-Charset", "UTF-8");
    method.addRequestHeader("Accept-Language", "en-ca,en;q=0.8");
    method.addRequestHeader("Accept-Content", "application/text");

    try {
      method.addParameter("id", id);
      method.addParameter("verified", verified ? "true" : "false");

      int status = client.executeMethod(method);

      if (status != 200) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Audit Create PDF failed: " + method.getStatusLine() + "\n" + method.getResponseBodyAsString().trim());
        }

        throw new RuntimeException("Audit Create PDF failed: " + method.getStatusLine() + "\n" + method.getResponseBodyAsString().trim());
      }
    } catch (Exception ex) {
      throw new RuntimeException("Audit Create PDF failed\n", ex);
    } finally {
      method.releaseConnection();
    }
  }

  @Override
  public boolean isConnected() {
    try {
      boolean connected = getStatus() != null;

      if (connected) {
        LOG.debug("pdfaPilot is connected...");
      } else {
        LOG.debug("pdfaPilot is NOT connected...");
      }

      return connected;
    } catch (Throwable ex) {
      if (LOG.isTraceEnabled()) {
        LOG.trace(ex.getMessage(), ex);
      }

      return false;
    }
  }

  @Override
  public boolean isLicensed() {
    try {
      boolean licensed = !getStatus().getBoolean("expired");

      if (licensed) {
        LOG.debug("pdfaPilot is licensed...");
      } else {
        LOG.debug("pdfaPilot is NOT licensed...");
      }

      return licensed;
    } catch (Throwable ex) {
      if (LOG.isTraceEnabled()) {
        LOG.trace(ex.getMessage(), ex);
      }

      return false;
    }
  }

  private HttpClient getHttpClient() {
    if (_httpClient == null) {
      _httpClient = createHttpClient(0, _connectionTimeoutInMillis);
    }

    return _httpClient;
  }

  private HttpClient createHttpClient(int retries, int connectionTimeoutInMillis) {
    MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

    HttpClient client = new HttpClient(connectionManager);

    client.getHttpConnectionManager().getParams().setTcpNoDelay(true);

    client.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeoutInMillis);
    
    
    client.getHttpConnectionManager().getParams().setSoTimeout(_transformationTimeout * 1000);

    client.getHttpConnectionManager().getParams().setMaxTotalConnections(_maxTotalConnections);
    client.getHttpConnectionManager().getParams().setDefaultMaxConnectionsPerHost(_maxConcurrentConnections);
    URI uri;

    try {
      uri = new URI(_serverUrl);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    HostConfiguration hostConfiguration = new HostConfiguration();
    hostConfiguration.setHost(uri.getHost(), uri.getPort(), uri.getScheme());
    client.getHttpConnectionManager().getParams().setMaxConnectionsPerHost(hostConfiguration, _maxConcurrentConnections);

    client.getState().setCredentials(new AuthScope(null, -1, null), new UsernamePasswordCredentials(_username, _password));

    client.getParams().setAuthenticationPreemptive(true);
    client.getParams().setConnectionManagerTimeout(connectionTimeoutInMillis);

    DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(retries, true);
    client.getParams().setParameter("http.method.retry-handler", retryhandler);

    return client;
  }

  public class CreateResult {

    public CreateResult(File file, String id) {
      this.file = file;
      this.id = id;
    }

    public File file;

    public String id;

  }

}
