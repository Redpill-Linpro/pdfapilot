package org.redpill.pdfapilot.server.web.rest;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.redpill.pdfapilot.server.service.CreateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class CreateController extends AbstractController {

  @Resource(name = "pps.CreateService")
  private CreateService _createService;

  @RequestMapping(value = "/create/pdf", method = RequestMethod.POST, produces = "application/pdf")
  public void createPdf(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "filename", required = true) String filename,
      @RequestParam(value = "file", required = true) MultipartFile file) {
    try {
      _createService.createPdf(file.getInputStream(), filename, (targetFile) -> {
        response.setContentLengthLong(targetFile.length());

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", getTargetFilename(filename));
        response.setHeader(headerKey, headerValue);

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

  @RequestMapping(value = "/create/pdfa", method = RequestMethod.POST, produces = "application/pdf")
  public void createPdfa(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "filename", required = true) String filename,
      @RequestParam(value = "file", required = true) MultipartFile file, @RequestParam(value = "level", required = true) String level) {
    try {
      _createService.createPdfa(file.getInputStream(), filename, level, (targetFile) -> {
        response.setContentLengthLong(targetFile.length());

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", getTargetFilename(filename));
        response.setHeader(headerKey, headerValue);

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

  private String getTargetFilename(String sourceFilename) {
    String basename = FilenameUtils.getBaseName(sourceFilename);

    return basename + ".pdf";
  }

}
