package org.redpill.alfresco.pdfapilot.verifier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Properties;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MetadataVerifierTest {

  @Mock
  Properties globalProperties;

  @InjectMocks
  MetadataVerifierImpl mv;

  @Test
  public void testNotAllowedMetadataExtractionDefault() {
    File file = mock(File.class);
    NodeRef node = new NodeRef("workspace://SpacesStore/mockNode");

    when(globalProperties.getProperty(MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_DEFAULT)).thenReturn("102000");
    when(file.length()).thenReturn(200000L*1024);
    boolean result = mv.allowMetadataOperations(file, node);
    assertFalse("Do not allow when file is above default limit",result);
  }

  @Test
  public void testAllowedMetadataExtractionDefault() {
    File file = mock(File.class);
    NodeRef node = new NodeRef("workspace://SpacesStore/mockNode");

    when(globalProperties.getProperty(MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_DEFAULT)).thenReturn("102000");
    when(file.length()).thenReturn(100000L*1024);
    boolean result = mv.allowMetadataOperations(file, node);
    assertTrue("Allow when file is below default limit", result);
  }
  
  @Test
  public void testNotAllowedMetadataExtractionFileExt() {
    File file = mock(File.class);
    NodeRef node = new NodeRef("workspace://SpacesStore/mockNode");

    when(globalProperties.getProperty(MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_DEFAULT)).thenReturn("102000");
    when(globalProperties.getProperty(MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_PREFIX + "xlsx" + MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_SUFFIX)).thenReturn("10200");
    when(file.length()).thenReturn(20000L*1024);
    when(file.getName()).thenReturn("test.xlsx");
    boolean result = mv.allowMetadataOperations(file, node);
    assertFalse("Do not allow when xlsx file is between default limit and file type limit", result);
  }

  @Test
  public void testAllowedMetadataExtractionFileExt() {
    File file = mock(File.class);
    NodeRef node = new NodeRef("workspace://SpacesStore/mockNode");

    when(globalProperties.getProperty(MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_DEFAULT)).thenReturn("102000");
    when(globalProperties.getProperty(MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_PREFIX + "xlsx" + MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_SUFFIX)).thenReturn("10200");
    when(file.length()).thenReturn(10000L*1024);
    when(file.getName()).thenReturn("test.xlsx");
    boolean result = mv.allowMetadataOperations(file, node);
    assertTrue("Allow when xlsx file is below both default limit and file type limit", result);
  }
  
  @Test
  public void testNotAllowedMetadataExtractionFileExt2() {
    File file = mock(File.class);
    NodeRef node = new NodeRef("workspace://SpacesStore/mockNode");

    when(globalProperties.getProperty(MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_DEFAULT)).thenReturn("102000");
    when(globalProperties.getProperty(MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_PREFIX + "xlsx" + MetadataVerifierImpl.SIZE_LIMIT_PROPERTY_SUFFIX)).thenReturn("10200");
    when(file.length()).thenReturn(1000000L*1024);
    when(file.getName()).thenReturn("test.xlsx");
    boolean result = mv.allowMetadataOperations(file, node);
    assertFalse("Do not allow when xlsx file is above both default limit and file type limit", result);
  }

}
