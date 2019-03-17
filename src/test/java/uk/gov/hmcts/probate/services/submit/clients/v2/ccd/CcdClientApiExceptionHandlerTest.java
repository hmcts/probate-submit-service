package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CcdClientApiExceptionHandlerTest {

    private CcdClientApiExceptionHandler exceptionHandler = new CcdClientApiExceptionHandler();

    @Test
    void handleCcdClientApiExceptionReturnsResponseStatus500() throws IOException {

        ResponseEntity responseEntity = exceptionHandler
                .handleCcdClientApiException(new CcdClientApiException("{\"test\": \"test\"}", HttpStatus.INTERNAL_SERVER_ERROR));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(500);
    }

    @Test
    void handleCcdClientApiExceptionReturnsResponseStatus400() throws IOException {

        ResponseEntity responseEntity = exceptionHandler
                .handleCcdClientApiException(new CcdClientApiException("{\"test\": \"test\"}", HttpStatus.BAD_REQUEST));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void handleCcdClientApiExceptionWithBlankMessage() throws IOException {

        ResponseEntity responseEntity = exceptionHandler
                .handleCcdClientApiException(new CcdClientApiException("", HttpStatus.BAD_REQUEST));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(400);
    }
}