package uk.gov.hmcts.probate.services.submit.controllers.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.probate.services.submit.services.CasesService;
import uk.gov.hmcts.probate.services.submit.services.CaveatExpiryService;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Tag(name = "CasesController", description = "Cases API")
@RestController
@RequiredArgsConstructor
public class CasesController {

    private final CasesService casesService;
    private final CaveatExpiryService caveatExpiryService;

    @Operation(summary = "Get case to CCD using session identifier", description = "Get case to CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case retrieval from CCD successful"),
        @ApiResponse(responseCode = "400", description = "Case retrieval from CCD successful")
    })
    @GetMapping(path = "/cases/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> getCase(@RequestParam("caseType") CaseType caseType,
                                                      @PathVariable("applicationId") String applicationId) {
        log.info("Retrieving case of caseType: {}", caseType.getName());
        return ResponseEntity.ok(casesService.getCase(applicationId.toLowerCase(), caseType));
    }

    @Operation(summary = "Get case to CCD using case Id", description = "Get case to CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case retrieval from CCD successful"),
        @ApiResponse(responseCode = "400", description = "Case retrieval from CCD successful")
    })
    @GetMapping(path = "/cases", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> getCase(@RequestParam(name = "caseId") String caseId) {
        log.info("Retrieving case using application id: {}", caseId.toLowerCase());
        return ResponseEntity.ok(casesService.getCaseById(caseId.toLowerCase()));
    }

    @Operation(summary = "Get case to CCD using applicant email", description = "Get case to CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case retrieval from CCD successful"),
        @ApiResponse(responseCode = "400", description = "Case retrieval from CCD successful")
    })
    @GetMapping(path = "/cases/applicantEmail/{applicantEmail}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> getCaseByApplicantEmail(@RequestParam("caseType") CaseType caseType,
                                                                      @PathVariable("applicantEmail")
                                                                          String applicantEmail) {
        log.info("Retrieving case of caseType: {}", caseType.getName());
        return ResponseEntity.ok(casesService.getCaseByApplicantEmail(applicantEmail, caseType));
    }

    @Operation(summary = "Get all cases from CCD using session identifier", description = "Get all cases from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cases retrieval from CCD successful"),
        @ApiResponse(responseCode = "400", description = "Cases retrieval from CCD successful")
    })
    @GetMapping(path = "/cases/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ProbateCaseDetails>> getAllCases(@RequestParam("caseType") CaseType caseType) {
        log.info("Retrieving cases of caseType: {}", caseType.getName());
        return ResponseEntity.ok(casesService.getAllCases(caseType));
    }

    @Operation(summary = "Get case by Invitation Id from CCD", description = "Get case bu invite id from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case retrieval from CCD successful"),
        @ApiResponse(responseCode = "400", description = "Case retrieval from CCD unsuccessful")
    })
    @GetMapping(path = "/cases/invitation/{invitationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> getCaseByInvitationId(@RequestParam("caseType") CaseType caseType,
                                                                    @PathVariable("invitationId") String invitationId) {
        log.info("Retrieving case of caseType: {}", caseType.getName());
        return ResponseEntity.ok(casesService.getCaseByInvitationId(invitationId, caseType));
    }

    @PostMapping(path = "/cases/{applicationId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> saveCase(@PathVariable("applicationId") String applicationId,
                                                       @RequestParam(name = "eventDescription",
                                                          defaultValue = "Probate Application") String eventDescription,
                                                       @RequestBody ProbateCaseDetails caseRequest) {
        log.info("Saving case for case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        return new ResponseEntity(casesService.saveCase(applicationId.toLowerCase(),
            caseRequest, eventDescription), OK);
    }

    @PostMapping(path = "/cases/initiate", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> initiateCase(@RequestBody ProbateCaseDetails caseRequest) {
        log.info("Initiate case for case type : {}", caseRequest.getCaseData().getClass().getSimpleName());
        return new ResponseEntity(casesService.initiateCase(caseRequest), OK);
    }


    @PostMapping(path = "/cases/caseworker/{applicationId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> saveCaseAsCaseworker(@PathVariable("applicationId") String applicationId,
                                                                   @RequestBody ProbateCaseDetails caseRequest) {
        log.info("Saving case for caseworker case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        return new ResponseEntity(casesService.saveCaseAsCaseworker(applicationId.toLowerCase(), caseRequest), OK);
    }


    @PutMapping(path = "/cases/{applicationId}/validations", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<CaseData> validate(@PathVariable("applicationId") String applicationId,
                                             @RequestParam("caseType") CaseType caseType) {
        log.info("CasesController.validate() caseType: {}, applicationId: {}", caseType.getName(), applicationId);
        return new ResponseEntity(casesService.validate(applicationId, caseType), OK);
    }

    @Operation(summary = "Caveat expire from CCD by expiryDate", description = "Get expired caveats from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Caveat search from CCD successful"),
        @ApiResponse(responseCode = "400", description = "Caveat search from CCD not successful")
    })
    @GetMapping(path = "/cases/caveats/expire", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ProbateCaseDetails>> expireCaveats(@RequestParam("expiryDate") String expiryDate) {
        log.info("Expiring Caveats for expiryDate: {}", expiryDate);
        return ResponseEntity.ok(caveatExpiryService.expireCaveats(expiryDate));
    }

}
