package uk.gov.hmcts.probate.services.submit.clients;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AddressParserTest {

  private static final String POST_CODE = "PostCode";
  public static final String ADDRESS_LINE_1 = "AddressLine1";
  private AddressParser addressParser;

  @Parameter
  public String address;

  @Parameter(value = 1)
  public Map<String, String> expected;

  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapper();
    addressParser = new AddressParser(objectMapper);
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"102 Petty France, Westminster, London SW1H 9AJ", ImmutableMap.builder()
            .put(ADDRESS_LINE_1, "102 Petty France, Westminster, London")
            .put(POST_CODE, "SW1H 9AJ")
            .build()},
        {"102 Petty France, Westminster, London SW1H9AJ", ImmutableMap.builder()
            .put(ADDRESS_LINE_1, "102 Petty France, Westminster, London")
            .put(POST_CODE, "SW1H9AJ")
            .build()},
        {"102 Petty France, Westminster, London", ImmutableMap.builder()
            .put(ADDRESS_LINE_1, "102 Petty France, Westminster, London")
            .put(POST_CODE, "")
            .build()},
        {"102 Petty France, Westminster, London,SW1H 9AJ", ImmutableMap.builder()
            .put(ADDRESS_LINE_1, "102 Petty France, Westminster, London")
            .put(POST_CODE, "SW1H 9AJ")
            .build()},
        {"102 Petty France,Westminster,London, SW1H 9AJ", ImmutableMap.builder()
            .put(ADDRESS_LINE_1, "102 Petty France,Westminster,London,")
            .put(POST_CODE, "SW1H 9AJ")
            .build()},
        {"102 Petty France, Westminster, London, SW1H 9AJ", ImmutableMap.builder()
            .put(ADDRESS_LINE_1, "102 Petty France, Westminster, London,")
            .put(POST_CODE, "SW1H 9AJ")
            .build()},
        {"602, Westcliffe Apartments, South Wharf Road, London, Greater London W2 1JB",
            ImmutableMap.builder()
                .put(ADDRESS_LINE_1,
                    "602, Westcliffe Apartments, South Wharf Road, London, Greater London")
                .put(POST_CODE, "W2 1JB")
                .build()},
        {"602, Westcliffe Apartments, South Wharf Road, London, Greater London W21JB",
            ImmutableMap.builder()
                .put(ADDRESS_LINE_1,
                    "602, Westcliffe Apartments, South Wharf Road, London, Greater London")
                .put(POST_CODE, "W21JB")
                .build()}

    });
  }

  @Test
  public void shouldParseAddress() {
    String addressField = "address";
    ObjectNode addressNode = objectMapper.createObjectNode();
    addressNode.set(addressField, new TextNode(address));
    ObjectNode objectNode = addressParser.parse(Optional.ofNullable(addressNode), addressField);

    String addressLine1 =
        objectNode.get(ADDRESS_LINE_1) == null ? "" : objectNode.get(ADDRESS_LINE_1).asText();
    String postCode =
        objectNode.get(POST_CODE) == null ? "" : objectNode.get(POST_CODE).asText();

    assertThat(addressLine1, is(equalTo(expected.get(ADDRESS_LINE_1))));
    assertThat(postCode, is(equalTo(expected.get(POST_CODE))));
  }
}
