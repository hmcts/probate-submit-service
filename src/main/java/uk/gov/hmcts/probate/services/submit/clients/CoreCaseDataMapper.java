package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.probate.services.submit.model.PaymentResponse;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "ccd")
public class CoreCaseDataMapper {
    private static final String IHT_FORM_VALUE_205 = "IHT205";
    private static final String VALUE = "value";
    private static final String DECEASED = "deceased";
    private static final String DECEASED_OTHER_NAMES = "deceasedOtherNames";
    private static final String DECEASED_ESTATE_VALUE = "deceasedEstateValue";
    private static final String DECEASED_ESTATE_LAND = "deceasedEstateLand";
    private static final String EXECUTORS_NOT_APPLYING = "executorsNotApplying";
    private static final String EXECUTORS_APPLYING = "executorsApplying";
    private static final String INTRO = "intro";
    private static final String APPLICANT = "applicant";
    private static final String BINARY_URL_SUFFIX = "binary";
    public static final String IHT_FORM_ID = "ihtFormId";
    private final Logger logger = LoggerFactory.getLogger(CoreCaseDataMapper.class);
    private final DateFormat originalDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");
    private final DateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

    @Autowired
    private ObjectMapper mapper;

    @Value("${ccd.probate.fullName}")
    private String fullName;
    @Value("${ccd.probate.isApplying}")
    private String isApplying;
    @Value("${ccd.probate.address}")
    private String address;
    @Value("${ccd.probate.email}")
    private String email;
    @Value("${ccd.probate.mobile}")
    private String mobile;
    @Value("${ccd.probate.hasOtherName}")
    private String hasOtherName;
    @Value("${ccd.probate.currentName}")
    private String currentName;
    @Value("${ccd.probate.currentNameReason}")
    private String currentNameReason;
    @Value("${ccd.probate.otherReason}")
    private String otherReason;
    @Value("${ccd.probate.notApplyingKey}")
    private String notApplyingKey;
    @Value("${ccd.probate.filename}")
    private String filename;
    @Value("${ccd.probate.url}")
    private String url;
    @Value("${ccd.ccd.notApplyingExecutorName}")
    private String notApplyingExecutorName;
    @Value("${ccd.ccd.notApplyingExecutorReason}")
    private String notApplyingExecutorReason;
    @Value("${ccd.ccd.applyingExecutorName}")
    private String applyingExecutorName;
    @Value("${ccd.ccd.applyingExecutorEmail}")
    private String applyingExecutorEmail;
    @Value("${ccd.ccd.applyingExecutorPhoneNumber}")
    private String applyingExecutorPhoneNumber;
    @Value("${ccd.ccd.applyingExecutorAddress}")
    private String applyingExecutorAddress;
    @Value("${ccd.ccd.applyingExecutorOtherNames}")
    private String applyingExecutorOtherNames;
    @Value("${ccd.ccd.applyingExecutorOtherNamesReason}")
    private String applyingExecutorOtherNamesReason;
    @Value("${ccd.ccd.applyingExecutorOtherReason}")
    private String applyingExecutorOtherReason;
    @Value("${ccd.ccd.DocumentType}")
    private String DocumentType;
    @Value("${ccd.ccd.DocumentLink}")
    private String DocumentLink;
    @Value("${ccd.ccd.Comment}")
    private String Comment;

    @Value("${ccd.ccd.documentUrl}")
    private String documentUrl;
    @Value("${ccd.ccd.documentBinaryUrl}")
    private String documentBinaryUrl;
    @Value("${ccd.ccd.documentFilename}")
    private String documentFilename;

    @NotNull
    private Map<String, String> reasonMap;
    @NotNull
    private Map<String, String> dateMap;
    @NotNull
    private Map<String, String> fieldMap;
    @NotNull
    private Map<String, String> monetaryValueMap;
    @NotNull
    private Map<String, String> executorMap;
    @NotNull
    private Map<String, String> aliasMap;
    @NotNull
    private Map<String, String> declarationMap;
    @NotNull
    private Map<String, String> legalStatementMap;
    @NotNull
    private Map<String, String> addressMap;
    @NotNull
    private Map<String, String> documentUploadMap;

    public Map<String, String> getReasonMap() {
        return reasonMap;
    }

    public void setReasonMap(Map<String, String> reasonMap) {
        this.reasonMap = reasonMap;
    }

    public Map<String, String> getDateMap() {
        return dateMap;
    }

    public void setDateMap(Map<String, String> dateMap) {
        this.dateMap = dateMap;
    }

