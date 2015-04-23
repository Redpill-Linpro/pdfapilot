package org.redpill.pdfapilot.promus.domain;

public class ThreadPoolStatus {

  private int _activeCount;
  private int _corePoolSize;
  private int _largestPoolSize;
  private int _maximumPoolSize;
  private int _poolSize;
  private long _taskCount;

  public int getActiveCount() {
    return _activeCount;
  }

  public void setActiveCount(int activeCount) {
    _activeCount = activeCount;
  }

  public int getCorePoolSize() {
    return _corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    _corePoolSize = corePoolSize;
  }

  public int getLargestPoolSize() {
    return _largestPoolSize;
  }

  public void setLargestPoolSize(int largestPoolSize) {
    _largestPoolSize = largestPoolSize;
  }

  public int getMaximumPoolSize() {
    return _maximumPoolSize;
  }

  public void setMaximumPoolSize(int maximumPoolSize) {
    _maximumPoolSize = maximumPoolSize;
  }

  public int getPoolSize() {
    return _poolSize;
  }

  public void setPoolSize(int poolSize) {
    _poolSize = poolSize;
  }

  public long getTaskCount() {
    return _taskCount;
  }

  public void setTaskCount(long taskCount) {
    _taskCount = taskCount;
  }

}
