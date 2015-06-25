package org.redpill.pdfapilot.promus.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.pdfapilot.promus.domain.Status;
import org.redpill.pdfapilot.promus.service.StatusService;
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

      List<String> commandList = new ArrayList<String>();

      commandList.add(_executable);
      commandList.add("--status");

      String[] command = commandList.toArray(new String[commandList.size()]);

      LOG.debug("Executing: " + StringUtils.join(command, " "));

      ExecCommand exec = new ExecCommand(command);

      LOG.debug("Std err: " + exec.getError());

      Long total = System.currentTimeMillis() - start;

      if (exec.getExitValue() >= 100) {
        LOG.debug(exec.getOutput());
      }

      List<String> inputs = new ArrayList<String>();

      for (String line : exec.getOutput()) {
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
