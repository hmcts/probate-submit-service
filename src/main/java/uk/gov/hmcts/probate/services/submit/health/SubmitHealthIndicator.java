package uk.gov.hmcts.probate.services.submit.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

public class SubmitHealthIndicator implements HealthIndicator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubmitHealthIndicator.class);

    private static final String EXCEPTION_KEY = "exception";
    private static final String MESSAGE_KEY = "message";
    private static final String URL_KEY = "url";

    private final String url;
    private RestTemplate restTemplate;

    public SubmitHealthIndicator(String servicesCcdBaseUrl, RestTemplate restTemplate) {
        this.url = servicesCcdBaseUrl;
        this.restTemplate = restTemplate;
    }

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
            return getHealthWithDownStatus(url, uhsce.getMessage(),
                "UnknownHttpStatusCodeException - " + uhsce.getStatusText());
        }

        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return getHealthWithDownStatus(url, "HTTP Status code not 200",
                "HTTP Status: " + responseEntity.getStatusCodeValue());
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
