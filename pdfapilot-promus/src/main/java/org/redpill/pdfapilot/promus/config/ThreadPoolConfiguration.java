package org.redpill.pdfapilot.promus.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.redpill.pdfapilot.promus.service.proxies.CustomThreadPoolExecutorImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main application entry point for this Spring Boot application.
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
@Configuration
public class ThreadPoolConfiguration {

  @Value("${pdfaPilot.corePoolSize}")
  private int _corePoolSize;

  @Value("${pdfaPilot.maxPoolSize}")
  private int _maxPoolSize;

  @Value("${pdfaPilot.keepAliveTime}")
  private int _keepAliveTime;

  @Value("${pdfaPilot.queueCapacity}")
  private int _queueCapacity;

  @Inject
  private ApplicationEventPublisher _applicationEventPublisher;

  /**
   * Constructs a ThreadPoolExecutor with some pre-configured values for use
   * with the pdfaPilot binary.
   * 
   * @return The thread pool executor.
   */
  @Bean(name = "ppc.threadPoolExecutor")
  public Executor pdfaPilotThreadPoolExecutor() {
    BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(_queueCapacity);

    CustomThreadPoolExecutorImpl taskExecutor = new CustomThreadPoolExecutorImpl(_corePoolSize, _maxPoolSize, _keepAliveTime, TimeUnit.MILLISECONDS, blockingQueue);
    taskExecutor.setApplicationEventPublisher(_applicationEventPublisher);

    taskExecutor.setRejectedExecutionHandler((r, executor) -> {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      executor.execute(r);
    });

    return taskExecutor;
  }

}
