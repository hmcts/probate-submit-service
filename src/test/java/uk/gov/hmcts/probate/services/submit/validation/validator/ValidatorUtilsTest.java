package uk.gov.hmcts.probate.services.submit.validation.validator;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;


public class ValidatorUtilsTest {

    @Test
    public void shouldReturnTrueIfAllValuesNotNull() {
        Boolean result = ValidatorUtils.allValuesNotNull("blah", "blah");
        Assertions.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfaValuesNull() {
        Boolean result = ValidatorUtils.allValuesNotNull("blah", null);
        Assertions.assertFalse(result);
    }


}