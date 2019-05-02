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

    private final Map<Integer, Registry> registryMap;
    private final ObjectMapper mapper;

    private static int registryCounter = 1;

    @Autowired
    public SequenceService(Map<Integer, Registry> registryMap, ObjectMapper mapper) {
        this.registryMap = registryMap;
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
        registryMapper.put("address", registry.getAddress());
        registryDataObject.set(REGISTRY, registryMapper);

        return registryDataObject;
    }


    public Registry identifyNextRegistry() {
        Registry nextRegistry =
                registryMap.get(registryCounter % registryMap.size());
        registryCounter++;
        return nextRegistry;
    }
}
