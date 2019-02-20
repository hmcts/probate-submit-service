package uk.gov.hmcts.probate.services.submit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RegistryService {

    private static final String SUBMISSION_REFERENCE = "submissionReference";
    private static final String REGISTRY = "registry";
    private static final String SEQUENCE_NUMBER = "sequenceNumber";
    private static final String EMAIL = "email";
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JavaMailSenderImpl mailSender;
    private final ObjectMapper mapper;

    @Autowired
    public RegistryService(JavaMailSenderImpl mailSender, ObjectMapper mapper) {
        this.mailSender = mailSender;
        this.mapper = mapper;
    }


    JsonNode populateRegistryData(long submissionReference, JsonNode formDataObject) {
        ObjectNode registryDataObject = mapper.createObjectNode();
        ObjectNode registryMapper = mapper.createObjectNode();

        JsonNode formData = formDataObject.get("formdata");
        registryDataObject.put(SUBMISSION_REFERENCE, submissionReference);

        if(formData.has(REGISTRY)) {
            registryMapper.set(SEQUENCE_NUMBER, formData.get(REGISTRY).get(SEQUENCE_NUMBER));
        } else {
            registryMapper.put(SEQUENCE_NUMBER, submissionReference);
        }
        registryMapper.put( EMAIL, mailSender.getJavaMailProperties().getProperty("recipient"));
        logger.info("MAIL-Recepient  Praveen");
        logger.info(mailSender.getJavaMailProperties().getProperty("recipient"));

        registryDataObject.set(REGISTRY, registryMapper);
        return registryDataObject;
    }
}
