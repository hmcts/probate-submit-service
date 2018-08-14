package uk.gov.hmcts.probate.services.submit.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;
import uk.gov.hmcts.probate.services.submit.model.ParsingSubmitException;
import uk.gov.hmcts.probate.services.submit.services.SubmitService;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpHeaders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class SubmitControllerTest {


    private static final String SUBMIT_SERVICE_URL = "/submit";
    private static final String RESUBMIT_SERVICE_URL = "/resubmit";

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    @Mock
    private SubmitService mockSubmitService;

    @InjectMocks
    private SubmitController submitController;
    
    private HttpHeaders httpHeaders;
   
    String userId;
    String authorizationToken ;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(submitController).setHandlerExceptionResolvers(new DefaultErrorAttributes(), createExceptionResolver())
                .build();
        userId = "123";
        authorizationToken = "dummyToken";
        httpHeaders = new HttpHeaders();          
        httpHeaders.add("Authorization", authorizationToken);
        httpHeaders.add("UserId", userId);
    }

    private ExceptionHandlerExceptionResolver createExceptionResolver() {

        return new ExceptionHandlerExceptionResolver() {
            protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
                Method method = new ExceptionHandlerMethodResolver(GenericExceptionHandler.class).resolveMethod(exception);
                return new ServletInvocableHandlerMethod(new GenericExceptionHandler(), method);
            }
        };
    }

    @Test
    public void submitSuccessfully() throws Exception {
        JsonNode validApplication = TestUtils.getJsonNodeFromFile("formPayload.json");
        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");
        when(mockSubmitService.submit(eq(validApplication), eq(userId), eq(authorizationToken))).thenReturn(registryData);

        ResultActions result = mockMvc.perform(post(SUBMIT_SERVICE_URL)
                .headers(httpHeaders)
                .content(validApplication.toString())
                .contentType(contentType));

        result.andExpect(status().isOk())
                .andExpect(content().string(registryData.toString()));
    }

    @Test
    public void resubmitSuccessfully() throws Exception {
        when(mockSubmitService.resubmit(eq(Long.parseLong("123456789")))).thenReturn("1111111111");

        ResultActions result = mockMvc.perform(get(RESUBMIT_SERVICE_URL + "/123456789"));

        result.andExpect(status().isOk())
                .andExpect(content().string("1111111111"));
    }


    @Test
    public void submitInvalidJson() throws Exception {
        String invalidJson = "invalid json";

        ResultActions result = mockMvc.perform(post(SUBMIT_SERVICE_URL)
                .content(invalidJson)
                .contentType(contentType));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void testSubmitThrowingMailException() throws Exception {
        JsonNode validApplication = TestUtils.getJsonNodeFromFile("formPayload.json");
        doThrow(MailSendException.class).when(mockSubmitService).submit(eq(validApplication), eq(userId), eq(authorizationToken));

        ResultActions result = mockMvc.perform(post(SUBMIT_SERVICE_URL)
                .headers(httpHeaders)
                .content(validApplication.toString())
                .contentType(contentType));

        result.andExpect(status().isBadGateway())
                .andExpect(status().reason("Could not send the probate email"));
    }

    @Test
    public void testSubmitThrowingParsingSubmitException() throws Exception {
        JsonNode validApplication = TestUtils.getJsonNodeFromFile("formPayload.json");
        doThrow(ParsingSubmitException.class).when(mockSubmitService).submit(eq(validApplication), eq(userId), eq(authorizationToken));

        ResultActions result = mockMvc.perform(post(SUBMIT_SERVICE_URL)
                .headers(httpHeaders)
                .content(validApplication.toString())
                .contentType(contentType));

        result.andExpect(status().isUnprocessableEntity())
                .andExpect(status().reason("Error creating email payload"));
    }


}
