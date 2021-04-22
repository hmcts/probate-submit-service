package uk.gov.hmcts.probate.services.submit.controllers.v2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseValidationException;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.client.ValidationError;
import uk.gov.hmcts.reform.probate.model.client.ValidationErrorResponse;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


@Slf4j
@ControllerAdvice
public class CaseValidationExceptionHandler {

    @ExceptionHandler(CaseValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handle(CaseValidationException exception) {
        List<ValidationError> validationErrors = exception.getConstraintViolations().stream()
            .map(this::mapValidationError)
            .collect(Collectors.toList());

        ValidationErrorResponse validationErrorResponse = ValidationErrorResponse.builder()
            .errors(validationErrors)
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity(validationErrorResponse, headers, BAD_REQUEST);
    }

    private ValidationError mapValidationError(ConstraintViolation<CaseData> caseDataConstraintViolation) {
        return ValidationError.builder()
            .message(caseDataConstraintViolation.getMessage())
            .field(caseDataConstraintViolation.getPropertyPath().toString())
            .build();
    }
}
