package org.redpill.pdfapilot.server.domain;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An authority (a security role) used by Spring Security.
 */
@Document(collection = "T_AUTHORITY")
public class Authority implements Serializable {

  private static final long serialVersionUID = -6728495323557326557L;
  
  @NotNull
  @Size(min = 0, max = 50)
  @Id
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Authority authority = (Authority) o;

    if (name != null ? !name.equals(authority.name) : authority.name != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Authority{" + "name='" + name + '\'' + "}";
  }
}
