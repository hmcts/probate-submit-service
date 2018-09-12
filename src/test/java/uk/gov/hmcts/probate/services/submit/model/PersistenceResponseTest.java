package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class PersistenceResponseTest {

    private PersistenceResponse persistenceResponse;

    private JsonNode jsonNode;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        jsonNode = objectMapper.readTree("{\n" +
                "    \"id\": 17\n" +
                "  }");
        persistenceResponse = new PersistenceResponse(jsonNode);
    }

    @Test
    public void getIdAsJsonNode() {
        assertThat(persistenceResponse.getIdAsJsonNode(), is(equalTo(jsonNode.get("id"))));
    }

    @Test
    public void getIdAsLong() {
        assertThat(persistenceResponse.getIdAsLong(), is(equalTo(17L)));
    }
}