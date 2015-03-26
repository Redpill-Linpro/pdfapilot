package org.redpill.pdfapilot.server.service.proxies;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.pdfapilot.server.domain.Version;
import org.redpill.pdfapilot.server.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("pps.VersionService")
public class VersionServiceProxy implements VersionService {

  private final Log LOG = LogFactory.getLog(getClass());

  @Resource(name = "pps.versionService")
  private VersionService _versionService;

  @Autowired
  private ThreadPoolExecutor _threadPoolExecutor;

  @Value("${pdfaPilot.taskTimeout}")
  private int _timeout;

  @Override
  public Version getVersion() {
    FutureTask<Version> task = null;
    try {
      task = new FutureTask<Version>(() -> _versionService.getVersion());

      _threadPoolExecutor.execute(task);

      return task.get(_timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      task.cancel(true);

      LOG.warn(e.getMessage(), e);
    } catch (InterruptedException e) {
      // We were asked to stop
      task.cancel(true);

      return null;
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }

    return null;
  }

}
