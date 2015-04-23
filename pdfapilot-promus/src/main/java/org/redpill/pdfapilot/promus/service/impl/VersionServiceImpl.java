package org.redpill.pdfapilot.promus.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.pdfapilot.promus.domain.Version;
import org.redpill.pdfapilot.promus.service.VersionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("pps.versionService")
public class VersionServiceImpl implements VersionService {

  private final Log LOG = LogFactory.getLog(getClass());

  @Value("${pdfaPilot.exe}")
  private String _executable;

  @Override
  public Version getVersion() {
    try {
      LOG.debug("Executing: " + _executable + " --version");

      Long start = System.currentTimeMillis();

      String[] command = { _executable, "--version" };
      ExecCommand exec = new ExecCommand(command);

      LOG.debug("Std err: " + exec.getError());

      Long total = System.currentTimeMillis() - start;

      if (exec.getExitValue() >= 100) {
        LOG.debug(exec.getOutput());
      }

      List<String> inputs = new ArrayList<String>();

      for (String line : exec.getOutput()) {
        inputs.add(line.trim());
      }

      Version version = new Version();
      version.setTime(total);
      version.setVersion(StringUtils.join(inputs, ", "));

      return version;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

}
