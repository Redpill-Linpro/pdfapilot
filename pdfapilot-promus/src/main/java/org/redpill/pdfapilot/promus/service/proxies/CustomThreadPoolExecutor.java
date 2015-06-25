package org.redpill.pdfapilot.promus.service.proxies;

import org.redpill.pdfapilot.promus.domain.ThreadPoolStatus;

public interface CustomThreadPoolExecutor {

  ThreadPoolStatus getThreadPoolStatus();

}