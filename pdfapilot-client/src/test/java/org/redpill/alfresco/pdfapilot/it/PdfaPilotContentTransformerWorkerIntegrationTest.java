package org.redpill.alfresco.pdfapilot.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redpill.alfresco.module.metadatawriter.factories.MetadataContentFactory;
import org.redpill.alfresco.module.metadatawriter.factories.UnsupportedMimetypeException;
import org.redpill.alfresco.module.metadatawriter.services.ContentFacade;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotContentTransformerWorker;
import org.redpill.alfresco.pdfapilot.worker.PdfaPilotTransformationOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

@Ignore
@ContextConfiguration(locations = { "classpath*:alfresco/subsystems/pdfaPilot/default/pdfapilot-context.xml", "classpath:test-pdfa-pilot-convert-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class PdfaPilotContentTransformerWorkerIntegrationTest {

  @Autowired
  private ApplicationContext _applicationContext;

  private MimetypeService _mimetypeService;

  private PdfaPilotContentTransformerWorker _contentTransformerWorker;
  
  private MetadataContentFactory _metadataContentFactory;

  @Before
  public void setUp() {
    _contentTransformerWorker = (PdfaPilotContentTransformerWorker) _applicationContext.getBean("transformer.worker.PdfaPilot");

    _mimetypeService = mock(MimetypeService.class);
    _contentTransformerWorker.setMimetypeService(_mimetypeService);
    
    _metadataContentFactory = mock(MetadataContentFactory.class);
    _contentTransformerWorker.setMetadataContentFactory(_metadataContentFactory);
  }

  @Test
  public void testIsAvailable() {
    assertTrue(_contentTransformerWorker.isAvailable());
  }

  @Test
  public void testGetVersionString() {
    String versionString = _contentTransformerWorker.getVersionString();

    assertTrue(StringUtils.hasText(versionString));
  }

  @Test
  public void testIsDocTransformable() {
    when(_mimetypeService.getExtension("application/msword")).thenReturn("doc");
    when(_mimetypeService.getExtension("application/pdf")).thenReturn("pdf");

    boolean isTransformable = _contentTransformerWorker.isTransformable("application/msword", "application/pdf", null);

    assertTrue(isTransformable);
  }

  @Test
  public void testIsPdfTransformable() {
    when(_mimetypeService.getExtension("application/pdf")).thenReturn("pdf");
    when(_mimetypeService.getExtension("application/pdf")).thenReturn("pdf");

    boolean isTransformable = _contentTransformerWorker.isTransformable("application/pdf", "application/pdf", null);

    assertTrue(isTransformable);
  }

  @Test
  public void testIsFooTransformable() {
    when(_mimetypeService.getExtension("application/foobar")).thenReturn("foo");
    when(_mimetypeService.getExtension("application/pdf")).thenReturn("pdf");

    boolean isTransformable = _contentTransformerWorker.isTransformable("application/foobar", "application/pdf", null);

    assertFalse(isTransformable);
  }

  @Test
  public void transformDoc() throws IOException, UnsupportedMimetypeException {
    transformFile("/test.doc", MimetypeMap.MIMETYPE_WORD, 6);
  }

  @Test
  public void transformDocWithAmpersandName() throws IOException, UnsupportedMimetypeException {
    transformFile("/test&tips.doc", MimetypeMap.MIMETYPE_WORD, 6);
  }

  @Test
  public void transformPdf() throws IOException, UnsupportedMimetypeException {
    transformFile("/test.pdf", MimetypeMap.MIMETYPE_PDF, 5);
  }

  @Test
  public void transformXlsm() throws IOException, UnsupportedMimetypeException {
    transformFile("/test.xlsm", MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_MACRO, 1);
  }

  protected void transformFile(String filename, String sourceMimetype, int expectedPageCount) throws IOException, UnsupportedMimetypeException {
    ContentFacade contentFacade = mock(ContentFacade.class);
    
    when(_mimetypeService.getExtension(sourceMimetype)).thenReturn(FilenameUtils.getExtension(filename));
    when(_mimetypeService.getExtension("application/pdf")).thenReturn("pdf");
    when(_metadataContentFactory.createContent(any(InputStream.class), any(OutputStream.class), eq(sourceMimetype))).thenReturn(contentFacade);

    File sourceFile = TempFileProvider.createTempFile("test_", FilenameUtils.getName(filename));
    File targetFile = TempFileProvider.createTempFile("test_", "test.pdf");
    NodeRef sourceNodeRef = new NodeRef("workspace://SpacesStore/this_is_a_node_ref");

    InputStream inputStream = this.getClass().getResourceAsStream(filename);

    OutputStream outputStream = new FileOutputStream(sourceFile);

    FileCopyUtils.copy(inputStream, outputStream);

    ContentReader reader = new FileContentReader(sourceFile);
    reader.setMimetype(sourceMimetype);

    FileContentWriter writer = new FileContentWriter(targetFile);
    writer.setMimetype(MimetypeMap.MIMETYPE_PDF);

    PdfaPilotTransformationOptions options = new PdfaPilotTransformationOptions();
    options.setOptimize(false);
    options.setLevel(PdfaPilotTransformationOptions.PDFA_LEVEL_2B);
    options.setSourceNodeRef(sourceNodeRef);

    try {
      _contentTransformerWorker.transform(reader, writer, options);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertPageCount(expectedPageCount, writer.getFile());

    FileUtils.deleteQuietly(sourceFile);
    FileUtils.deleteQuietly(targetFile);
  }

  private void assertPageCount(int expectedPageCount, File file) throws IOException {
    PDDocument document = PDDocument.load(file);

    try {
      assertEquals(expectedPageCount, document.getNumberOfPages());
    } finally {
      document.close();
    }
  }

  @Test
  public void testNotEnabled() {
    _contentTransformerWorker.setEnabled(false);

    assertFalse(_contentTransformerWorker.isTransformable(null, null, null));
  }

  @Test
  public void testNoTargetFormat() {
    when(_mimetypeService.getExtension("application/msword")).thenReturn("doc");
    when(_mimetypeService.getExtension("application/pdf")).thenReturn("");

    assertFalse(_contentTransformerWorker.isTransformable("application/msword", "application/pdf", null));
  }

  @Test
  public void testNullTargetFormat() throws IOException {
    when(_mimetypeService.getExtension("application/msword")).thenReturn(null);
    when(_mimetypeService.getExtension("application/pdf")).thenReturn("pdf");

    File sourceFile = new File("/tmp/test.doc");
    File targetFile = new File("/tmp/test.pdf");

    InputStream inputStream = this.getClass().getResourceAsStream("/test.doc");

    OutputStream outputStream = new FileOutputStream(sourceFile);

    FileCopyUtils.copy(inputStream, outputStream);

    ContentReader reader = new FileContentReader(sourceFile);
    reader.setMimetype(MimetypeMap.MIMETYPE_WORD);

    FileContentWriter writer = new FileContentWriter(targetFile);
    writer.setMimetype(MimetypeMap.MIMETYPE_PDF);

    TransformationOptions options = mock(TransformationOptions.class);

    try {
      _contentTransformerWorker.transform(reader, writer, options);
      fail();
    } catch (Exception e) {
      // this is correct...
    }

    FileUtils.deleteQuietly(sourceFile);
    FileUtils.deleteQuietly(targetFile);
  }

}
