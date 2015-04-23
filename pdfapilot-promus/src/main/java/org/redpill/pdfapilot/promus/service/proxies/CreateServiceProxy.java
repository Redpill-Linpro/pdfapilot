package org.redpill.pdfapilot.promus.service.proxies;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.pdfapilot.promus.service.CreateProcessor;
import org.redpill.pdfapilot.promus.service.CreateService;
import org.redpill.pdfapilot.promus.service.impl.CreateCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("pps.CreateService")
public class CreateServiceProxy implements CreateService {

  private final Log LOG = LogFactory.getLog(getClass());

  @Resource(name = "pps.createService")
  private CreateService _createService;

  @Resource(name = "ppc.threadPoolExecutor")
  private Executor _threadPoolExecutor;

  @Value("${pdfaPilot.taskTimeout}")
  private int _timeout;

  @Override
  public void createPdf(InputStream inputStream, String filename, Map<String, Object> properties, CreateCallback createCallback) {
    FutureTask<Void> task = null;

    try {
      task = new FutureTask<Void>(() -> {
        _createService.createPdf(inputStream, filename, properties, createCallback);
        return null;
      });

      _threadPoolExecutor.execute(task);

      task.get(_timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      task.cancel(true);

      LOG.warn(e.getMessage(), e);
    } catch (InterruptedException e) {
      // We were asked to stop
      task.cancel(true);

      return;
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void createPdfa(InputStream inputStream, String filename, Map<String, Object> properties, String level, CreateCallback createCallback) {
    FutureTask<Void> task = null;

    try {
      task = new FutureTask<Void>(() -> {
        _createService.createPdfa(inputStream, filename, properties, level, createCallback);
        return null;
      });

      _threadPoolExecutor.execute(task);

      task.get(_timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      task.cancel(true);

      LOG.warn(e.getMessage(), e);
    } catch (InterruptedException e) {
      // We were asked to stop
      task.cancel(true);

      return;
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void preProcess(File sourceFile) {
    _createService.preProcess(sourceFile);
  }

  @Override
  public void postProcess(File sourceFile, File targetFile, Long duration, Boolean pdfa) {
    _createService.postProcess(sourceFile, targetFile, duration, pdfa);
  }

  @Override
  public void errorProcess(File sourceFile, Boolean pdfa, Throwable ex) {
    _createService.errorProcess(sourceFile, null, ex);
  }

  @Override
  public void registerProcessor(CreateProcessor processor) {
    _createService.registerProcessor(processor);
  }

}
