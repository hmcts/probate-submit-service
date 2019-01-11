package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.probate.services.submit.model.PaymentResponse;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ConfigurationProperties(prefix = "ccd", ignoreInvalidFields = true)
public class CoreCaseDataMapperTest {

    private static final String CREATE_CASE_CCD_EVENT_ID = "createCase";

    @Autowired
    private CoreCaseDataMapper coreCaseDataMapper;

    @Autowired
    private ObjectMapper mapper;

    private JsonNode registryData, ccdToken;
    private Calendar submissonTimestamp;
    private String ccdEventId;
    private JsonNode submitdata;

    @Before
    public void setup() throws ParseException, IOException {
        registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");
        submissonTimestamp = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSS");
        submissonTimestamp.setTime(sdf.parse("2017-08-24 11:37:07.221"));
        ccdToken = new TextNode("dummyToken");
        ccdEventId = "applyForGrant";
        submitdata = TestUtils.getJsonNodeFromFile("formPayload.json").get("submitdata");
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
        JsonNode value = mapper.createObjectNode().set("totalFee", new TextNode("216.50"));

        Optional<JsonNode> mappedData = coreCaseDataMapper.monetaryValueMapper(value, "totalFee");
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapUnmappableMonetaryValueWithDecimalTestLessThanOnePound() {
        Optional expected = Optional.of(new TextNode("50"));
        JsonNode value = mapper.createObjectNode().set("totalFee", new TextNode("0.50"));

        Optional<JsonNode> mappedData = coreCaseDataMapper.monetaryValueMapper(value, "totalFee");
        assertEquals(expected, mappedData);
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
    public void mapExecutorsTest() throws IOException {
        Map<String, JsonNode> expected = new HashMap<>();
        expected.put("executorsNotApplying", TestUtils.getJsonNodeFromFile("ccdNotApplyingExecutors.json"));
        expected.put("executorsApplying", TestUtils.getJsonNodeFromFile("ccdApplyingExecutors.json"));
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getExecutorMap(), coreCaseDataMapper::executorsMapper);
        assertEquals(expected, mappedData);

    }

    @Test
    public void mapDocumentTest() throws IOException {
        Map<String, JsonNode> expected = new HashMap<>();
        expected.put("boDocumentsUploaded", TestUtils.getJsonNodeFromFile("ccdDocumentUploads.json"));
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getDocumentUploadMap(), coreCaseDataMapper::documentUploadMapper);
        assertEquals(expected, mappedData);

    }

    @Test
    public void mapNonExistentDocumentTest()  {
        Optional<JsonNode> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.documentUploadMapper(submitdata, "noSuchField");
        assertEquals(expected, mappedData);

    }

