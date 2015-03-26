package org.redpill.alfresco.repo.content.transform;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.httpclient.methods.multipart.PartSource;

public class ContentReaderPartSource implements PartSource {

  private String _filename;

  private ContentReader _contentReader;

  public ContentReaderPartSource(String filename, ContentReader contentReader) {
    ParameterCheck.mandatoryString("filename", filename);
    ParameterCheck.mandatory("contentReader", contentReader);

    _filename = filename;
    _contentReader = contentReader;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return _contentReader.getContentInputStream();
  }

  @Override
  public String getFileName() {
    return _filename;
  }

  @Override
  public long getLength() {
    return _contentReader.getSize();
  }

}
