package org.redpill.pdfapilot.server.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.redpill.pdfapilot.server.service.proxies.CustomThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
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

  /**
   * Constructs a ThreadPoolExecutor with some pre-configured values for use
   * with the pdfaPilot binary.
   * 
   * @return The thread pool executor.
   */
  @Bean
  public ThreadPoolExecutor pdfaPilotThreadPoolExecutor() {
    BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(_queueCapacity);

    ThreadPoolExecutor taskExecutor = new CustomThreadPoolExecutor(_corePoolSize, _maxPoolSize, _keepAliveTime, TimeUnit.MILLISECONDS, blockingQueue);

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
