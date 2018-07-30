package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@Configuration
@Validated
@ConfigurationProperties(prefix = "ccd")
public class CoreCaseDataMapperTest {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private CoreCaseDataMapper coreCaseDataMapper;
    private JsonNode registryData, ccdToken;
    private Calendar submissonTimestamp;
    private String ccdEventId;
    private JsonNode submitdata;

    @NotNull
    private Map<String, String> fieldMap;

    public Map<String, String> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, String> fieldMap) {
        this.fieldMap = fieldMap;
    }

    @NotNull
    private Map<String, String> monetaryValueMap;

    public Map<String, String> getMonetaryValueMap() {
        return monetaryValueMap;
    }

    public void setMonetaryValueMap(Map<String, String> monetaryValueMap) {
        this.monetaryValueMap = monetaryValueMap;
    }


    @Before
    public void setup() throws ParseException {
        registryData = testUtils.getJsonNodeFromFile("registryDataSubmit.json");
        submissonTimestamp = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSS");
        submissonTimestamp.setTime(sdf.parse("2017-08-24 11:37:07.221"));
        ccdToken = new TextNode("dummyToken");
        ccdEventId = "applyForGrant";
        submitdata = testUtils.getJsonNodeFromFile("formPayload.json").get("submitdata");
    }

    @Test
    public void createCcdDataTest() {
        JsonNode mappedData = coreCaseDataMapper.createCcdData(submitdata, ccdEventId, ccdToken, submissonTimestamp, registryData);
        assertEquals(mappedData.get("event").get("id").asText(), ccdEventId);
        assertEquals(mappedData.get("event_token"), ccdToken);
        assertNotNull (mappedData.get("data"));       
    }

    @Test
    public void mapDataTest() {
        JsonNode mappedData = coreCaseDataMapper.mapData(submitdata, submissonTimestamp, registryData);
        JsonNode registry = registryData.get("registry");
        assertTrue(mappedData.get("applicationSubmittedDate").asText().equals("2017-08-24"));
        assertTrue(mappedData.get("applicationID").equals(registryData.get("submissionReference")));
        assertTrue(mappedData.get("registryLocation").equals(registry.get("name")));
        assertNotNull(mappedData.get("primaryApplicantForenames"));
        assertNotNull(mappedData.get("deceasedDateOfDeath"));
        assertNotNull(mappedData.get("executorsNotApplying"));
        assertNotNull(mappedData.get("deceasedDateOfDeath"));
        assertNotNull(mappedData.get("declaration"));
        assertNotNull(mappedData.get("applicationType"));
    }

    @Test
    public void mapMonetaryValuesTest() {
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getMonetaryValueMap(), coreCaseDataMapper::monetaryValueMapper);

        monetaryValueMap
                .values()
                .forEach(
                        e -> assertTrue(e + " is not found in the mapped data", mappedData.containsKey(e))
                );
    }

    @Test
    public void mapMonetaryValueTest() {
        Optional expected = Optional.of(new TextNode("2222200"));
        Optional<JsonNode> mappedData = coreCaseDataMapper.monetaryValueMapper(submitdata, "ihtNetValue");
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapNonExistentMonetaryValueTest() {
        Optional<JsonNode> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.monetaryValueMapper(submitdata, "noSuchField");
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapUnmappableMonetaryValueTest() {
        Optional<JsonNode> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.monetaryValueMapper(submitdata, "applicantFirstName");
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapUnmappableMonetaryValueWithDecimalTest() {
        Optional expected = Optional.of(new TextNode("21650"));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode value = mapper.createObjectNode().set("totalFee", new TextNode("216.50"));

        Optional<JsonNode> mappedData = coreCaseDataMapper.monetaryValueMapper(value, "totalFee");
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapFieldsTest() {
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getFieldMap(), coreCaseDataMapper::fieldMapper);

        fieldMap
                .values()
                .forEach(
                        e -> assertTrue(e + " is not found in the mapped data", mappedData.containsKey(e))
                );
    }

    @Test
    public void getSimpleFieldTest() {
        Optional<JsonNode> mappedData = coreCaseDataMapper.fieldMapper(submitdata, "applicantFirstName");
        assertTrue(mappedData.isPresent());
    }

    @Test
    public void getArrayFieldTest() {
        Optional<JsonNode> mappedData = coreCaseDataMapper.fieldMapper(submitdata, "executorsNotApplying");
        assertTrue(mappedData.isPresent());
    }

    @Test
    public void getNonExistentFieldTest() {
        Optional<String> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.fieldMapper(submitdata, "noSucnoSuchFieldElement");
        assertFalse(mappedData.isPresent());
    }

    @Test
    public void mapExecutorsTest() {
        Map<String, JsonNode> expected = new HashMap<>();
        expected.put("executorsNotApplying", testUtils.getJsonNodeFromFile("ccdNotApplyingExecutors.json"));
        expected.put("executorsApplying", testUtils.getJsonNodeFromFile("ccdApplyingExecutors.json"));
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getExecutorMap(), coreCaseDataMapper::executorsMapper);
        assertEquals(expected, mappedData);

    }

    @Test
    public void mapNonExistentExecutorsTest() {
        Optional<JsonNode> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.executorsMapper(submitdata, "noSuchField");
        assertEquals(expected, mappedData);
    }

    public void mapNotApplyingExecutorTest() {
        Optional<JsonNode> expected = Optional.of(testUtils.getJsonNodeFromFile("ccdNotApplyingExecutors.json").at("/0"));
        JsonNode executor = submitdata.at("/executorsNotApplying/0");
        Optional<JsonNode> mappedData = coreCaseDataMapper.mapExecutor(executor);
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapApplyingExecutorTest() {
        Optional<JsonNode> expected = Optional.of(testUtils.getJsonNodeFromFile("ccdApplyingExecutors.json").at("/0"));
        JsonNode executor = submitdata.at("/executorsApplying/0");
        Optional<JsonNode> mappedData = coreCaseDataMapper.mapExecutor(executor);
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapMissingExecutorApplyingFieldTest() {
        Optional<JsonNode> expected = Optional.of(testUtils.getJsonNodeFromFile("ccdNotApplyingExecutors.json").at("/4"));
        JsonNode executor = submitdata.at("/executorsNotApplying/4");
        Optional<JsonNode> mappedData = coreCaseDataMapper.mapExecutor(executor);
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapApplyingExecutorWithNewNameTest() {
        Optional<JsonNode> expected = Optional.of(testUtils.getJsonNodeFromFile("ccdApplyingExecutors.json").at("/1"));
        JsonNode executor = submitdata.at("/executorsApplying/1");
        Optional<JsonNode> mappedData = coreCaseDataMapper.mapExecutor(executor);
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapDatesTest() {
        Map<String, JsonNode> expectedDates = new HashMap<>();
        expectedDates.put("deceasedDateOfBirth", new TextNode("1900-02-01"));
        expectedDates.put("deceasedDateOfDeath", new TextNode("2000-02-01"));
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getDateMap(), coreCaseDataMapper::dateMapper);
        assertEquals(expectedDates, mappedData);
    }

    @Test
    public void mapDateTest() {
        Optional<JsonNode> mappedData = coreCaseDataMapper.dateMapper(submitdata, "deceasedDob");
        assert (mappedData.isPresent());
        assertEquals(Optional.of(new TextNode(LocalDate.of(1900, 2, 1).toString())), mappedData);
    }

    @Test
    public void mapNonExistentDateTest() {
        Optional<JsonNode> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.dateMapper(submitdata, "noSuchField");
        assertFalse(mappedData.isPresent());
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapUnmappableDateTest() {
        Optional<JsonNode> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.dateMapper(submitdata, "applicantFirstName");
        assertFalse(mappedData.isPresent());
        assertEquals(expected, mappedData);
    }
       
    @Test
    public void mapAliasesTest() {
        JsonNode expected = testUtils.getJsonNodeFromFile("ccdAliases.json");
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getAliasMap(), coreCaseDataMapper::aliasesMapper);
        assertEquals(expected, mappedData.get("deceasedAliasNameList"));
    }
        
    @Test
    public void mapNonExistentAliasesTest() {
        Optional<JsonNode> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.aliasesMapper(submitdata, "noSuchField");
        assertEquals(expected, mappedData);
    }
    
    @Test
    public void mapAliasTest() {
        Optional<JsonNode> expected = Optional.of(testUtils.getJsonNodeFromFile("ccdAliases.json").at("/0"));
        JsonNode alias = submitdata.at("/deceasedOtherNames/name_0");
        Optional<JsonNode> mappedData = coreCaseDataMapper.mapAlias(alias);
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapDeclarationTest() {
        JsonNode expected = testUtils.getJsonNodeFromFile("ccdDeclaration.json");
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getDeclarationMap(), coreCaseDataMapper::declarationMapper);
        assertEquals(expected, mappedData.get("declaration"));
    }

    @Test
    public void mapNonExistentDeclarationTest() {
        Optional<JsonNode> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.declarationMapper(submitdata, "noSuchField");
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapLegalStatementTest() {
        JsonNode expected = testUtils.getJsonNodeFromFile("ccdLegalStatement.json");
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getLegalStatementMap(), coreCaseDataMapper::legalStatementMapper);
        assertEquals(expected, mappedData.get("legalStatement"));
    }

    @Test
    public void mapNonExistentLegalStatementTest() {
        Optional<JsonNode> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.legalStatementMapper(submitdata, "noSuchField");
        assertEquals(expected, mappedData);
    }


    @Test
    public void mapAddressesTest() {
       Map<String, JsonNode> expected = testUtils.getJsonMapFromFile("ccdAddresses.json");
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getAddressMap(), coreCaseDataMapper::addressMapper);
        assertEquals(expected, mappedData);
    }
}
