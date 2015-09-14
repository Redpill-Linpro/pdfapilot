package org.redpill.alfresco.pdfapilot.verifier;

import java.io.File;

import org.alfresco.service.cmr.repository.NodeRef;
import org.redpill.alfresco.module.metadatawriter.factories.UnsupportedMimetypeException;

public interface MetadataVerifier {

  /**
   * Extracts the title from the document and returns it if found, otherwise
   * returns null. Writes a hash of the nodeRef as a new title.
   * 
   * @param file
   * @param node
   * @param mimetype
   * @return the title or null if no title found or the hash couldn't be
   *         written.
   */
  String changeMetadataTitle(File file, NodeRef node, String basename, String mimetype);

  /**
   * Writes a metadata title to a document that supports it. Throws an exception
   * if title can't be written.
   * 
   * @param file
   * @param node
   * @param mimetype
   * @param title
   * @throws UnsupportedMimetypeException
   */
  void writeMetadataTitle(File file, String basename, String mimetype, String title) throws UnsupportedMimetypeException;

  /**
   * Extracts the metadata title from the file. If no title found it returns an
   * empty string (not null).
   * 
   * @param file
   * @param mimetype
   * @return the title or an empty string
   */
  String extractMetadataTitle(File file, String mimetype);

  boolean verifyMetadata(NodeRef node, File file, String basename, String title) throws UnsupportedMimetypeException;

}
