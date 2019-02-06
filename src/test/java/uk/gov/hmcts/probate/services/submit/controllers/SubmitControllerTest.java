package uk.gov.hmcts.probate.services.submit.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.probate.services.submit.model.ParsingSubmitException;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;
import uk.gov.hmcts.probate.services.submit.services.SubmitService;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = {SubmitController.class}, secure = false)
public class SubmitControllerTest {

    private static final String SUBMIT_SERVICE_URL = "/submit";
    private static final String RESUBMIT_SERVICE_URL = "/resubmit";
    private static final String UPDATE_PAYMENT_STATUS_URL = "/updatePaymentStatus";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubmitService mockSubmitService;

    private HttpHeaders httpHeaders;

    private String userId;
    private String authorizationToken;

    @Before
    public void setup() {
        userId = "123";
        authorizationToken = "dummyToken";
        httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", authorizationToken);
        httpHeaders.add("UserId", userId);
    }

    @Test
    public void shouldSubmitSuccessfully() throws Exception {
        SubmitData validApplication = new SubmitData(TestUtils.getJsonNodeFromFile("formPayload.json"));
        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");

        when(mockSubmitService.submit(eq(validApplication), eq(userId), eq(authorizationToken))).thenReturn(registryData);

        mockMvc.perform(post(SUBMIT_SERVICE_URL)
                .headers(httpHeaders)
                .content(validApplication.getJson().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(registryData.toString()));
    }

    @Test
    public void shouldResubmitSuccessfully() throws Exception {
        when(mockSubmitService.resubmit(eq(Long.parseLong("123456789")))).thenReturn("1111111111");

        mockMvc.perform(get(RESUBMIT_SERVICE_URL + "/123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string("1111111111"));
    }


    @Test
    public void shouldReturn400OnSubmitOfInvalidJson() throws Exception {
        String invalidJson = "invalid json";

        mockMvc.perform(post(SUBMIT_SERVICE_URL)
                .content(invalidJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn400OnSubmitOfInvalidForDataJson() throws Exception {
        String invalidJson = "invalid json";

        mockMvc.perform(post(SUBMIT_SERVICE_URL)
                .content(invalidJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn502WhenMailExceptionThrownOnSubmit() throws Exception {
        SubmitData validApplication = new SubmitData(TestUtils.getJsonNodeFromFile("formPayload.json"));
        doThrow(MailSendException.class).when(mockSubmitService).submit(eq(validApplication), eq(userId), eq(authorizationToken));

        mockMvc.perform(post(SUBMIT_SERVICE_URL)
                .headers(httpHeaders)
                .content(validApplication.getJson().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(status().reason("Could not send the probate email"));
    }

    @Test
    public void shouldReturn422StatusWhenParsingSubmitExceptionThrownOnSubmit() throws Exception {
        SubmitData validApplication = new SubmitData(TestUtils.getJsonNodeFromFile("formPayload.json"));
        doThrow(ParsingSubmitException.class).when(mockSubmitService).submit(eq(validApplication), eq(userId), eq(authorizationToken));

        mockMvc.perform(post(SUBMIT_SERVICE_URL)
                .headers(httpHeaders)
                .content(validApplication.getJson().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(status().reason("Error creating email payload"));
    }

    @Test
    public void shouldUpdatePaymentStatusSuccessfully() throws Exception {
        SubmitData validApplication = new SubmitData(TestUtils.getJsonNodeFromFile("formPayload.json"));
        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");

        when(mockSubmitService.updatePaymentStatus(eq(validApplication), eq(userId), eq(authorizationToken)))
                .thenReturn(registryData);

        mockMvc.perform(post(UPDATE_PAYMENT_STATUS_URL)
                .headers(httpHeaders)
                .content(validApplication.getJson().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(registryData.toString()));
    }

    @Test
    public void shouldReturn502WhenHttpClientErrorExceptionThrownOnSubmit() throws Exception {
        SubmitData validApplication = new SubmitData(TestUtils.getJsonNodeFromFile("formPayload.json"));
        doThrow(HttpClientErrorException.class).when(mockSubmitService).submit(eq(validApplication), eq(userId), eq(authorizationToken));

        mockMvc.perform(post(SUBMIT_SERVICE_URL)
                .headers(httpHeaders)
                .content(validApplication.getJson().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(status().reason("Could not persist submitted application"));
    }
}
