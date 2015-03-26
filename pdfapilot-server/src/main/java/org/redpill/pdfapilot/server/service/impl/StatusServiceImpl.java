package org.redpill.pdfapilot.server.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.pdfapilot.server.domain.Status;
import org.redpill.pdfapilot.server.service.StatusService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("pps.statusService")
public class StatusServiceImpl implements StatusService {

  private final Log LOG = LogFactory.getLog(getClass());

  @Value("${pdfaPilot.exe}")
  private String _executable;

  public Status getStatus() {
    try {
      LOG.debug("Executing: " + _executable + " --status");
      
      Long start = System.currentTimeMillis();
      
      String[] command = { _executable, "--status" };
      Process process = Runtime.getRuntime().exec(command);
      process.waitFor();
      
      Long total = System.currentTimeMillis() - start;
      
      List<String> lines = Arrays.asList(IOUtils.toString(process.getInputStream()).split("\n"));
      
      List<String> inputs = new ArrayList<String>();
      
      for (String line : lines) {
        if (!line.startsWith("Serialization")) {
          continue;
        }
        
        line = StringUtils.replace(line, "Serialization:", "").trim();
        line = StringUtils.replace(line, "Serialization", "").trim();
        line = StringUtils.replace(line, "\t", " ").trim();
        
        if (StringUtils.isBlank(line)) {
          continue;
        }
        
        inputs.add(line);
      }

      Status status = new Status();

      status.setTime(total);
      status.setStatus(inputs);

      return status;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

}
