package org.redpill.alfresco.pdfapilot.it;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.util.TempFileProvider;
import org.junit.Test;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotTransformationOptions;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PdfaPilotWorkerIntegrationTest extends AbstractRepoIntegrationTest {

  private static final String DEFAULT_USERNAME = "testuser_" + System.currentTimeMillis();

  private static SiteInfo _site;

  @Autowired
  @Qualifier("ppc.pdfaPilotWorker")
  private ContentTransformerWorker _worker;

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
  public void testOdt() throws Exception {
    testDocument("test.odt");
  }

  @Test
  public void testDoc() throws Exception {
    testDocument("test.doc");
  }

  @Test
  public void testDocx() throws Exception {
    testDocument("test.docx");
  }

  @Test
  public void testPdf() throws Exception {
    testDocument("test.pdf");
  }

  @Test
  public void testPdf2() throws Exception {
    testDocument("test2.pdf");
  }

  @Test
  public void testXlsm() throws Exception {
    testDocument("test.xlsm");
  }

  @Test
  public void testXls() throws Exception {
    testDocument("test.xls");
  }

  @Test
  public void testXlsx() throws Exception {
    testDocument("test.xlsx");
  }

  @Test
  public void testDoc2() throws Exception {
    testDocument("test2.doc");
  }

  @Test
  public void testTwoCharacterFilename() throws Exception {
    testDocument("fy.doc");
  }

  @Test
  public void testPptx() throws Exception {
    testDocument("test.pptx");
  }

  @Test
  public void testPptx2() throws Exception {
    testDocument("test2.pptx");
  }

  public void testDocument(String filename) throws InterruptedException {
    List<Thread> threads = new ArrayList<Thread>();

    final NodeRef document = uploadDocument(_site, filename).getNodeRef();

    for (int x = 0; x < 1; x++) {
      Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
          createDocument(document);
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
        break;
      }

      Thread.sleep(100);
    }
    
    for (Thread thread : threads) {
      if (thread.getStackTrace() != null && thread.getStackTrace().length > 0) {
        fail();
      }
    }
  }

  public void createDocument(NodeRef document) {
    AuthenticationUtil.setFullyAuthenticatedUser("System");

    ContentReader contentReader = _contentService.getReader(document, ContentModel.PROP_CONTENT);
    assertTrue(contentReader.exists());

    File file = TempFileProvider.createTempFile("temp_", ".pdf");
    ContentWriter contentWriter = new FileContentWriter(file);
    contentWriter.setMimetype("application/pdf");

    PdfaPilotTransformationOptions options = new PdfaPilotTransformationOptions();
    options.setLevel("2b");
    options.setOptimize(false);
    options.setSourceNodeRef(document);

    try {
      _worker.transform(contentReader, contentWriter, options);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    System.out.println(file.getAbsolutePath());
  }

}
