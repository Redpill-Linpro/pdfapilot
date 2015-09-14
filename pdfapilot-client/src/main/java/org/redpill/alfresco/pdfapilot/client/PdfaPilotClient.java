package org.redpill.alfresco.pdfapilot.client;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.json.JSONObject;
import org.redpill.alfresco.pdfapilot.client.PdfaPilotClientImpl.CreateResult;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotTransformationOptions;

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

  void createPdfa(String filename, InputStream inputStream, OutputStream outputStream, PdfaPilotTransformationOptions options);

  CreateResult createPdfa(String filename, File sourceFile, PdfaPilotTransformationOptions options);

  String createPdfa(String sourceFilename, ContentReader contentReader, ContentWriter contentWriter, PdfaPilotTransformationOptions options);
  
  boolean isConnected();
  
  boolean isLicensed();

  void auditCreationResult(String id, boolean verified);

}
