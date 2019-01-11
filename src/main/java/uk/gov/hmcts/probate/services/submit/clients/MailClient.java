package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.services.submit.model.ParsingSubmitException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Calendar;

@Component
public class MailClient implements Client<JsonNode, String> {

    private JavaMailSenderImpl mailSender;
    private MailMessageBuilder mailMessageBuilder;

    @Autowired
    public MailClient(JavaMailSenderImpl mailSender, MailMessageBuilder mailMessageBuilder) {
        this.mailSender = mailSender;
        this.mailMessageBuilder = mailMessageBuilder;
    }

    @Override
    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public String execute(JsonNode submitData, JsonNode registryData,  Calendar submissionTimestamp) {
        try {
            MimeMessage message = mailMessageBuilder.buildMessage(submitData, registryData, mailSender.getJavaMailProperties(), submissionTimestamp);
            mailSender.send(message);
            return submitData.at("/submitdata/submissionReference").asText();
        } catch (MessagingException ex) {
            throw new ParsingSubmitException("Could not build or extract the data from the message", ex);
        }
    }
}
