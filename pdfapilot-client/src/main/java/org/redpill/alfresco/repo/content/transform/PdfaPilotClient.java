package org.redpill.alfresco.repo.content.transform;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.json.JSONObject;
import org.redpill.alfresco.repo.content.transform.PdfaPilotClientImpl.CreateResult;

public interface PdfaPilotClient {
  
  static final String RESPONSE_ID_HEADER = "X-CreatePDF-ID";

  JSONObject getVersion();

  JSONObject getStatus();

  void create(String filename, InputStream inputStream, OutputStream outputStream, PdfaPilotTransformationOptions options);

  CreateResult create(String filename, File sourceFile, PdfaPilotTransformationOptions options);

  String create(String filename, ContentReader contentReader, ContentWriter contentWriter, PdfaPilotTransformationOptions options);

  void createPdf(String filename, InputStream inputStream, OutputStream outputStream);

  String createPdf(String sourceFilename, ContentReader contentReader, ContentWriter contentWriter);

  CreateResult createPdf(String filename, File sourceFile);

  void createPdfa(String filename, InputStream inputStream, OutputStream outputStream);

  CreateResult createPdfa(String filename, File sourceFile);

  String createPdfa(String sourceFilename, ContentReader contentReader, ContentWriter contentWriter);
  
  boolean isConnected();
  
  boolean isLicensed();

  void auditCreationResult(String id, boolean verified);

}
