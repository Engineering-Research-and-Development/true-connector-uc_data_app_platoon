/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.usagecontrol;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.tecnalia.datausage.utils.HttpUtils;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import io.dataspaceconnector.utils.RuleUtils;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * @author usuario
 */
@Component
@PropertySource("classpath:platoon_datausage_cape.properties")
//@PropertySource("file:/etc/platoon_datausage_cape.properties")
public class PersonalDataEnforcement {
    @Value("${cape.enforce.usage.url}")
    private String enforceConsentsUrl = "";
    @Value("${cape.auth.server.url}")
    private String enforceAuthServerUrl = "";
    @Value("${cape.auth.client.id}")
    private String enforceAuthClientId = "";
    @Value("${cape.auth.client.secret}")
    private String enforceAuthClientSecret = "";
    @Value("${cape.auth.grant.type}")
    private String enforceAuthGrantType = "";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonalDataEnforcement.class);
    
    private final HttpUtils httpUtils;

    public PersonalDataEnforcement(HttpUtils httpUtils)
        throws IllegalArgumentException {

        if (httpUtils == null)
            throw new IllegalArgumentException("The HttpUtils cannot be null.");

        this.httpUtils = httpUtils;
    }

    public String enforce (Permission permission, String providerURI,
            String consumerURI, String targetDataUri, String dataObject) {
        
        JSONArray filteredDataObject = new JSONArray();
        
        String consumerPurpose = getConsumerPurpose(permission, consumerURI);

        JSONArray dataObjectsList = new JSONArray(dataObject);
        for(Object onePersonDataObject: dataObjectsList) {
            JSONObject onePersonDataObjectJson = (JSONObject)onePersonDataObject;
            String userId = getUserId(permission, onePersonDataObjectJson);
            JSONObject filteredOnePersonDataObjectJson = enforceConsents(consumerURI, providerURI,
                            userId, targetDataUri, consumerPurpose, onePersonDataObjectJson);
            filteredDataObject.put(filteredOnePersonDataObjectJson);           
        }
        
        return filteredDataObject.toString();
    }
    
    String getConsumerPurpose(Permission permission, String consumerURI) {
        String consumerPurposeAsString = "";  
        URI pip = RuleUtils.getPipEndpoint(permission.getPreDuty().get(0));
        
        try {
            String encodedConsumerUri =  URLEncoder.encode(consumerURI, StandardCharsets.UTF_8.toString());
            consumerPurposeAsString = httpUtils.sendHttpGetRequest(
                    pip + "?consumerUri="+ encodedConsumerUri);
       } catch (UnsupportedEncodingException | URISyntaxException | RuntimeException e) {
            return consumerPurposeAsString;
        }

        return consumerPurposeAsString;
    }
    
    String getUserId(Permission permission, JSONObject onePersonDataObjectJson) {
        String userId = "";  
        //Get the value of the jsonPath attribute
        Map <String, Object> propsMap = permission.getPreDuty().get(0).getProperties();
        TypedLiteral userIdPathLit = (TypedLiteral)propsMap.get("https://w3id.org/idsa/code/JsonPath");
        String userIdPath = userIdPathLit.getValue();

        //Get the value from the DataObject indicated by the jsonPath attribute
        String onePersonDataObjectStr = onePersonDataObjectJson.toString();
        DocumentContext jsonContext = JsonPath.parse(onePersonDataObjectStr);
        userId = jsonContext.read(userIdPath);        
        return userId;
    }

    public JSONObject enforceConsents(String consumerURI, String providerURI,
                            String userId, String targetDataUri, String consumerPurpose, 
                            JSONObject onePersonDataObjectJson) {
        JSONObject filteredOnePersonDataObjectJson = new JSONObject();
        Map<String,String>params = new HashMap<String, String>();
        params.put("sinkServiceUrl", consumerURI);
        params.put("sourceServiceUrl", providerURI);
        params.put("userId", userId);
        params.put("datasetId", targetDataUri);
        params.put("checkConsentAtOperator", "true");
        params.put("purposeName", consumerPurpose);
        try {
            String filteredOnePersonDataObjectAsString = httpUtils.sendHttpPostRequestWithOAuth(
                        enforceConsentsUrl, params, onePersonDataObjectJson.toString(),
                        enforceAuthServerUrl, enforceAuthClientId, enforceAuthClientSecret, enforceAuthGrantType);
            filteredOnePersonDataObjectJson = new JSONObject(filteredOnePersonDataObjectAsString);
        } catch (URISyntaxException | RuntimeException e) {
        }
        
        return filteredOnePersonDataObjectJson;
    }
}
