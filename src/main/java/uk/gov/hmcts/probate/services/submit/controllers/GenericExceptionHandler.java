package uk.gov.hmcts.probate.services.submit.controllers;

import com.fasterxml.jackson.core.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.probate.services.submit.model.ParsingSubmitException;


@ControllerAdvice
@RestController
public class GenericExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler({RestClientException.class})
    @ResponseStatus(value = HttpStatus.BAD_GATEWAY, reason = "Could not persist submitted application")
    public void submissionPersistFailure(Exception ex) {
        logException(ex);
    }


    @ExceptionHandler({JsonParseException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Error while processing the received payload")
    public void jsonTransformationFailure(Exception ex) {
        logException(ex);
    }

    private void logException(Exception ex) {
        logger.error(ex.getMessage(), ex);
    }
}
