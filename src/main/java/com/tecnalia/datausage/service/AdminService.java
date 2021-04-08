/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.service;



import com.tecnalia.datausage.model.AccessStore;
import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.Prohibition;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;


import java.util.List;
import java.util.Optional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.tecnalia.datausage.model.ContractStore;
import com.tecnalia.datausage.model.RuleStore;
import com.tecnalia.datausage.repository.AccessRepository;
import com.tecnalia.datausage.repository.ContractRepository;


import com.tecnalia.datausage.repository.RuleRepository;
import java.io.IOException;
import java.util.UUID;
import javax.transaction.Transactional;
import org.eclipse.rdf4j.rio.RDFFormat;

/**
 *
 * @author root
 */


@Service
//@Component
public class AdminService {
    
     private static final Logger log = LoggerFactory.getLogger(AdminService.class);
   
    @Autowired
    private  AccessRepository accessRepository;
   

    
  @Autowired
    public AdminService(AccessRepository accessRepository) throws IllegalArgumentException {
  // public PolicyService(OdrlPolicyRepo repository) throws IllegalArgumentException {
        
        if (accessRepository == null)
            throw new IllegalArgumentException("The Contract Repo cannot be null.");       

        this.accessRepository = accessRepository;
        
    }

  
    public ResponseEntity<String> getAccess(String consumerUri,String targetUri)  {
        

        AccessStore accessStore = null; 
        try{
          
            Optional<AccessStore> bCheckExistsAccess = this.accessRepository.findByConsumerUriAndTargetUri(consumerUri,targetUri);
            
          
            if (bCheckExistsAccess.isPresent()) {
               
               
                
                log.info("PolicyService:::policyId :"+bCheckExistsAccess.isPresent());
                accessStore = (AccessStore)bCheckExistsAccess.get();
                log.info("PolicyService:::numAccess :"+accessStore.getNumAccess());
                      
                
               
            }
            return new ResponseEntity<String>(String.valueOf(accessStore.getNumAccess()), HttpStatus.OK);
        }
        catch(Exception e){
               System.out.println(e.getMessage());
              
              return new ResponseEntity<String>("-1", HttpStatus.NOT_FOUND);
            
        }
        
     
        
         


        
        
    
    }
    
   
    
}
