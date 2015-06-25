package org.redpill.pdfapilot.promus.domain;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Status {

  private Long _time;

  private List<String> _status = new ArrayList<String>();

  private LocalDate _expirationDate;

  public Long getTime() {
    return _time;
  }

  public void setTime(Long total) {
    _time = total;
  }

  public List<String> getStatus() {
    return _status;
  }

  public void setStatus(List<String> status) {
    _status = status;

    parseStatus();
  }

  public LocalDate getExpirationDate() {
    return _expirationDate;
  }

  private void parseStatus() {
    try {
      String expirationDateString = null;

      for (String status : _status) {
        if (status.startsWith("Expiration Date")) {
          expirationDateString = StringUtils.replace(status, "Expiration Date", "").trim();
          break;
        }
      }

      DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

      _expirationDate = LocalDate.parse(expirationDateString, fmt);
    } catch (Exception ex) {
      _expirationDate = null;
    }
  }

  public boolean isExpired() {
    if (_expirationDate == null) {
      return true;
    }
    
    LocalDate today = LocalDate.now();

    Period period = Period.between(today, _expirationDate);

    return period.getDays() < 0;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
