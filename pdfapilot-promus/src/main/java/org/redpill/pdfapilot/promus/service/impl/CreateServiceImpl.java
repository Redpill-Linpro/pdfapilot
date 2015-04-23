package org.redpill.pdfapilot.promus.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.pdfapilot.promus.domain.CreatePdfResult;
import org.redpill.pdfapilot.promus.service.CreateProcessor;
import org.redpill.pdfapilot.promus.service.CreateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("pps.createService")
public class CreateServiceImpl implements CreateService {

  private final Log LOG = LogFactory.getLog(getClass());

  @Value("${pdfaPilot.exe}")
  private String _executable;

  @Value("${pdfaPilot.endpoint}")
  private String _endpoint;

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
  
  @Value("${pdfaPilot.nooptimization}")
  private boolean _nooptimization;

  @Autowired
  private ApplicationEventPublisher _applicationEventPublisher;

  private Set<CreateProcessor> _processors = new HashSet<CreateProcessor>();

  @Autowired
  private AuditEventRepository _auditEventRepository;

  @Override
  public void createPdf(InputStream inputStream, String filename, Map<String, Object> properties, CreateCallback createCallback) {
    Map<String, String> options = new HashMap<String, String>();
    options.put("optimize", null);

    createPdf(inputStream, filename, options, properties, createCallback);
  }

  @Override
  public void createPdfa(InputStream inputStream, String filename, Map<String, Object> properties, String level, CreateCallback createCallback) {
    Map<String, String> options = new HashMap<String, String>();
    options.put("level", level);

    createPdf(inputStream, filename, options, properties, createCallback);
  }

  protected void createPdf(InputStream inputStream, String filename, Map<String, String> options, Map<String, Object> properties, CreateCallback createCallback) {
    File sourceFile = null;
    File targetFile = null;
    File tempDir = null;
    boolean pdfa = false;

    try {
      List<String> commandList = new ArrayList<String>();

      commandList.add(_executable);
      commandList.add("--timeout=" + _timeout);

      if (options.containsKey("optimize")) {
        commandList.add("--optimizepdf");
      }

      if (options.containsKey("level")) {
        commandList.add("--level=" + options.get("level"));
        pdfa = true;
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

      if (options.containsKey("nooptimization") || _nooptimization) {
        commandList.add("--nooptimization");
      }

      if (StringUtils.isNotBlank(_endpoint)) {
        commandList.add("--dist");
        commandList.add("--endpoint=" + _endpoint);
      }

      File systemTempDir = FileUtils.getTempDirectory();
      tempDir = new File(systemTempDir, UUID.randomUUID().toString());
      FileUtils.forceMkdir(tempDir);

      sourceFile = new File(tempDir, FilenameUtils.getName(filename));
      FileUtils.copyInputStreamToFile(inputStream, sourceFile);

      commandList.add(sourceFile.getAbsolutePath());

      String[] command = commandList.toArray(new String[commandList.size()]);

      LOG.debug("Executing: " + StringUtils.join(command, " "));

      Long start = System.currentTimeMillis();

      preProcess(sourceFile);

      ExecCommand exec = new ExecCommand(command);

      LOG.debug("Std err: " + exec.getError());

      if (exec.getExitValue() >= 100) {
        LOG.debug(exec.getOutput());
        throw new RuntimeException("Execution of " + StringUtils.join(command, " ") + " failed with exit value " + exec.getExitValue());
      }

      for (String line : exec.getOutput()) {
        if (!line.startsWith("Output")) {
          continue;
        }

        line = StringUtils.replace(line, "Output", "").trim();

        targetFile = new File(line);
      }

      Long duration = System.currentTimeMillis() - start;

      LOG.debug("Time: " + duration);

      postProcess(sourceFile, targetFile, duration, pdfa);

      String id = auditSuccess(sourceFile, targetFile, pdfa, properties, duration);

      createCallback.handleFile(targetFile, id);
    } catch (Throwable ex) {
      errorProcess(sourceFile, pdfa, ex);

      String id = auditFailure(sourceFile, targetFile, pdfa, properties, ex);

      throw new RuntimeException("Creation failed, create id '" + id + "'", ex);
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
  public void postProcess(File sourceFile, File targetFile, Long duration, Boolean pdfa) {
    for (CreateProcessor processor : _processors) {
      processor.postProcess(sourceFile, targetFile, duration, pdfa);
    }
  }

  @Override
  public void errorProcess(File sourceFile, Boolean pdfa, Throwable ex) {
    for (CreateProcessor processor : _processors) {
      processor.errorProcess(sourceFile, pdfa, ex);
    }
  }

  @Override
  public void registerProcessor(CreateProcessor processor) {
    if (_processors.contains(processor)) {
      return;
    }

    _processors.add(processor);
  }

  private String auditSuccess(File sourceFile, File targetFile, boolean pdfa, Map<String, Object> properties, Long duration) {
    String sourceFilename = sourceFile != null ? sourceFile.getName() : null;
    Long sourceLength = sourceFile != null ? sourceFile.length() : null;
    String targetFilename = targetFile != null ? targetFile.getName() : null;
    Long targetLength = targetFile != null ? targetFile.length() : null;
    String id = UUID.randomUUID().toString();

    CreatePdfResult result = new CreatePdfResult(id, sourceFilename, sourceLength, targetFilename, targetLength, pdfa, properties, duration, null);

    _applicationEventPublisher.publishEvent(new CreatePdfEvent(this, result));

    return id;
  }

  private String auditFailure(File sourceFile, File targetFile, boolean pdfa, Map<String, Object> properties, Throwable exception) {
    String sourceFilename = sourceFile != null ? sourceFile.getName() : null;
    Long sourceLength = sourceFile != null ? sourceFile.length() : null;
    String targetFilename = targetFile != null ? targetFile.getName() : null;
    Long targetLength = targetFile != null ? targetFile.length() : null;
    String stacktrace = ExceptionUtils.getStackTrace(exception);
    String id = UUID.randomUUID().toString();

    CreatePdfResult result = new CreatePdfResult(id, sourceFilename, sourceLength, targetFilename, targetLength, pdfa, properties, null, stacktrace);

    _applicationEventPublisher.publishEvent(new CreatePdfEvent(this, result));

    return id;
  }

}
