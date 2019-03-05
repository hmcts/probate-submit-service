package uk.gov.hmcts.probate.services.submit.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;
import uk.gov.hmcts.probate.services.submit.services.SubmitService;

import javax.ws.rs.core.MediaType;

@RestController
public class SubmitController {

    private SubmitService submitService;

    @Autowired
    public SubmitController(SubmitService submitService) {
        this.submitService = submitService;
    }

    @RequestMapping(path = "/submit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<JsonNode> submit(@RequestBody JsonNode submitData, @RequestHeader("UserId") String userId, @RequestHeader("Authorization") String authorization) {
        return new ResponseEntity<>(submitService.submit(new SubmitData(submitData), userId, authorization), HttpStatus.OK);
    }

    @RequestMapping(path = "/updatePaymentStatus", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON)
    public ResponseEntity<JsonNode> updatePaymentStatus(@RequestBody JsonNode submitData, @RequestHeader("UserId") String userId, @RequestHeader("Authorization") String authorization) {
        return new ResponseEntity<>(submitService.updatePaymentStatus(new SubmitData(submitData), userId, authorization), HttpStatus.OK);
    }

    @RequestMapping(path = "/resubmit/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> resubmit(@PathVariable("id") long sequenceId) {
        return new ResponseEntity<>(submitService.resubmit(sequenceId), HttpStatus.OK);
    }
}
