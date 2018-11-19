package uk.gov.hmcts.probate.services.submit.services;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.info.git.location=classpath:uk/gov/hmcts/probate/services/submit/git-test.properties"})
public class GitCommitInfoEndpointTest {

  private static final String EXPECTED_COMMIT_ID_INFO_RESPONSE = "0773f12";
  private static final String EXPECTED_COMMIT_TIME_INFO_RESPONSE = "2018-05-23T13:59+1234";

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SubmitService submitService;

  @Test
  public void shouldGetGitCommitInfoEndpoint() throws Exception {
    mockMvc.perform(get("/info"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.git.commit.id").value(EXPECTED_COMMIT_ID_INFO_RESPONSE))
        .andExpect(jsonPath("$.git.commit.time").value(EXPECTED_COMMIT_TIME_INFO_RESPONSE));
  }
}
