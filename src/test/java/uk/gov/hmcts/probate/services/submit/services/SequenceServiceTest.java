package uk.gov.hmcts.probate.services.submit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.probate.services.submit.Registry;
import uk.gov.hmcts.probate.services.submit.clients.PersistenceClient;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
//TODO: Remove linient strictness for quality test maintanance
public class SequenceServiceTest {

    @Mock
    private Map<Integer, Registry> registryMap;

    @Mock
    private Registry mockRegistry;

    private SequenceService sequenceService;

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        sequenceService = new SequenceService(registryMap, mapper);
    }

    @Test
    public void nextRegistry() throws IOException {
        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");

        Registry registry = new Registry();
        registry.setName("Oxford");
        registry.setAddress("Test Address Line 1\nTest Address Line 2\nTest Address Postcode");

        JsonNode result = sequenceService.populateRegistrySubmitData(registry);
        assertEquals(result.toString(), registryData.toString());
    }

    @Test
    public void populateRegistrySubmitData() throws IOException {
        when(registryMap.size()).thenReturn(2);
        when(registryMap.get(anyInt())).thenReturn(mockRegistry);

        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");
        when(sequenceService.identifyNextRegistry()).thenReturn(mockRegistry);
        when(mockRegistry.getName()).thenReturn("Oxford");
        when(mockRegistry.getAddress()).thenReturn("Test Address Line 1\nTest Address Line 2\nTest Address Postcode");

        JsonNode response = sequenceService.populateRegistrySubmitData(mockRegistry);
        assertEquals(response.toString(), registryData.toString());
    }

    @Test
    public void identifyNextRegistryRatios() {
        String oxf = "Oxford";
        String bir = "Birmingham";
        String man = "Manchester";
        Map<Integer, Registry> newRegistryMap = new HashMap<>();
        newRegistryMap.put(0, buildRegistry(oxf));
        newRegistryMap.put(1, buildRegistry(bir));
        newRegistryMap.put(2, buildRegistry(man));
        newRegistryMap.put(3, buildRegistry(man));
        newRegistryMap.put(4, buildRegistry(man));
        newRegistryMap.put(5, buildRegistry(man));
        newRegistryMap.put(6, buildRegistry(man));
        newRegistryMap.put(7, buildRegistry(man));

        SequenceService sequenceServiceTest = new SequenceService(newRegistryMap, mapper);

        double numOxf = 0;
        double numBirm = 0;
        double numMan = 0;
        double totalCalls = 200;

        for (double i = 0; i < totalCalls; i++) {
            Registry result = sequenceServiceTest.identifyNextRegistry();
            switch (result.getName()) {
                case "Oxford":
                    numOxf++;
                    break;
                case "Birmingham":
                    numBirm++;
                    break;
                case "Manchester":
                    numMan++;
                    break;
                default:
                    break;
            }
        }

        double oxfRatio = 100 * numOxf / totalCalls;
        double birmRatio = 100 * numBirm / totalCalls;
        double manRatio = 100 * numMan / totalCalls;
        assertThat(12.5, is(equalTo(oxfRatio)));
        assertThat(12.5, is(equalTo(birmRatio)));
        assertThat(75.0, is(equalTo(manRatio)));

    }

    private Registry buildRegistry(String name) {
        Registry registry = new Registry();
        registry.setName(name);
        return registry;
    }
}
