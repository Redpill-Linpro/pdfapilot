package org.redpill.pdfapilot.server.web.rest;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This API is exposed both as form based login (/api...) & basic auth
 * (/bapi...).
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
@RequestMapping({ "/api/v1", "/bapi/v1" })
public abstract class AbstractController {

}
