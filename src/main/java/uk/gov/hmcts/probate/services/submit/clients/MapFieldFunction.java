/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

@FunctionalInterface
public interface MapFieldFunction {
    public Optional<String> map(JsonNode node, String field);

}
