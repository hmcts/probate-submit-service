package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.*;

@Component
class MailMessageBuilder {

    private SpringTemplateEngine templateEngine;

    @Autowired
    public MailMessageBuilder(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public MimeMessage buildMessage(JsonNode submitData, JsonNode registryData, Properties messageProperties,  Calendar submissionTimestamp) throws MessagingException {
        MimeMessage mailMessage = new MimeMessage(Session.getDefaultInstance(messageProperties));
        MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage);
        messageHelper.setSubject(messageProperties.getProperty("subject"));
        messageHelper.setFrom(messageProperties.getProperty("sender"));
        messageHelper.setTo(registryData.get("email").asText());

        String messageText = templateEngine.process("email-template", createTemplateContext(submitData, registryData.get("sequenceNumber").asLong(), submissionTimestamp));

        messageHelper.setText(messageText, true);
        return mailMessage;
    }

    private Context createTemplateContext(JsonNode submitData, long registrySequenceNumber, Calendar submissonTimestamp) {
        Context ctx = new Context(Locale.getDefault());
        ctx.setVariables(getDataMap(submitData));
        ctx.setVariable("submissionDate", submissonTimestamp);
        ctx.setVariable("registrySequenceNumber", registrySequenceNumber);
        return ctx;
    }

    private Map<String, Object> getDataMap(JsonNode submitData) {
        Map<String, Object> dataMap = new HashMap<>();
        submitData.get("submitdata")
                .fieldNames()
                .forEachRemaining(fieldName ->
                        dataMap.put(fieldName, getFieldValue(submitData.get("submitdata"), fieldName)));
        return dataMap;
    }

    private Object getFieldValue(JsonNode submitData, String fieldName) {
        return submitData.get(fieldName) instanceof TextNode ? submitData.get(fieldName).asText() : submitData.get(fieldName);
    }
}
