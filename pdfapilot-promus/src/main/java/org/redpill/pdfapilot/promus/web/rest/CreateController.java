package org.redpill.pdfapilot.promus.web.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.redpill.pdfapilot.promus.service.AuditEventService;
import org.redpill.pdfapilot.promus.service.CreateService;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;

@RestController
public class CreateController extends AbstractController {

  private static final String RESPONSE_ID_HEADER = "X-CreatePDF-ID";

  @Resource(name = "pps.CreateService")
  private CreateService _createService;

  @Inject
  private AuditEventService _auditEventService;

  @Timed
  @RequestMapping(value = "/create/pdf", method = RequestMethod.POST, produces = "application/pdf")
  public void createPdf(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "filename", required = true) String filename,
      @RequestPart(value = "data", required = false) String data, @RequestParam(value = "file", required = true) MultipartFile file) {
    try {
      Map<String, Object> properties = parseProperties(data);

      _createService.createPdf(file.getInputStream(), filename, properties, (targetFile, id) -> {
        response.setContentLengthLong(targetFile.length());

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", getTargetFilename(filename));
        response.setHeader(headerKey, headerValue);

        response.setHeader(RESPONSE_ID_HEADER, id);

        try {
          OutputStream outputStream = response.getOutputStream();

          FileUtils.copyFile(targetFile, outputStream);
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      });
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Timed
  @RequestMapping(value = "/create/pdfa", method = RequestMethod.POST, produces = "application/pdf")
  public void createPdfa(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "filename", required = true) String filename,
      @RequestPart(value = "data", required = false) String data, @RequestParam(value = "file", required = true) MultipartFile file, @RequestParam(value = "level", required = true) String level) {
    try {
      Map<String, Object> properties = parseProperties(data);

      _createService.createPdfa(file.getInputStream(), filename, properties, level, (targetFile, id) -> {
        response.setContentLengthLong(targetFile.length());

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", getTargetFilename(filename));
        response.setHeader(headerKey, headerValue);

        response.setHeader(RESPONSE_ID_HEADER, id);

        try {
          OutputStream outputStream = response.getOutputStream();

          FileUtils.copyFile(targetFile, outputStream);
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      });
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Timed
  @RequestMapping(value = "/create/audit", method = RequestMethod.POST)
  @ResponseStatus(value = HttpStatus.OK)
  public ResponseEntity<Void> audit(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "id", required = true) String id,
      @RequestParam(value = "verified", required = true) String verified) {
    _auditEventService.auditCreate(id, verified);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  private Map<String, Object> parseProperties(String data) {
    JsonParser parser = new BasicJsonParser();

    return parser.parseMap(data);
  }

  private String getTargetFilename(String sourceFilename) {
    String basename = FilenameUtils.getBaseName(sourceFilename);

    return basename + ".pdf";
  }

}
