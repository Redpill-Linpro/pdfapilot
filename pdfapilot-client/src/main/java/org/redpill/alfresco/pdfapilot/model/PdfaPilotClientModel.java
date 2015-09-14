package org.redpill.alfresco.pdfapilot.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public interface PdfaPilotClientModel {

  public static final String PPC_CORE_URI = "http://www.redpill-linpro.se/pdfapilotclient/model/core/1.0";

  public static final QName RD_PDFA = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "pdfa");

}
