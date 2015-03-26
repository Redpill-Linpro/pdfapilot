package org.redpill.pdfapilot.server.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.pdfapilot.server.service.CreateProcessor;
import org.redpill.pdfapilot.server.service.CreateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("pps.createService")
public class CreateServiceImpl implements CreateService {

  private final Log LOG = LogFactory.getLog(getClass());

  @Value("${pdfaPilot.exe}")
  private String _executable;

  @Value("${pdfaPilot.timeout}")
  private int _timeout;

  @Value("${pdfaPilot.noprogress}")
  private boolean _noprogress;

  @Value("${pdfaPilot.nohits}")
  private boolean _nohits;

  @Value("${pdfaPilot.nosummary}")
  private boolean _nosummary;

  @Value("${pdfaPilot.forceopenoffice}")
  private boolean _forceopenoffice;

  private Set<CreateProcessor> _processors = new HashSet<CreateProcessor>();

  @Override
  public void createPdf(InputStream inputStream, String filename, CreateCallback createCallback) {
    Map<String, String> options = new HashMap<String, String>();
    options.put("optimize", null);

    createPdf(inputStream, filename, options, createCallback);
  }

  @Override
  public void createPdfa(InputStream inputStream, String filename, String level, CreateCallback createCallback) {
    Map<String, String> options = new HashMap<String, String>();
    options.put("level", level);

    createPdf(inputStream, filename, options, createCallback);
  }

  protected void createPdf(InputStream inputStream, String filename, Map<String, String> options, CreateCallback createCallback) {
    File sourceFile = null;
    File targetFile = null;
    File tempDir = null;

    try {
      List<String> commandList = new ArrayList<String>();

      File systemTempDir = FileUtils.getTempDirectory();
      tempDir = new File(systemTempDir, UUID.randomUUID().toString());
      FileUtils.forceMkdir(tempDir);

      sourceFile = new File(tempDir, FilenameUtils.getName(filename));

      FileUtils.copyInputStreamToFile(inputStream, sourceFile);

      commandList.add(_executable);
      commandList.add("--timeout=" + _timeout);

      if (options.containsKey("optimize")) {
        commandList.add("--optimizepdf");
      }

      if (options.containsKey("level")) {
        commandList.add("--level=" + options.get("level"));
      }

      if ((options.containsKey("nohits") || _nohits) && !options.containsKey("optimize")) {
        commandList.add("--nohits");
      }

      if ((options.containsKey("nosummary") || _nosummary) && !options.containsKey("optimize")) {
        commandList.add("--nosummary");
      }

      if (options.containsKey("noprogress") || _noprogress) {
        commandList.add("--noprogress");
      }

      if (options.containsKey("forceopenoffice") || _forceopenoffice) {
        commandList.add("--topdf_forceopenoffice");
      }

      commandList.add(sourceFile.getAbsolutePath());

      String command = StringUtils.join(commandList, " ");

      LOG.debug("Executing: " + command);

      Long start = System.currentTimeMillis();

      preProcess(sourceFile);

      Process process = Runtime.getRuntime().exec(command);
      process.waitFor();

      List<String> lines = Arrays.asList(IOUtils.toString(process.getInputStream()).split("\n"));

      for (String line : lines) {
        if (!line.startsWith("Output")) {
          continue;
        }

        line = StringUtils.replace(line, "Output", "").trim();

        targetFile = new File(line);
      }

      Long total = System.currentTimeMillis() - start;

      LOG.debug("Time: " + total);

      postProcess(targetFile);

      createCallback.handleFile(targetFile);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      FileUtils.deleteQuietly(tempDir);
    }
  }

  @Override
  public void preProcess(File sourceFile) {
    for (CreateProcessor processor : _processors) {
      processor.preProcess(sourceFile);
    }
  }

  @Override
  public void postProcess(File targetFile) {
    for (CreateProcessor processor : _processors) {
      processor.postProcess(targetFile);
    }
  }

  @Override
  public void registerProcessor(CreateProcessor processor) {
    if (_processors.contains(processor)) {
      return;
    }

    _processors.add(processor);
  }

}
