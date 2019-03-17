package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;

@Slf4j
@ControllerAdvice
public class CcdClientApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CcdClientApiException.class)
    public ResponseEntity<CcdClientApiErrorResponse> handleCcdClientApiException(final CcdClientApiException exception) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(exception.getMessage());
        return new ResponseEntity<>(new CcdClientApiErrorResponse(json), exception.getStatus());
    }

    class CcdClientApiErrorResponse {

        private JsonNode ccdClientApiError;

        CcdClientApiErrorResponse(JsonNode apiError){
            this.ccdClientApiError=apiError;
        }

        public JsonNode getCcdClientApiError() {
            return ccdClientApiError;
        }
    }
}