    public Map<String, String> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, String> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public Map<String, String> getMonetaryValueMap() {
        return monetaryValueMap;
    }

    public void setMonetaryValueMap(Map<String, String> monetaryValueMap) {
        this.monetaryValueMap = monetaryValueMap;
    }

    public Map<String, String> getExecutorMap() {
        return executorMap;
    }

    public void setExecutorMap(Map<String, String> executorMap) {
        this.executorMap = executorMap;
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    public void setAliasMap(Map<String, String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    public Map<String, String> getDeclarationMap() {
        return declarationMap;
    }

    public void setDeclarationMap(Map<String, String> declarationMap) {
        this.declarationMap = declarationMap;
    }

    public Map<String, String> getLegalStatementMap() {
        return legalStatementMap;
    }

    public void setLegalStatementMap(Map<String, String> legalStatementMap) {
        this.legalStatementMap = legalStatementMap;
    }

    public Map<String, String> getAddressMap() {
        return addressMap;
    }

    public void setAddressMap(Map<String, String> addressMap) {
        this.addressMap = addressMap;
    }

    public Map<String, String> getDocumentUploadMap() {
        return documentUploadMap;
    }

    public void setDocumentUploadMap(Map<String, String> documentUploadMap) {
        this.documentUploadMap = documentUploadMap;
    }

    public JsonNode createCcdData(JsonNode probateData, String ccdEventId, JsonNode ccdToken, Calendar submissionTimestamp, JsonNode registryData) {
        ObjectNode event = mapper.createObjectNode();
        event.put("id", ccdEventId);
        event.put("description", "");
        event.put("summary", "Probate application");
        ObjectNode formattedData = mapper.createObjectNode();
        formattedData.set("event", event);
        formattedData.put("ignore_warning", true);
        formattedData.set("event_token", ccdToken);
        formattedData.set("data", mapData(probateData, submissionTimestamp, registryData));
        return formattedData;
    }

    public ObjectNode mapData(JsonNode probateData, Calendar submissionTimestamp, JsonNode registryData) {
        ObjectNode ccdData = mapper.createObjectNode();
        JsonNode registry = registryData.get("registry");
        ccdData.set("applicationID", registryData.get("submissionReference"));
        LocalDate localDate = LocalDateTime.ofInstant(submissionTimestamp.toInstant(), ZoneId.systemDefault()).toLocalDate();
        ccdData.put("applicationSubmittedDate", localDate.toString());
        boolean ihtCompletedOnline = "online".equalsIgnoreCase(probateData.get("ihtForm").asText());
        String ihtFormId = probateData.get(IHT_FORM_ID) == null ? "" : probateData.get(IHT_FORM_ID).asText();
        ccdData.put("ihtFormCompletedOnline", ihtCompletedOnline ? "Yes" : "No");
        ccdData.put(IHT_FORM_ID, ihtCompletedOnline ? IHT_FORM_VALUE_205 : ihtFormId);
        ccdData.put("softStop", "True".equalsIgnoreCase(probateData.get("softStop").asText()) ? "Yes" : "No");
        ccdData.set("registryLocation", registry.get("name"));
        ccdData.put("applicationType", "Personal");

        ccdData.setAll(map(probateData, fieldMap, this::fieldMapper));
        ccdData.setAll(map(probateData, dateMap, this::dateMapper));
        ccdData.setAll(map(probateData, executorMap, this::executorsMapper));
        ccdData.setAll(map(probateData, monetaryValueMap, this::monetaryValueMapper));
        ccdData.setAll(map(probateData, aliasMap, this::aliasesMapper));
        ccdData.setAll(map(probateData, declarationMap, this::declarationMapper));
        ccdData.setAll(map(probateData, legalStatementMap, this::legalStatementMapper));
        ccdData.setAll(map(probateData, addressMap, this::addressMapper));
        ccdData.setAll(map(probateData, documentUploadMap, this::documentUploadMapper));
        return ccdData;
    }

    public Optional<JsonNode> fieldMapper(JsonNode probateData, String fieldName) {
        return Optional.ofNullable(probateData.get(fieldName));
    }

    public Map<String, JsonNode> map(JsonNode probateData, Map<String, String> probateToCCDKeyMap, BiFunction<JsonNode, String, Optional<JsonNode>> function) {
        return probateToCCDKeyMap.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getValue(), function.apply(probateData, entry.getKey())))
                .filter(entry -> entry.getValue().isPresent())
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().get()))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    }

    public Optional<JsonNode> dateMapper(JsonNode probateData, String field) {
        Optional<JsonNode> ret = Optional.empty();
        Optional<JsonNode> date = Optional.ofNullable(probateData.get(field));
        if (date.isPresent()) {
            try {
                ret = date
                        .map(j -> LocalDate.parse(j.asText(), formatter))
                        .map(LocalDate::toString)
                        .map(TextNode::new);

            } catch (DateTimeParseException e) {
                logger.error("Unable to parse date: " + date, e);
            }
        }
        return ret;
    }

    public Optional<JsonNode> executorsMapper(JsonNode probateData, String fieldname) {
        Optional<JsonNode> ret = Optional.empty();
        Optional<JsonNode> executors = Optional.ofNullable(probateData.get(fieldname));
        if (executors.isPresent()) {
            ArrayNode executorsCcdFormat = mapper.createArrayNode();
            executors.get()
                    .elements().forEachRemaining(
                    executor -> mapExecutor(executor).ifPresent(executorsCcdFormat::add)
            );
            ret = Optional.of(executorsCcdFormat);
        }
        return ret;
    }

    public Optional<JsonNode> mapExecutor(JsonNode executor) {
        ObjectNode ccdFormat = mapper.createObjectNode();
        ObjectNode value = mapper.createObjectNode();
        String executorName = executor.get(fullName).asText();

        if (executor.has(isApplying) && executor.get(isApplying).asBoolean()) {
            value.set(applyingExecutorName, new TextNode(executorName.trim()));
            String executorPhoneNumber = executor.get(mobile).asText();
            value.set(applyingExecutorPhoneNumber, new TextNode(executorPhoneNumber.trim()));
            String executorEmail = executor.get(email).asText();
            value.set(applyingExecutorEmail, new TextNode(executorEmail.trim()));
            JsonNode executorAddress = executor.get(address);
            ObjectNode ccdExecutorAddressObject = mapper.createObjectNode();
            ccdExecutorAddressObject.set("AddressLine1", executorAddress);
            value.set(applyingExecutorAddress, ccdExecutorAddressObject);
        } else {
            value.set(notApplyingExecutorName, new TextNode(executorName.trim()));
            String reason = executor.get(notApplyingKey).asText();
            JsonNode mappedReason = new TextNode(reasonMap.get(reason));
            value.set(notApplyingExecutorReason, mappedReason);
        }

        if (executor.has(hasOtherName) && executor.get(hasOtherName).asBoolean()) {
            String executorOtherName = executor.get(currentName).asText();
            value.set(applyingExecutorOtherNames, new TextNode(executorOtherName.trim()));
            if (executor.has(currentNameReason)) {
                String executorOtherNameReason = executor.get(currentNameReason).asText();
                value.set(applyingExecutorOtherNamesReason, new TextNode(executorOtherNameReason.trim()));
            }
        }

        if (executor.has(otherReason) && executor.get(hasOtherName).asBoolean()) {
            String executorOtherReason = executor.get(otherReason).asText();
            value.set(applyingExecutorOtherReason, new TextNode(executorOtherReason.trim()));
        }


        ccdFormat.set(VALUE, value);
        return Optional.of(ccdFormat);
    }

    public Optional<JsonNode> monetaryValueMapper(JsonNode probateData, String fieldName) {
        Optional<JsonNode> ret = Optional.empty();
        Optional<JsonNode> field = Optional.ofNullable(probateData.get(fieldName));

        if (field.isPresent()) {

            try {
                ret = field
                        .map(f -> new BigDecimal(f.asText()))
                        .map(i -> i.multiply(new BigDecimal(100)).setScale(0))
                        .map(String::valueOf)
                        .map(TextNode::new);

            } catch (NumberFormatException e) {
                logger.error("Unable to parse value: " + field, e);
            }
        }
        return ret;
    }

    public Optional<JsonNode> aliasesMapper(JsonNode probateData, String fieldname) {
        Optional<JsonNode> ret = Optional.empty();
        Optional<JsonNode> aliases = Optional.ofNullable(probateData.get(fieldname));
        if (aliases.isPresent()) {
            ArrayNode aliasesCcdFormat = mapper.createArrayNode();

            probateData.get(fieldname)
                    .elements().forEachRemaining(alias -> mapAlias(alias).ifPresent(aliasesCcdFormat::add)
            );

            ret = Optional.of(aliasesCcdFormat);
        }
        return ret;
    }

    public Optional<JsonNode> mapAlias(JsonNode alias) {
        ObjectNode ccdFormat = mapper.createObjectNode();
        ObjectNode value = mapper.createObjectNode();
        value.set("Forenames", alias.get("firstName"));
        value.set("LastName", alias.get("lastName"));
        ccdFormat.set(VALUE, value);
        return Optional.of(ccdFormat);
    }

    public Optional<JsonNode> addressMapper(JsonNode probateData, String fieldname) {
        Optional<JsonNode> ret = Optional.empty();
        Optional<JsonNode> optionalAddress = Optional.ofNullable(probateData.get(fieldname));
        if (optionalAddress.isPresent()) {
            ObjectNode ccdAddressObject = mapper.createObjectNode();
            ccdAddressObject.set("AddressLine1", optionalAddress.get());
            return Optional.of(ccdAddressObject);
        }
        return ret;
    }

    public Optional<JsonNode> declarationMapper(JsonNode probateData, String fieldname) {

        Optional<JsonNode> declaration = Optional.ofNullable(probateData.get(fieldname));
        Optional<JsonNode> ret = Optional.empty();
        if (declaration.isPresent()) {
            ObjectNode ccdDeclaration = mapper.createObjectNode();
            ccdDeclaration.set("confirm", declaration.get().get("confirm"));
            ccdDeclaration.set("confirmItem1", declaration.get().get("confirmItem1"));
            ccdDeclaration.set("confirmItem2", declaration.get().get("confirmItem2"));
            ccdDeclaration.set("confirmItem3", declaration.get().get("confirmItem3"));
            ccdDeclaration.set("requests", declaration.get().get("requests"));
            ccdDeclaration.set("requestsItem1", declaration.get().get("requestsItem1"));
            ccdDeclaration.set("requestsItem2", declaration.get().get("requestsItem2"));
            ccdDeclaration.set("understand", declaration.get().get("understand"));
            ccdDeclaration.set("understandItem1", declaration.get().get("understandItem1"));
            ccdDeclaration.set("understandItem2", declaration.get().get("understandItem2"));
            ccdDeclaration.set("accept", declaration.get().get("accept"));

            return Optional.of(ccdDeclaration);
        }

        return ret;
    }

    public Optional<JsonNode> legalStatementMapper(JsonNode probateData, String fieldname) {

        Optional<JsonNode> legalStatement = Optional.ofNullable(probateData.get(fieldname));
        Optional<JsonNode> ret = Optional.empty();
        if (legalStatement.isPresent()) {
            ObjectNode ccdLegalStatement = mapper.createObjectNode();
            ccdLegalStatement.set(APPLICANT, legalStatement.get().get(APPLICANT));
            ccdLegalStatement.set(DECEASED, legalStatement.get().get(DECEASED));

            if (legalStatement.get().has(DECEASED_OTHER_NAMES)) {
                ccdLegalStatement.set(DECEASED_OTHER_NAMES, legalStatement.get().get(DECEASED_OTHER_NAMES));
            }

            ccdLegalStatement.set(DECEASED, legalStatement.get().get(DECEASED));

            if (legalStatement.get().has(DECEASED_ESTATE_VALUE)) {
                ccdLegalStatement.set(DECEASED_ESTATE_VALUE, legalStatement.get().get(DECEASED_ESTATE_VALUE));
            }

            if (legalStatement.get().has(DECEASED_ESTATE_LAND)) {
                ccdLegalStatement.set(DECEASED_ESTATE_LAND, legalStatement.get().get(DECEASED_ESTATE_LAND));
            }

            if (legalStatement.get().has(EXECUTORS_NOT_APPLYING)) {
                ArrayNode executorsNotApplying = mapper.createArrayNode();
                legalStatement.get().get(EXECUTORS_NOT_APPLYING).elements().forEachRemaining(executor -> mapExecNotApplying(executor).ifPresent(executorsNotApplying::add));
                ccdLegalStatement.set(EXECUTORS_NOT_APPLYING, executorsNotApplying);
            }

            if (legalStatement.get().has(EXECUTORS_APPLYING)) {
                ArrayNode executorsApplying = mapper.createArrayNode();
                legalStatement.get().get(EXECUTORS_APPLYING).elements().forEachRemaining(executorApplying -> mapExecApplying(executorApplying).ifPresent(executorsApplying::add));
                ccdLegalStatement.set(EXECUTORS_APPLYING, executorsApplying);
            }

            ccdLegalStatement.set(INTRO, legalStatement.get().get(INTRO));

            return Optional.of(ccdLegalStatement);
        }

        return ret;
    }

    public Optional<JsonNode> mapExecNotApplying(JsonNode executor) {
        ObjectNode ccdExecutorsNotApplying = mapper.createObjectNode();
        ObjectNode value = mapper.createObjectNode();
        value.set("executor", executor);
        ccdExecutorsNotApplying.set(VALUE, value);
        return Optional.of(ccdExecutorsNotApplying);
    }

    public Optional<JsonNode> mapExecApplying(JsonNode executorApplying) {
        ObjectNode ccdExecutorsApplying = mapper.createObjectNode();
        ObjectNode value = mapper.createObjectNode();
        value.set("name", executorApplying.get("name"));
        value.set("sign", executorApplying.get("sign"));
        ccdExecutorsApplying.set(VALUE, value);
        return Optional.of(ccdExecutorsApplying);
    }

    public JsonNode updatePaymentStatus(PaymentResponse paymentResponse, String ccdEventId, JsonNode ccdToken) {
        ObjectNode event = mapper.createObjectNode();
        event.put("id", ccdEventId);
        event.put("description", "");
        event.put("summary", "Probate application");
        ObjectNode formattedData = mapper.createObjectNode();
        formattedData.set("event", event);
        formattedData.put("ignore_warning", true);
        formattedData.set("event_token", ccdToken);


        ObjectNode probateData = mapper.createObjectNode();
        if ("Success".equalsIgnoreCase(paymentResponse.getStatus())) {
            LocalDate localDate = LocalDateTime.now().toLocalDate();
            probateData.put("applicationSubmittedDate", localDate.toString());
        }
        if (paymentResponse.getAmount() != 0L) {
            ObjectNode paymentNode = mapper.createObjectNode();
            ObjectNode paymentValueNode = mapper.createObjectNode();
            paymentValueNode.put("status", paymentResponse.getStatus());
            addDate(paymentValueNode, paymentResponse.getDateCreated());
            paymentValueNode.put("reference", paymentResponse.getReference());
            paymentValueNode.put("amount", paymentResponse.getAmount().toString());
            paymentValueNode.put("method", paymentResponse.getChannel());
            paymentValueNode.put("transactionId", paymentResponse.getTransactionId());
            paymentValueNode.put("siteId", paymentResponse.getSiteId());
            paymentNode.set(VALUE, paymentValueNode);
            ArrayNode paymentArrayNode = mapper.createArrayNode();
            paymentArrayNode.add(paymentNode);
            probateData.set("payments", paymentArrayNode);
        }
        formattedData.set("data", probateData);
        return formattedData;
    }

    private void addDate(ObjectNode paymentValueNode, String date) {
        String formattedDate = formatDate(date);
        if (StringUtils.isNotBlank(formattedDate)) {
            paymentValueNode.put("date", formattedDate);
        }
    }

    private String formatDate(String originalDateStr) {
        try {
            Date originalDate = originalDateFormat.parse(originalDateStr);
            return newDateFormat.format(originalDate);
        } catch (ParseException pe) {
            logger.error("Error parsing payment date", pe);
        }
        return "";
    }

    public Optional<JsonNode> documentUploadMapper(JsonNode probateData, String fieldname) {
        Optional<JsonNode> ret = Optional.empty();
        Optional<JsonNode> documentUploads = Optional.ofNullable(probateData.get(fieldname));
        if (documentUploads.isPresent()) {
            ArrayNode documentUploadCcdFormat = mapper.createArrayNode();
            documentUploads.get()
                    .elements().forEachRemaining(
                    document -> mapDocument(document).ifPresent(documentUploadCcdFormat::add)
            );
            ret = Optional.of(documentUploadCcdFormat);
        }
        return ret;
    }

    private Optional<JsonNode> mapDocument(JsonNode document) {
        ObjectNode ccdFormat = mapper.createObjectNode();
        ObjectNode value = mapper.createObjectNode();

        String documentUploadType = "deathCertificate";
        value.set(DocumentType, new TextNode(documentUploadType.trim()));
        String documentUploadURL = document.get(url).asText();

        String documentUploadName = document.get(filename).asText();

        ObjectNode docLinkValue = mapper.createObjectNode();
        docLinkValue.set(documentUrl, new TextNode(documentUploadURL.trim()));
        docLinkValue.set(documentBinaryUrl, new TextNode(getBinaryDocumentUploadURL(documentUploadURL.trim())));
        docLinkValue.set(documentFilename, new TextNode(documentUploadName.trim()));

        value.set(DocumentLink, docLinkValue);
        value.set(Comment, new TextNode(documentUploadName.trim()));

        ccdFormat.set(VALUE, value);
        return Optional.of(ccdFormat);
    }

    private String getBinaryDocumentUploadURL(String trim) {
        return trim + "/" + BINARY_URL_SUFFIX;
    }
}

