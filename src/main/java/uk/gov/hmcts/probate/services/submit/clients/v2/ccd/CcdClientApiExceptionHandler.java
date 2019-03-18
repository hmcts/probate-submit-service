package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.probate.model.client.CcdClientApiError;

@Slf4j
@ControllerAdvice
public class CcdClientApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CcdClientApiException.class)
    public ResponseEntity<CcdClientApiError> handleCcdClientApiException(final CcdClientApiException exception){
        return new ResponseEntity<>(
                exception.getCcdClientApiError(),
                exception.getStatus()
        );
    }
}


