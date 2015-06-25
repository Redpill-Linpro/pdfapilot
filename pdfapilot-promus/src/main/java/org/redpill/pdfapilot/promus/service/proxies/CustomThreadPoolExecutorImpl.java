package org.redpill.pdfapilot.promus.service.proxies;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.redpill.pdfapilot.promus.domain.ThreadPoolStatus;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public class CustomThreadPoolExecutorImpl extends ThreadPoolExecutor implements ApplicationEventPublisherAware, CustomThreadPoolExecutor {

  private ApplicationEventPublisher _applicationEventPublisher;

  private volatile int _count = 0;

  public CustomThreadPoolExecutorImpl(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    _count++;
    
    super.beforeExecute(t, r);

    ThreadPoolStatus threadPoolStatus = getThreadPoolStatus();
    ThreadPoolStatusEvent event = new ThreadPoolStatusEvent(this, threadPoolStatus);
    _applicationEventPublisher.publishEvent(event);
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    _count--;

    super.afterExecute(r, t);
    
    if (t != null) {
      System.out.println("Perform exception handler logic");
    }

    ThreadPoolStatus threadPoolStatus = getThreadPoolStatus();
    ThreadPoolStatusEvent event = new ThreadPoolStatusEvent(this, threadPoolStatus);
    _applicationEventPublisher.publishEvent(event);
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    _applicationEventPublisher = applicationEventPublisher;
  }

  /* (non-Javadoc)
   * @see org.redpill.pdfapilot.promus.service.proxies.CustomThreadPoolExecutor#getThreadPoolStatus()
   */
  @Override
  public ThreadPoolStatus getThreadPoolStatus() {
    ThreadPoolStatus threadPoolStatus = new ThreadPoolStatus();

    threadPoolStatus.setActiveCount(_count);
    threadPoolStatus.setCorePoolSize(getCorePoolSize());
    threadPoolStatus.setLargestPoolSize(getLargestPoolSize());
    threadPoolStatus.setMaximumPoolSize(getMaximumPoolSize());
    threadPoolStatus.setPoolSize(getPoolSize());
    threadPoolStatus.setTaskCount(getTaskCount());

    return threadPoolStatus;
  }

  public class ThreadPoolStatusEvent extends ApplicationEvent {

    private static final long serialVersionUID = -7496151999960605629L;

    private ThreadPoolStatus _threadPoolStatus;

    public ThreadPoolStatusEvent(Object source, ThreadPoolStatus threadPoolStatus) {
      super(source);
      _threadPoolStatus = threadPoolStatus;
    }

    public ThreadPoolStatus getThreadPoolStatus() {
      return _threadPoolStatus;
    }

  }

}