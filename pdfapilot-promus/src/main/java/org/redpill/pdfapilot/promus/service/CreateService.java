package org.redpill.pdfapilot.promus.service;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.redpill.pdfapilot.promus.domain.CreatePdfResult;
import org.redpill.pdfapilot.promus.domain.ThreadPoolStatus;
import org.redpill.pdfapilot.promus.service.impl.CreateCallback;
import org.springframework.context.ApplicationEvent;

public interface CreateService {

  static final String EVENT_CREATE_PDF_SUCCESS = "CREATE_PDF_SUCCESS";
  static final String EVENT_CREATE_PDF_FAILURE = "CREATE_PDF_FAILURE";
  static final String EVENT_CREATE_PDFA_SUCCESS = "CREATE_PDFA_SUCCESS";
  static final String EVENT_CREATE_PDFA_FAILURE = "CREATE_PDFA_FAILURE";

  static final String METRIC_CREATE_PDF_SUCCESS = "pdfapilot.promus.createPdf.success";
  static final String METRIC_CREATE_PDF_FAILURE = "pdfapilot.promus.createPdf.failure";
  static final String METRIC_CREATE_PDFA_SUCCESS = "pdfapilot.promus.createPdfa.success";
  static final String METRIC_CREATE_PDFA_FAILURE = "pdfapilot.promus.createPdfa.failure";

  void createPdf(InputStream inputStream, String filename, Map<String, Object> properties, CreateCallback createCallback);

  void createPdfa(InputStream inputStream, String filename, Map<String, Object> properties, String level, CreateCallback createCallback);

  /**
   * Pre-processes the source file.
   * 
   * @param sourceFile
   */
  void preProcess(File sourceFile);

  /**
   * Post-processes the target file.
   * 
   * @param targetFile
   */
  void postProcess(File sourceFile, File targetFile, Long duration, Boolean pdfa);

  void errorProcess(File sourceFile, Boolean pdfa, Throwable ex);

  void registerProcessor(CreateProcessor processor);

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

  public class CreatePdfEvent extends ApplicationEvent {

    private static final long serialVersionUID = 4301589965555125153L;

    private CreatePdfResult _createPdfResult;

    public CreatePdfEvent(Object source, CreatePdfResult createPdfResult) {
      super(source);
      _createPdfResult = createPdfResult;
    }

    public CreatePdfResult getCreatePdfResult() {
      return _createPdfResult;
    }

  }

}
