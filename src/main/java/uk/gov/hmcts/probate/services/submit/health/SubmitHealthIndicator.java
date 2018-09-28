package uk.gov.hmcts.probate.services.submit.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class SubmitHealthIndicator implements HealthIndicator {

	private final static String EXCEPTION_KEY = "exception";
	private final static String MESSAGE_KEY = "message";
    private final static String URL_KEY = "url";

    private final String url;
    private RestTemplate restTemplate;

    @Override
    public Health health() {
    	ResponseEntity<String> responseEntity;

        try {
            responseEntity = restTemplate.getForEntity(url + "/health", String.class);

        } catch (ResourceAccessException rae) {
            log.error(rae.getMessage(), rae);
            return getHealthWithDownStatus(url, rae.getMessage(), "ResourceAccessException");
        } catch (HttpStatusCodeException hsce) {
            log.error(hsce.getMessage(), hsce);
            return getHealthWithDownStatus(url, hsce.getMessage(),
                    "HttpStatusCodeException - HTTP Status: " + hsce.getStatusCode().value());
        } catch (UnknownHttpStatusCodeException uhsce) {
            log.error(uhsce.getMessage(), uhsce);
            return getHealthWithDownStatus(url, uhsce.getMessage(), "UnknownHttpStatusCodeException - " + uhsce.getStatusText());
        }

        if (responseEntity != null && !responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return getHealthWithDownStatus(url, "HTTP Status code not 200", "HTTP Status: " + responseEntity.getStatusCodeValue());
        }

        return getHealthWithUpStatus(url);

    }

    private Health getHealthWithUpStatus(String url) {
        return Health.up()
                .withDetail(URL_KEY, url)
                .withDetail(MESSAGE_KEY, "HTTP Status OK")
                .build();
    }

    private Health getHealthWithDownStatus(String url, String message, String status) {
        return Health.down()
                .withDetail(URL_KEY, url)
                .withDetail(MESSAGE_KEY, message)
                .withDetail(EXCEPTION_KEY, status)
                .build();
    }
}
