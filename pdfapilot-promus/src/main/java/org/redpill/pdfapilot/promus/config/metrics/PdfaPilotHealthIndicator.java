package org.redpill.pdfapilot.promus.config.metrics;

import javax.annotation.Resource;

import org.redpill.pdfapilot.promus.domain.Status;
import org.redpill.pdfapilot.promus.service.StatusService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PdfaPilotHealthIndicator implements HealthIndicator {
  
  @Resource(name = "pps.statusService")
  private StatusService _statusService;

  @Override
  public Health health() {
    try { 
      Status status = _statusService.getStatus();
      
      if (status.isExpired()) {
        Builder builder = Health.down().withDetail("expired", "pdfaPilot license has expired");
        
        if (status.getExpirationDate() != null) {
          builder.withDetail("expirationDate", status.getExpirationDate());
        }
        
        return builder.build();
      }
      
      return Health.up().withDetail("expirationDate", status.getExpirationDate()).build();
    } catch (Exception ex) {
      return Health.down(ex).build();
    }
  }

}
