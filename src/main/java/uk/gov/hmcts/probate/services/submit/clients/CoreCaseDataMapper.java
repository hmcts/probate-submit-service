package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ccd")
public class CoreCaseDataMapper {

    private final Logger logger = LoggerFactory.getLogger(CoreCaseDataMapper.class);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

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
    @Value("${ccd.probate.notApplyingKey}")
    private String notApplyingKey;
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

    public JsonNode createCcdData(JsonNode probateData, String ccdEventId, JsonNode ccdToken, Calendar submissonTimestamp, JsonNode registryData) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode event = mapper.createObjectNode();
        event.put("id", ccdEventId);
        event.put("description", "");
        event.put("summary", "Probate application");
        ObjectNode formattedData = mapper.createObjectNode();
        formattedData.set("event", event);
        formattedData.put("ignore_warning", true);
        formattedData.set("event_token", ccdToken);
        formattedData.set("data", mapData(probateData, submissonTimestamp, registryData));
        return formattedData;
    }

    public ObjectNode mapData(JsonNode probateData, Calendar submissonTimestamp, JsonNode registryData) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode ccdData = mapper.createObjectNode();
        ccdData.set("applicationID", registryData.get("submissionReference"));
        LocalDate localDate = LocalDateTime.ofInstant(submissonTimestamp.toInstant(), ZoneId.systemDefault()).toLocalDate();
        ccdData.put("applicationSubmittedDate", localDate.toString());
        ccdData.put("deceasedDomicileInEngWales", "live (domicile) permanently in England or Wales".equalsIgnoreCase(probateData.get("deceasedDomicile").asText()) ? "Yes" : "No");
        ccdData.put("ihtFormCompletedOnline", "online".equalsIgnoreCase(probateData.get("ihtForm").asText()) ? "Yes" : "No");
        ccdData.put("softStop", "True".equalsIgnoreCase(probateData.get("softStop").asText()) ? "Yes" : "No");
        ccdData.put("registryLocation", registryData.get("registryName"));
        ccdData.put("applicationType", "Personal");

        ccdData.setAll(map(probateData, fieldMap, this::fieldMapper));
        ccdData.setAll(map(probateData, dateMap, this::dateMapper));
        ccdData.setAll(map(probateData, executorMap, this::executorsMapper));
        ccdData.setAll(map(probateData, monetaryValueMap, this::monetaryValueMapper));
        ccdData.setAll(map(probateData, aliasMap, this::aliasesMapper));
        ccdData.setAll(map(probateData, declarationMap, this::declarationMapper));
        ccdData.setAll(map(probateData, legalStatementMap, this::legalStatementMapper));
        ccdData.setAll(map(probateData, addressMap, this::addressMapper));
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
            ArrayNode executorsCcdFormat = new ObjectMapper().createArrayNode();
            executors.get()
                    .elements().forEachRemaining(
                    executor -> mapExecutor(executor).ifPresent(executorsCcdFormat::add)
            );
            ret = Optional.of(executorsCcdFormat);
        }
        return ret;
    }

    public Optional<JsonNode> mapExecutor(JsonNode executor) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode ccdFormat = mapper.createObjectNode();
        ObjectNode value = mapper.createObjectNode();
        String executorName = executor.get(fullName).asText();
        if (executor.get(isApplying).asBoolean()) {
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

        if (executor.has(hasOtherName) && executor.get(hasOtherName).asBoolean() == true) {
            String executorOtherName = executor.get(currentName).asText();
            value.set(applyingExecutorOtherNames, new TextNode(executorOtherName.trim()));
        }

        ccdFormat.set("value", value);
        return Optional.of(ccdFormat);
    }

    public String getText(JsonNode jsonNode, String fieldname) {
        return Optional.ofNullable(jsonNode.get(fieldname))
                .map(JsonNode::asText)
                .orElse("");
    }

    public Optional<JsonNode> monetaryValueMapper(JsonNode probateData, String fieldName) {
        Optional<JsonNode> ret = Optional.empty();
        Optional<JsonNode> field = Optional.ofNullable(probateData.get(fieldName));

        if (field.isPresent()) {

            try {
                ret = field
                        .map(f -> new BigDecimal(f.asText()))
                        .map(i -> i.multiply(new BigDecimal(100)))
                        .map(BigDecimal::intValue)
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
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode aliasesCcdFormat = mapper.createArrayNode();

            probateData.get(fieldname)
                    .elements().forEachRemaining(alias -> mapAlias(alias).ifPresent(aliasesCcdFormat::add)
            );

            ret = Optional.of(aliasesCcdFormat);
        }
        return ret;
    }

    public Optional<JsonNode> mapAlias(JsonNode alias) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode ccdFormat = mapper.createObjectNode();
        ObjectNode value = mapper.createObjectNode();
        value.set("Forenames", alias.get("firstName"));
        value.set("LastName", alias.get("lastName"));
        ccdFormat.set("value", value);
        return Optional.of(ccdFormat);
    }

    public Optional<JsonNode> addressMapper(JsonNode probateData, String fieldname) {
        Optional<JsonNode> ret = Optional.empty();
        Optional<JsonNode> address = Optional.ofNullable(probateData.get(fieldname));
        if (address.isPresent()) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode ccdAddressObject = mapper.createObjectNode();
            ccdAddressObject.set("AddressLine1", address.get());
            return Optional.of(ccdAddressObject);
        }
        return ret;
    }

    public Optional<JsonNode> declarationMapper(JsonNode probateData, String fieldname) {

        Optional<JsonNode> declaration = Optional.ofNullable(probateData.get(fieldname));
        Optional<JsonNode> ret = Optional.empty();
        if (declaration.isPresent()) {
            ObjectMapper mapper = new ObjectMapper();
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
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode ccdLegalStatement = mapper.createObjectNode();
            ObjectNode value = mapper.createObjectNode();
            ccdLegalStatement.set("applicant", legalStatement.get().get("applicant"));
            ccdLegalStatement.set("deceased", legalStatement.get().get("deceased"));

            if (legalStatement.get().has("deceasedOtherNames")) {
                ccdLegalStatement.set("deceasedOtherNames", legalStatement.get().get("deceasedOtherNames"));
            }

            ccdLegalStatement.set("deceased", legalStatement.get().get("deceased"));

            if (legalStatement.get().has("deceasedEstateValue")) {
                ccdLegalStatement.set("deceasedEstateValue", legalStatement.get().get("deceasedEstateValue"));
            }

            if (legalStatement.get().has("deceasedEstateLand")) {
                ccdLegalStatement.set("deceasedEstateLand", legalStatement.get().get("deceasedEstateLand"));
            }

            if (legalStatement.get().has("executorsNotApplying")) {
                ArrayNode executorsNotApplying = mapper.createArrayNode();
                legalStatement.get().get("executorsNotApplying").elements().forEachRemaining(executor -> mapExecNotApplying(executor).ifPresent(executorsNotApplying::add));
                ccdLegalStatement.set("executorsNotApplying", executorsNotApplying);
            }

            if (legalStatement.get().has("executorsApplying")) {
                ArrayNode executorsApplying = mapper.createArrayNode();
                legalStatement.get().get("executorsApplying").elements().forEachRemaining(executorApplying -> mapExecApplying(executorApplying).ifPresent(executorsApplying::add));
                ccdLegalStatement.set("executorsApplying", executorsApplying);
            }

            ccdLegalStatement.set("intro", legalStatement.get().get("intro"));
            
            return Optional.of(ccdLegalStatement);
        }

        return ret;
    }

    public Optional<JsonNode> mapExecNotApplying(JsonNode executor) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode ccdExecutorsNotApplying = mapper.createObjectNode();
        ObjectNode value = mapper.createObjectNode();
        value.set("executor", executor);
        ccdExecutorsNotApplying.set("value", value);
        return Optional.of(ccdExecutorsNotApplying);
    }

    public Optional<JsonNode> mapExecApplying(JsonNode executorApplying) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode ccdExecutorsApplying = mapper.createObjectNode();
        ObjectNode value = mapper.createObjectNode();
        value.set("name", executorApplying.get("name"));
        value.set("sign", executorApplying.get("sign"));
        ccdExecutorsApplying.set("value", value);
        return Optional.of(ccdExecutorsApplying);
    }


}
