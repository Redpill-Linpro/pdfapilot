package org.redpill.pdfapilot.server.service.proxies;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.pdfapilot.server.service.CreateProcessor;
import org.redpill.pdfapilot.server.service.CreateService;
import org.redpill.pdfapilot.server.service.impl.CreateCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("pps.CreateService")
public class CreateServiceProxy implements CreateService {

  private final Log LOG = LogFactory.getLog(getClass());

  @Resource(name = "pps.createService")
  private CreateService _createService;

  @Autowired
  private ThreadPoolExecutor _threadPoolExecutor;

  @Value("${pdfaPilot.taskTimeout}")
  private int _timeout;

  @Override
  public void createPdf(InputStream inputStream, String filename, CreateCallback createCallback) {
    FutureTask<Void> task = null;

    try {
      task = new FutureTask<Void>(() -> {
        _createService.createPdf(inputStream, filename, createCallback);
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
  public void createPdfa(InputStream inputStream, String filename, String level, CreateCallback createCallback) {
    FutureTask<Void> task = null;

    try {
      task = new FutureTask<Void>(() -> {
        _createService.createPdfa(inputStream, filename, level, createCallback);
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
  public void postProcess(File targetFile) {
    _createService.postProcess(targetFile);
  }

  @Override
  public void registerProcessor(CreateProcessor processor) {
    _createService.registerProcessor(processor);
  }

}
