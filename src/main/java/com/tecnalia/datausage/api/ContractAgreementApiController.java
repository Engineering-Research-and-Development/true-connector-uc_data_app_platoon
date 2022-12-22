package com.tecnalia.datausage.api;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tecnalia.datausage.model.ContractStore;
import com.tecnalia.datausage.service.ContractAgreementService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-04-13T10:06:12.128Z[GMT]")
@RestController
public class ContractAgreementApiController implements ContractAgreementApi {

     @Autowired
    private final ContractAgreementService contractAgreementService;

    private static final Logger log = LoggerFactory.getLogger(ContractAgreementApiController.class);

 /*   private final ObjectMapper objectMapper;

    private final HttpServletRequest request;*/

    @org.springframework.beans.factory.annotation.Autowired
   /* public PolicyApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }*/

    
    public ContractAgreementApiController(ContractAgreementService contractAgreementService ) {
        this.contractAgreementService = contractAgreementService;
       
    }

    public ResponseEntity<String> addContractAgreementUsingPOST(@Parameter(in = ParameterIn.DEFAULT, description = "Contract Agreement", required=true, schema=@Schema()) @Valid @RequestBody String body) {
     /*   String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<String>(objectMapper.readValue("\"\"", String.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<String>(HttpStatus.NOT_IMPLEMENTED);*/
       log.info("PolicyApiController:::body :"+body);
        log.info("PolicyApiController:::: accept????");
        return contractAgreementService.addOrUpdate(body);
    }

    public ResponseEntity<String> deleteContractAgreementUsingDELETE(@Parameter(in = ParameterIn.PATH, description = "", required=true, schema=@Schema()) @PathVariable("contractUuid") String contractUuid) {
      /*  String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<String>(objectMapper.readValue("\"\"", String.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<String>(HttpStatus.NOT_IMPLEMENTED);*/
       return contractAgreementService.deletePolicy(contractUuid);
    }

    public ResponseEntity<List<ContractStore>> getAllContractAgreementsGET() {
     /*   String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<ContractStore>>(objectMapper.readValue("[ {\n  \"contractAsString\" : \"contractAsString\",\n  \"targetId\" : \"targetId\",\n  \"consumerId\" : \"consumerId\",\n  \"providerId\" : \"providerId\",\n  \"contractUuid\" : \"contractUuid\",\n  \"contractId\" : \"contractId\"\n}, {\n  \"contractAsString\" : \"contractAsString\",\n  \"targetId\" : \"targetId\",\n  \"consumerId\" : \"consumerId\",\n  \"providerId\" : \"providerId\",\n  \"contractUuid\" : \"contractUuid\",\n  \"contractId\" : \"contractId\"\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<ContractStore>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<ContractStore>>(HttpStatus.NOT_IMPLEMENTED);*/
     
        return contractAgreementService.getAllOdrlPolicyPersistence();
    }

}
