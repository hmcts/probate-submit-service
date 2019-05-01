package uk.gov.hmcts.probate.services.submit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.probate.services.submit.Registry;
import uk.gov.hmcts.probate.services.submit.clients.PersistenceClient;

import java.util.Map;

@Service
public class SequenceService {
    private static final String REGISTRY = "registry";
    private static final String SEQUENCE_NUMBER = "sequenceNumber";

    private final Map<Integer, Registry> registryMap;
    private final PersistenceClient persistenceClient;
    private final JavaMailSenderImpl mailSender;
    private final ObjectMapper mapper;

    private static int registryCounter = 1;

    @Autowired
    public SequenceService(Map<Integer, Registry> registryMap, PersistenceClient persistenceClient, JavaMailSenderImpl mailSender, ObjectMapper mapper) {
        this.registryMap = registryMap;
        this.persistenceClient = persistenceClient;
        this.mailSender = mailSender;
        this.mapper = mapper;
    }

    public synchronized JsonNode nextRegistry() {
        Registry nextRegistry = identifyNextRegistry();
        return populateRegistrySubmitData(nextRegistry);
    }

    JsonNode populateRegistrySubmitData(Registry registry) {
        ObjectNode registryDataObject = mapper.createObjectNode();
        ObjectNode registryMapper = mapper.createObjectNode();

        registryMapper.put("name", registry.getName());
        registryMapper.put(SEQUENCE_NUMBER, getRegistrySequenceNumber(registry));
        registryMapper.put("address", registry.getAddress());
        registryDataObject.set(REGISTRY, registryMapper);

        return registryDataObject;
    }

    long getRegistrySequenceNumber(Registry registry) {
        return persistenceClient.getNextSequenceNumber(registry.getName());
    }

    public Registry identifyNextRegistry() {
        Registry nextRegistry =
                registryMap.get(registryCounter % registryMap.size());
        registryCounter++;
        return nextRegistry;
    }
}
