package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static feign.FeignException.errorStatus;

@Slf4j
public class CcdClientApiErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("CcdClientApi responded with {} - {}", response.status(), response.body());

        switch (response.status()) {
            case 400:
                log.error("throwing ResponseStatusException BAD_REQUEST");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.reason());
            case 404:
                log.error("throwing ResponseStatusException NOT_FOUND");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,  response.reason());
            case 409:
                log.error("throwing ResponseStatusException CONFLICT");
                throw new ResponseStatusException(HttpStatus.CONFLICT, response.reason());
        }

        return errorStatus(methodKey, response);
    }

}
