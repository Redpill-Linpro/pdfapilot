package org.redpill.pdfapilot.promus.service.proxies;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("pps.poolingInterceptor")
public class PoolingInterceptor implements MethodInterceptor {

  @Resource(name = "ppc.threadPoolExecutor")
  private Executor _threadPoolExecutor;

  @Value("${pdfaPilot.taskTimeout}")
  private int _timeout;

  protected String[] _methods;

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    // first check if invocation should be pooled or not
    if (_methods != null && !ArrayUtils.contains(_methods, invocation.getMethod().getName())) {
      return invocation.proceed();
    }
    
    FutureTask<Object> task = null;
    try {
      task = new FutureTask<>(() -> {
        try {
          return invocation.proceed();
        } catch (Throwable ex) {
          throw new RuntimeException(ex);
        }
      });

      _threadPoolExecutor.execute(task);

      return task.get(_timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      task.cancel(true);
    } catch (InterruptedException e) {
      // We were asked to stop
      task.cancel(true);

      return null;
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }

    return null;

  }

  public void setMethods(String[] methods) {
    _methods = methods;
  }

}
