package org.redpill.pdfapilot.server.web.rest;

import java.net.URI;

import javax.annotation.Nullable;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class AuthHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

  protected HttpHost _host;

  @Nullable
  protected String _username;

  @Nullable
  protected String _password;

  public AuthHttpComponentsClientHttpRequestFactory(HttpHost host) {
    this(host, null, null);
  }

  public AuthHttpComponentsClientHttpRequestFactory(HttpHost host, @Nullable String userName, @Nullable String password) {
    super();
    _host = host;
    _username = userName;
    _password = password;
  }

  public AuthHttpComponentsClientHttpRequestFactory(HttpClient httpClient, HttpHost host) {
    this(httpClient, host, null, null);
  }

  public AuthHttpComponentsClientHttpRequestFactory(HttpClient httpClient, HttpHost host, @Nullable String userName, @Nullable String password) {
    super(httpClient);
    _host = host;
    _username = userName;
    _password = password;
  }

  @Override
  protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
    // Create AuthCache instance
    AuthCache authCache = new BasicAuthCache();
    // Generate BASIC scheme object and add it to the local auth cache
    BasicScheme basicAuth = new BasicScheme();
    authCache.put(_host, basicAuth);

    // Add AuthCache to the execution context
    HttpClientContext localcontext = HttpClientContext.create();
    localcontext.setAuthCache(authCache);

    if (_username != null) {
      BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(new AuthScope(_host), new UsernamePasswordCredentials(_username, _password));
      localcontext.setCredentialsProvider(credsProvider);
    }
    
    return localcontext;
  }

}