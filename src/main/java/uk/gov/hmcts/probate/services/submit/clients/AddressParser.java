package uk.gov.hmcts.probate.services.submit.clients;

import static com.microsoft.applicationinsights.web.dependencies.apachecommons.lang3.StringUtils.SPACE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.microsoft.applicationinsights.web.dependencies.apachecommons.lang3.StringUtils;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddressParser {

  private static final int POSTCODE_MAX_LENGTH = 8;

  private static final int POSTCODE_MIN_LENGTH = 5;

  private static final String COMMA = ",";
  private static final String ADDRESS_LINE_1_KEY = "AddressLine1";
  private static final String POSTCODE_KEY = "PostCode";

  private final ObjectMapper objectMapper;

  @Autowired
  public AddressParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ObjectNode parse(Optional<JsonNode> jsonNodeOptional, String fieldName) {
    if (!jsonNodeOptional.isPresent()) {
      return null;
    }
    String address = jsonNodeOptional.get().get(fieldName).asText();
    String postCode = substringAfterLast(address, SPACE);
    String addressLine1 = substringBeforeLast(address, SPACE);
    if (postCode.length() < POSTCODE_MIN_LENGTH) {
      postCode = String.join(SPACE, substringAfterLast(addressLine1, SPACE), postCode);
      addressLine1 = substringBeforeLast(addressLine1, SPACE);
    }
    if (!isPostCode(postCode)) {
      return getAddressMapWithCommaSeparatorsOrDefault(address);
    }
    return createObjectNodeWithAddressLine1AndPostCode(addressLine1, postCode);
  }

  private ObjectNode getAddressMapWithCommaSeparatorsOrDefault(String address) {
    String postCodeAfterLastComma = substringAfterLast(address, COMMA);
    String addressLine1BeforeLastComma = substringBeforeLast(address, COMMA);
    if (isPostCode(postCodeAfterLastComma)) {
      return createObjectNodeWithAddressLine1AndPostCode(addressLine1BeforeLastComma,
          postCodeAfterLastComma);
    }
    return createObjectNodeWithAddressLine1(address);
  }

  private boolean isPostCode(String postCode) {
    return StringUtils.isAlphanumericSpace(postCode) && containsDigits(postCode)
        && postCode.length() <= POSTCODE_MAX_LENGTH && postCode.length() >= POSTCODE_MIN_LENGTH;
  }

  private boolean containsDigits(String str) {
    return str.chars().anyMatch(Character::isDigit);
  }

  private String substringAfterLast(String str, String separator) {
    return StringUtils.substringAfterLast(str, separator).trim();
  }

  private String substringBeforeLast(String str, String separator) {
    return StringUtils.substringBeforeLast(str, separator).trim();
  }

  private ObjectNode createObjectNodeWithAddressLine1(String addressLine1) {
    ObjectNode objectNode = objectMapper.createObjectNode();
    objectNode.set(ADDRESS_LINE_1_KEY, new TextNode(addressLine1));
    return objectNode;
  }

  private ObjectNode createObjectNodeWithAddressLine1AndPostCode(String addressLine1,
      String postCode) {
    ObjectNode objectNode = objectMapper.createObjectNode();
    objectNode.set(ADDRESS_LINE_1_KEY, new TextNode(addressLine1));
    objectNode.set(POSTCODE_KEY, new TextNode(postCode));
    return objectNode;
  }
}
