package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring4.SpringTemplateEngine;
import uk.gov.hmcts.probate.services.submit.model.ParsingSubmitException;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MailClientTest {

    @Autowired
    SpringTemplateEngine templateEngine;

    @Mock
    private MailMessageBuilder mailMessageBuilderMock;

    @Mock
    private JavaMailSenderImpl mailSenderMock;

    @Mock
    private MimeMessage mimeMessageMock;

    private MailClient mailClient;

    private Calendar submissionTimestamp;
    private JsonNode registryData;

    @Before
    public void setUp() throws IOException {
        mailClient = new MailClient(mailSenderMock, mailMessageBuilderMock);
        submissionTimestamp = Calendar.getInstance();
        registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");
    }


    @Test
    public void testProcessSuccess() throws MessagingException {
        doNothing().when(mailSenderMock).send(any(MimeMessage.class));
        when(mailMessageBuilderMock.buildMessage(any(JsonNode.class), any(JsonNode.class), any(Properties.class), any(Calendar.class))).thenReturn(mimeMessageMock);
        when(mimeMessageMock.getHeader(anyString(), any())).thenReturn("1234");

        String response = mailClient.execute(NullNode.getInstance(), registryData,
                submissionTimestamp);

        assertThat(response, is("1234"));
    }

    @Test(expected = ParsingSubmitException.class)
    public void shouldThrowParsingSubmitExceptionWhenMailClientThrowsMessageException() throws MessagingException {
        when(mailMessageBuilderMock.buildMessage(any(JsonNode.class), any(JsonNode.class),
                any(Properties.class), any(Calendar.class))).thenThrow(new MessagingException());

        mailClient.execute(NullNode.getInstance(), registryData, submissionTimestamp);
    }
}
