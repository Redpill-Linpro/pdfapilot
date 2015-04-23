package org.redpill.pdfapilot.promus.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.core.StringContains.containsString;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.redpill.pdfapilot.promus.service.VersionService;
import org.redpill.pdfapilot.promus.web.rest.VersionController;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class VersionControllerTest extends AbstractControllerTest {

  private MockMvc _restVersionMock;

  @Resource(name = "pps.VersionService")
  private VersionService _versionService;

  @Before
  public void setup() {
    VersionController versionController = new VersionController();
    ReflectionTestUtils.setField(versionController, "_versionService", _versionService);
    _restVersionMock = MockMvcBuilders.standaloneSetup(versionController).build();
  }

  @Test
  public void testGetExistingUser() throws Exception {
    _restVersionMock
        .perform(get("/bapi/v1/version")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.version").value(containsString("callas pdfaPilot CLI")));
  }

}
