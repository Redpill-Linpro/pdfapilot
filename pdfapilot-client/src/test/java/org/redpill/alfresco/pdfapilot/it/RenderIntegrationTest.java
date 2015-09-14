package org.redpill.alfresco.pdfapilot.it;

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.rendition.executer.AbstractTransformationRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.junit.Test;
import org.redpill.alfresco.pdfapilot.model.PdfaPilotClientModel;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotRenderingEngine;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotTransformationOptions;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class RenderIntegrationTest extends AbstractRepoIntegrationTest {

  @Autowired
  @Qualifier("RenditionService")
  private RenditionService _renditionService;

  @Autowired
  @Qualifier("ThumbnailService")
  private ThumbnailService _thumbnailService;

  private static final String DEFAULT_USERNAME = "testuser_" + System.currentTimeMillis();

  private static SiteInfo _site;

  @Override
  public void beforeClassSetup() {
    super.beforeClassSetup();

    createUser(DEFAULT_USERNAME);

    _authenticationComponent.setCurrentUser(DEFAULT_USERNAME);

    _site = createSite();
  }

  @Override
  public void afterClassSetup() {
    super.afterClassSetup();

    deleteSite(_site);

    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());

    deleteUser(DEFAULT_USERNAME);

    _authenticationComponent.clearCurrentSecurityContext();
  }

  @Test
  public void testDoc() throws InterruptedException {
    testFile("test.doc");
  }

  @Test
  public void testDocx() throws InterruptedException {
    testFile("test.docx");
  }

  @Test
  public void testXls() throws InterruptedException {
    testFile("test.xls");
  }

  @Test
  public void testXlsx() throws InterruptedException {
    testFile("test.xlsx");
  }

  @Test
  public void testPpt() throws InterruptedException {
    testFile("test.ppt");
  }

  @Test
  public void testPptx() throws InterruptedException {
    testFile("test.pptx");
  }

  @Test
  public void testRtf() throws InterruptedException {
    testFile("test.rtf");
  }

  @Test
  public void testOdt() throws InterruptedException {
    testFile("test.odt");
  }

  @Test
  public void testOdp() throws InterruptedException {
    testFile("test.odp");
  }

  @Test
  public void testOds() throws InterruptedException {
    testFile("test.ods");
  }

  public void testFile(String filename) throws InterruptedException {
    List<Thread> threads = new ArrayList<Thread>();

    final NodeRef document = uploadDocument(_site, filename).getNodeRef();

    for (int x = 0; x < 1; x++) {
      Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
          createRendition(document);
        }

      });

      thread.setName("Thread_" + System.currentTimeMillis());
      thread.start();

      threads.add(thread);
    }

    while (true) {
      int count = threads.size();

      for (Thread thread : threads) {
        if (!thread.isAlive()) {
          count--;
        }
      }

      if (count == 0) {
        return;
      }

      Thread.sleep(100);
    }
  }

  private void createRendition(final NodeRef document) {
    AuthenticationUtil.setFullyAuthenticatedUser("System");

    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        RenditionDefinition renditionDefinition = createRenditionDefinition();
        ChildAssociationRef rendition = _renditionService.render(document, renditionDefinition);

        assertNotNull(rendition);

        ThumbnailRegistry registry = _thumbnailService.getThumbnailRegistry();
        ThumbnailDefinition details = registry.getThumbnailDefinition("doclib");

        // Create the thumbnail
        _thumbnailService.createThumbnail(document, ContentModel.PROP_CONTENT, details.getMimetype(), details.getTransformationOptions(), details.getName());

        NodeRef thumbnail = _thumbnailService.getThumbnailByName(document, ContentModel.PROP_CONTENT, "doclib");

        assertNotNull(thumbnail);

        return null;
      }
    }, false, true);

  }

  private RenditionDefinition createRenditionDefinition() {
    RenditionDefinition definition = _renditionService.createRenditionDefinition(PdfaPilotClientModel.RD_PDFA, PdfaPilotRenderingEngine.NAME);

    definition.setTrackStatus(true);

    Map<String, Serializable> parameters = new HashMap<String, Serializable>();

    parameters.put(RenditionService.PARAM_RENDITION_NODETYPE, ContentModel.TYPE_CONTENT);

    parameters.put(AbstractRenderingEngine.PARAM_SOURCE_CONTENT_PROPERTY, ContentModel.PROP_CONTENT);
    parameters.put(AbstractRenderingEngine.PARAM_MIME_TYPE, "application/pdf");

    parameters.put(PdfaPilotRenderingEngine.PARAM_LEVEL, PdfaPilotTransformationOptions.PDFA_LEVEL_2B);
    parameters.put(PdfaPilotRenderingEngine.PARAM_OPTIMIZE, false);
    parameters.put(PdfaPilotRenderingEngine.PARAM_FAIL_SILENTLY, false);

    parameters.put(AbstractTransformationRenderingEngine.PARAM_TIMEOUT_MS, 300000L);
    parameters.put(AbstractTransformationRenderingEngine.PARAM_READ_LIMIT_TIME_MS, -1L);
    parameters.put(AbstractTransformationRenderingEngine.PARAM_MAX_SOURCE_SIZE_K_BYTES, -1L);
    parameters.put(AbstractTransformationRenderingEngine.PARAM_READ_LIMIT_K_BYTES, -1L);
    parameters.put(AbstractTransformationRenderingEngine.PARAM_MAX_PAGES, -1);
    parameters.put(AbstractTransformationRenderingEngine.PARAM_PAGE_LIMIT, -1);

    definition.addParameterValues(parameters);

    return definition;
  }

}
