package uk.gov.hmcts.probate.services.submit.controllers.v2;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
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
@Api(tags = {"CasesController"})
@SwaggerDefinition(tags = {@Tag(name = "CasesController", description = "Cases API")})
@RestController
@RequiredArgsConstructor
public class CasesController {

    private final CasesService casesService;
    private final CaveatExpiryService caveatExpiryService;

    @ApiOperation(value = "Get case to CCD using session identifier", notes = "Get case to CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case retrieval from CCD successful"),
        @ApiResponse(code = 400, message = "Case retrieval from CCD successful")
    })
    @GetMapping(path = "/cases/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> getCase(@RequestParam("caseType") CaseType caseType,
                                                      @PathVariable("applicationId") String applicationId) {
        log.info("Retrieving case of caseType: {}", caseType.getName());
        return ResponseEntity.ok(casesService.getCase(applicationId.toLowerCase(), caseType));
    }

    @ApiOperation(value = "Get case to CCD using applicant email", notes = "Get case to CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case retrieval from CCD successful"),
        @ApiResponse(code = 400, message = "Case retrieval from CCD successful")
    })
    @GetMapping(path = "/cases/applicantEmail/{applicantEmail}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> getCaseByApplicantEmail(@RequestParam("caseType") CaseType caseType,
                                                                      @PathVariable("applicantEmail") String applicantEmail) {
        log.info("Retrieving case of caseType: {}", caseType.getName());
        return ResponseEntity.ok(casesService.getCaseByApplicantEmail(applicantEmail, caseType));
    }

    @ApiOperation(value = "Get all cases from CCD using session identifier", notes = "Get all cases from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Cases retrieval from CCD successful"),
        @ApiResponse(code = 400, message = "Cases retrieval from CCD successful")
    })
    @GetMapping(path = "/cases/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ProbateCaseDetails>> getAllCases(@RequestParam("caseType") CaseType caseType) {
        log.info("Retrieving cases of caseType: {}", caseType.getName());
        return ResponseEntity.ok(casesService.getAllCases(caseType));
    }

    @ApiOperation(value = "Get case by Invitation Id from CCD", notes = "Get case bu invite id from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case retrieval from CCD successful"),
        @ApiResponse(code = 400, message = "Case retrieval from CCD unsuccessful")
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
                                                       @RequestBody ProbateCaseDetails caseRequest) {
        log.info("Saving case for case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        return new ResponseEntity(casesService.saveCase(applicationId.toLowerCase(), caseRequest), OK);
    }

    @PostMapping(path = "/cases/initiate", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> initiateCase(@RequestBody ProbateCaseDetails caseRequest) {
        log.info("Saving case for case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        return new ResponseEntity(casesService.initiateCase(caseRequest), OK);
    }


    @PostMapping(path = "/cases/initiate/caseworker", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> initiateCaseAsCaseWorker(@RequestBody ProbateCaseDetails caseRequest) {
        log.info("PRO-7946: ENDPOINT USED /cases/initiate/caseworker");

        log.info("Saving case for case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        return new ResponseEntity(casesService.initiateCaseAsCaseworker(caseRequest), OK);
    }


    @PostMapping(path = "/cases/caseworker/{applicationId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> saveCaseAsCaseworker(@PathVariable("applicationId") String applicationId,
                                                                   @RequestBody ProbateCaseDetails caseRequest) {
        log.info("Saving case for case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        return new ResponseEntity(casesService.saveCaseAsCaseworker(applicationId.toLowerCase(), caseRequest), OK);
    }


    @PostMapping(path = "/cases/{caseId}/caseworker/grantaccess/applicant/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity grantCaseAccessToUser(@PathVariable("caseId") String caseId, @PathVariable("userId") String userId) {
        log.info("PRO-7946: ENDPOINT USED /cases/{caseId}/caseworker/grantaccess/applicant/{userId}");

        log.info("Granting access to case for caseId: {} and userId : {}", caseId, userId);
        casesService.grantAccessForCase(CaseType.GRANT_OF_REPRESENTATION, caseId, userId);
        return new ResponseEntity(OK);
    }


    @PutMapping(path = "/cases/{applicationId}/validations", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<CaseData> validate(@PathVariable("applicationId") String applicationId,
                                             @RequestParam("caseType") CaseType caseType) {
        log.info("CasesController.validate() caseType: {}, applicationId: {}", caseType.getName(), applicationId);
        return new ResponseEntity(casesService.validate(applicationId, caseType), OK);
    }

    @ApiOperation(value = "Get case to CCD using case Id", notes = "Get case to CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case retrieval from CCD successful"),
        @ApiResponse(code = 400, message = "Case retrieval from CCD successful")
    })
    @GetMapping(path = "/cases", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProbateCaseDetails> getCase(@RequestParam(name = "caseId") String caseId) {
        log.info("Retrieving case using application id: {}", caseId.toLowerCase());
        return ResponseEntity.ok(casesService.getCaseById(caseId.toLowerCase()));
    }

    @ApiOperation(value = "Caveat expire from CCD by expiryDate", notes = "Get expired caveats from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Caveat search from CCD successful"),
        @ApiResponse(code = 400, message = "Caveat search from CCD not successful")
    })
    @GetMapping(path = "/cases/caveats/expire", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ProbateCaseDetails>> expireCaveats(@RequestParam("expiryDate") String expiryDate) {
        log.info("Expiring Caveats for expiryDate: {}", expiryDate);
        return ResponseEntity.ok(caveatExpiryService.expireCaveats(expiryDate));
    }

}
