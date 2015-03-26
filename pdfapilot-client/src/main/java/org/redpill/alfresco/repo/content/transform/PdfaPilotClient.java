package org.redpill.alfresco.repo.content.transform;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.json.JSONObject;

public interface PdfaPilotClient {

  String getVersion();

  JSONObject getStatus();

  void create(String filename, InputStream inputStream, OutputStream outputStream, PdfaPilotTransformationOptions options);

  File create(String filename, File sourceFile, PdfaPilotTransformationOptions options);

  void create(String filename, ContentReader contentReader, ContentWriter contentWriter, PdfaPilotTransformationOptions options);

  void createPdf(String filename, InputStream inputStream, OutputStream outputStream);

  void createPdf(String sourceFilename, ContentReader contentReader, ContentWriter contentWriter);

  File createPdf(String filename, File sourceFile);

  void createPdfa(String filename, InputStream inputStream, OutputStream outputStream);

  File createPdfa(String filename, File sourceFile);

  void createPdfa(String sourceFilename, ContentReader contentReader, ContentWriter contentWriter);

}
