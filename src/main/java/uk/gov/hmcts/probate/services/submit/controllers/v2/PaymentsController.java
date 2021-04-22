package uk.gov.hmcts.probate.services.submit.controllers.v2;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.probate.services.submit.services.PaymentsService;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Api(tags = {"PaymentsController"})
@SwaggerDefinition(tags = {@Tag(name = "PaymentsController", description = "Payments API")})
@RestController
@RequiredArgsConstructor
public class PaymentsController {

    private final PaymentsService paymentsService;

    @PostMapping(path = "/payments/{applicationId}/cases", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> createCase(@PathVariable("applicationId") String applicationId,
                                                         @RequestBody ProbateCaseDetails probateCaseDetails) {
        log.info("Updating payment details for case type: {}", CaseType.getCaseType(probateCaseDetails.getCaseData()));
        return new ResponseEntity(paymentsService.createCase(applicationId.toLowerCase(), probateCaseDetails), OK);
    }

    @PostMapping(path = "/ccd-case-update/{caseId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> updateCaseByCaseId(@PathVariable("caseId") String caseId,
                                                                 @RequestBody ProbateCaseDetails probateCaseDetails) {
        return new ResponseEntity(paymentsService.updateCaseByCaseId(caseId, probateCaseDetails), OK);
    }
}