    @Test
    public void mapNonExistentExecutorsTest() {
        Optional<JsonNode> expected = Optional.empty();
        Optional<JsonNode> mappedData = coreCaseDataMapper.executorsMapper(submitdata, "noSuchField");
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapNotApplyingExecutorTest() throws IOException {
        Optional<JsonNode> expected = Optional.of(TestUtils.getJsonNodeFromFile("ccdNotApplyingExecutors.json").at("/0"));
        JsonNode executor = submitdata.at("/executorsNotApplying/0");
        Optional<JsonNode> mappedData = coreCaseDataMapper.mapExecutor(executor);
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapApplyingExecutorTest() throws IOException {
        Optional<JsonNode> expected = Optional.of(TestUtils.getJsonNodeFromFile("ccdApplyingExecutors.json").at("/0"));
        JsonNode executor = submitdata.at("/executorsApplying/0");
        Optional<JsonNode> mappedData = coreCaseDataMapper.mapExecutor(executor);
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapMissingExecutorApplyingFieldTest() throws IOException {
        Optional<JsonNode> expected = Optional.of(TestUtils.getJsonNodeFromFile("ccdNotApplyingExecutors.json").at("/4"));
        JsonNode executor = submitdata.at("/executorsNotApplying/4");
        Optional<JsonNode> mappedData = coreCaseDataMapper.mapExecutor(executor);
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapApplyingExecutorWithNewNameTest() throws IOException {
        Optional<JsonNode> expected = Optional.of(TestUtils.getJsonNodeFromFile("ccdApplyingExecutors.json").at("/1"));
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
    public void mapAliasesTest() throws IOException {
        JsonNode expected = TestUtils.getJsonNodeFromFile("ccdAliases.json");
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
    public void mapAliasTest() throws IOException {
        Optional<JsonNode> expected = Optional.of(TestUtils.getJsonNodeFromFile("ccdAliases.json").at("/0"));
        JsonNode alias = submitdata.at("/deceasedOtherNames/name_0");
        Optional<JsonNode> mappedData = coreCaseDataMapper.mapAlias(alias);
        assertEquals(expected, mappedData);
    }

    @Test
    public void mapDeclarationTest() throws IOException {
        JsonNode expected = TestUtils.getJsonNodeFromFile("ccdDeclaration.json");
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
    public void mapLegalStatementTest() throws IOException {
        JsonNode expected = TestUtils.getJsonNodeFromFile("ccdLegalStatement.json");
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
    public void mapAddressesTest() throws IOException {
        Map<String, JsonNode> expected = TestUtils.getJsonMapFromFile("ccdAddresses.json");
        Map<String, JsonNode> mappedData = coreCaseDataMapper.map(submitdata, coreCaseDataMapper.getAddressMap(), coreCaseDataMapper::addressMapper);
        assertEquals(expected, mappedData);
    }

    @Test
    public void shouldUpdatePaymentStatus() throws IOException {
        String token  = "TOKEN123456";
        ObjectNode tokenNode = mapper.createObjectNode();
        tokenNode.put("token", token);
        JsonNode paymentJsonNode = TestUtils.getJsonNodeFromFile("paymentResponse.json");
        PaymentResponse paymentResponse = new PaymentResponse(paymentJsonNode);
        JsonNode updatedCcdJson = coreCaseDataMapper.updatePaymentStatus(paymentResponse,
                CREATE_CASE_CCD_EVENT_ID, tokenNode);

        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/status").asText(), is(equalTo("Success")));
        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/date").asText(), is(equalTo("2018-09-05")));
        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/reference").asText(), is(equalTo("RC-1536-1457-4509-0641")));
        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/amount").asText(), is(equalTo("36500")));
        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/transactionId").asText(), is(equalTo("r4jb083f4pi6g8chhcnmb2gsa3")));
        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/siteId").asText(), is(equalTo("P223")));
        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/method").asText(), is(equalTo("online")));
        assertThat(updatedCcdJson.get("event_token"), is(equalTo(tokenNode)));
    }

    @Test
    public void shouldUpdatePaymentStatusWhenDateIsNotCorrectFormat() throws IOException {
        String token  = "TOKEN123456";
        ObjectNode tokenNode = mapper.createObjectNode();
        tokenNode.put("token", token);
        JsonNode paymentJsonNode = TestUtils.getJsonNodeFromFile("paymentResponse.json");
        ((ObjectNode) paymentJsonNode).put("date", "1234");
        PaymentResponse paymentResponse = new PaymentResponse(paymentJsonNode);
        JsonNode updatedCcdJson = coreCaseDataMapper.updatePaymentStatus(paymentResponse,
                CREATE_CASE_CCD_EVENT_ID, tokenNode);

        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/status").asText(), is(equalTo("Success")));
        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/date").asText(), is(equalTo("")));
        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/reference").asText(), is(equalTo("RC-1536-1457-4509-0641")));
        assertThat(updatedCcdJson.at("/data/payments").get(0).at("/value/amount").asText(), is(equalTo("36500")));
        assertThat(updatedCcdJson.get("event_token"), is(equalTo(tokenNode)));
    }

    @Test
    public void shouldNotUpdatePaymentStatusWhenNoPaymentResponse() throws IOException {
        String token  = "TOKEN123456";
        ObjectNode tokenNode = mapper.createObjectNode();
        tokenNode.put("token", token);
        JsonNode paymentJsonNode = TestUtils.getJsonNodeFromFile("noPaymentResponse.json");
        PaymentResponse paymentResponse = new PaymentResponse(paymentJsonNode);
        JsonNode updatedCcdJson = coreCaseDataMapper.updatePaymentStatus(paymentResponse,
                CREATE_CASE_CCD_EVENT_ID, tokenNode);

        assertThat(updatedCcdJson.at("/data/payments").isMissingNode(), is(equalTo(true)));
        assertThat(updatedCcdJson.get("event_token"), is(equalTo(tokenNode)));
    }

}
