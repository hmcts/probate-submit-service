package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.probate.services.submit.model.ParsingSubmitException;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class MailClientTest {

    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Autowired
    SpringTemplateEngine templateEngine;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private MailMessageBuilder mailMessageBuilderMock;

    @Mock
    private JavaMailSenderImpl mailSenderMock;

    @Mock
    private MimeMessage mimeMessageMock;
    
    private MailClient mailClient;

    private Calendar submissionTimestamp;
    private JsonNode registryData;

    private ObjectMapper objectMapper;

    private ObjectNode submitData;

    @Before
    public void setUp() throws IOException {
        mailClient = new MailClient(mailSenderMock, mailMessageBuilderMock);
        submissionTimestamp = Calendar.getInstance();
        registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json").get("registry");
        objectMapper = new ObjectMapper();

        submitData = objectMapper.createObjectNode();
//        submitData.set("submitdata", objectMapper.createObjectNode().set("submissionReference", new LongNode(1234)));
    }


    @Test
    public void testProcessSuccess() throws MessagingException {
        doNothing().when(mailSenderMock).send(any(MimeMessage.class));
        when(mailMessageBuilderMock.buildMessage(any(JsonNode.class), any(JsonNode.class), any(Properties.class), any(Calendar.class))).thenReturn(mimeMessageMock);
        when(mimeMessageMock.getHeader(anyString(), any())).thenReturn("1234");

        String response = mailClient.execute(submitData, registryData,
                submissionTimestamp);

        assertThat(response, is(""));
    }

    @Test(expected = ParsingSubmitException.class)
    public void shouldThrowParsingSubmitExceptionWhenMailClientThrowsMessageException() throws MessagingException {
        when(mailMessageBuilderMock.buildMessage(any(JsonNode.class), any(JsonNode.class),
                any(Properties.class), any(Calendar.class))).thenThrow(new MessagingException());
        when(mailSenderMock.getJavaMailProperties()).thenReturn(new Properties());

        mailClient.execute(NullNode.getInstance(), registryData, submissionTimestamp);
    }
}
