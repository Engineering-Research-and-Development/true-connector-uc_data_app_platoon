/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dataspaceconnector.services.usagecontrol;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;

import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Rule;
import io.dataspaceconnector.exceptions.PolicyRestrictionException;
import io.dataspaceconnector.model.TimeInterval;
import io.dataspaceconnector.utils.ErrorMessages;
import io.dataspaceconnector.utils.RuleUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

//TECNALIA-ICT-OPTIMA: Remove and add new methods.
/**
 * This class provides policy pattern recognition and calls the {@link
 * PolicyInformationService} on data
 * request or access. Refers to the ids policy decision point (PDP).
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class RuleValidator {

    /**
     * Policy execution point.
     */
    private final @NonNull PolicyExecutionService executionService;

    /**
     * Policy information point.
     */
    private final @NonNull PolicyInformationService informationService;

    //TECNALIA-ICT-OPTIMA: Different input parameters: 
    // - consumerUri instead of issuerConnector
    // - created: ContractAgreement start date
     /**
     * Validates the data access for a given rule.
     *
     * @param pattern         The recognized policy pattern.
     * @param rule            The ids rule.
     * @param target          The requested/accessed element.
     * @param consumerUri     The URI of the consumer connector.
     * @param created         The start date of the ContractAgreement.
     * @throws PolicyRestrictionException If a policy restriction was detected.
     */
    void validatePolicy(final PolicyPattern pattern, final Rule rule, final String target,
                        final String consumerURI, final Date created) throws PolicyRestrictionException {
        //TECNALIA-ICT-OPTIMA: Remove and add policy patterns.
        switch (pattern) {
            case PROVIDE_ACCESS:
                break;
            case USAGE_DURING_INTERVAL:
                validateInterval(rule);
                break;
            case DURATION_USAGE:
                //TECNALIA-ICT-OPTIMA: Add created (ContractAgreement start date) input parameter
                validateDuration(rule, target, created);
                break;
            case USAGE_LOGGING:
                executionService.logDataAccess(target);
                break;
            case N_TIMES_USAGE:
                //TECNALIA-ICT-OPTIMA: Add consumerURI input parameter
                validateAccessNumber(rule, target, consumerURI);
                break;
            case ROLE_RESTRICTED_USAGE:
                validateRole(rule, consumerURI);
                break;
            case PURPOSE_RESTRICTED_USAGE:
                validatePurpose(rule, consumerURI);
                break;
            case PERSONAL_DATA:
                break;
            case PROHIBIT_ACCESS:
                throw new PolicyRestrictionException(ErrorMessages.NOT_ALLOWED);
            default:
                if (log.isDebugEnabled()) {
                    log.debug("No pattern detected. [target=({})]", target);
                }
                throw new PolicyRestrictionException(ErrorMessages.POLICY_RESTRICTION);
        }
    }

    /**
     * Checks if the requested data access is in the allowed time interval.
     *
     * @param rule The ids rule.
     * @throws PolicyRestrictionException If the policy could not be read or a restriction is
     *                                    detected.
     */
    private void validateInterval(final Rule rule) throws PolicyRestrictionException {
        TimeInterval timeInterval;
        try {
            timeInterval = RuleUtils.getTimeInterval(rule);
        } catch (ParseException e) {
            if (log.isWarnEnabled()) {
                log.warn("Could not read time interval. [exception=({})]", e.getMessage());
            }
            throw new PolicyRestrictionException(ErrorMessages.DATA_ACCESS_INVALID_INTERVAL, e);
        }

        final var current = RuleUtils.getCurrentDate();
        if (!current.isAfter(timeInterval.getStart()) || !current.isBefore(timeInterval.getEnd())) {
            if (log.isWarnEnabled()) {
                log.warn("Invalid time interval. [timeInterval=({})]", timeInterval);
            }
            throw new PolicyRestrictionException(ErrorMessages.DATA_ACCESS_INVALID_INTERVAL);
        }
    }

    //TECNALIA-ICT-OPTIMA: New input parameters: 
    // - created: ContractAgreement start date
    /**
     * Adds a duration to a given date and checks if the duration has already been exceeded.
     *
     * @param rule    The ids rule.
     * @param target  The accessed element.
     * @param created The start date of the ContractAgreement.
     * @throws PolicyRestrictionException If the policy could not be read or a restriction is
     *                                    detected.
     */
    private void validateDuration(final Rule rule, final String target, final Date created)
            throws PolicyRestrictionException {

        final javax.xml.datatype.Duration duration;
        try {
            duration = RuleUtils.getDuration(rule);
        } catch (DatatypeConfigurationException e) {
            if (log.isWarnEnabled()) {
                log.warn("Could not read duration. [target=({}), exception=({})]",
                        target, e.getMessage(), e);
            }
            throw new PolicyRestrictionException(ErrorMessages.DATA_ACCESS_INVALID_INTERVAL, e);
        }

        if (duration == null) {
            if (log.isWarnEnabled()) {
                log.warn("Duration is null. [target=({})]", target);
            }
            throw new PolicyRestrictionException(ErrorMessages.DATA_ACCESS_INVALID_INTERVAL);
        }

        final var maxTime = RuleUtils.getCalculatedDate(created, duration);
        final var validDate = RuleUtils.checkDate(new Date(), maxTime);

        if (!validDate) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid date time. [target=({})]", target);
            }
            throw new PolicyRestrictionException(ErrorMessages.DATA_ACCESS_INVALID_INTERVAL);
        }
    }

    //TECNALIA-ICT-OPTIMA: New input parameters: 
    // - consumerURI: The URI of the consumer connector.
     /**
     * Checks whether the maximum number of accesses has already been reached.
     *
     * @param rule        The ids rule.
     * @param target      The accessed element.
     * @param consumerUri The URI of the consumer connector.
     * @throws PolicyRestrictionException If the access number has been reached.
     */
    private void validateAccessNumber(final Rule rule, final String target, final String consumerURI)
            throws PolicyRestrictionException {
        final var max = RuleUtils.getMaxAccess(rule);
        //final var endpoint = PolicyUtils.getPipEndpoint(rule);
        // NOTE: might be used later
        final var pipEndpoint = RuleUtils.getPipEndpoint(rule);

        final var accessed = informationService.getAccessNumber(pipEndpoint, target, consumerURI);
        
        if (accessed >= max) {
            if (log.isDebugEnabled()) {
                log.debug("Access number reached. [target=({})]", target);
            }
            throw new PolicyRestrictionException(ErrorMessages.DATA_ACCESS_NUMBER_REACHED);
        }
    }
    

    //TECNALIA-ICT-OPTIMA: New method. 
    /**
     * Checks whether the consumer's Role is allowed to access the data.								 
     *
     * @param rule        The ids rule.
     * @param consumerURI The consumer URI.
     * @throws PolicyRestrictionException If the role is not allowed to access the data.
     */
    public void validateRole(final Rule rule, String consumerURI)
            throws PolicyRestrictionException {
        URI allowedRoleURI = RuleUtils.getAllowedRole(rule);
        if (allowedRoleURI == null) {
            if (log.isWarnEnabled()) {
                log.warn("Role is null. [rule=({})]", rule);
            }
            throw new PolicyRestrictionException(ErrorMessages.DATA_ACCESS_INVALID_ROLE);
        }
        String allowedRoleAsString = allowedRoleURI.toString();
        String consumerRoleAsString = "";
        URI pipEndpoint = RuleUtils.getPipEndpoint(rule);
        
       try {
            String encodedConsumerUri =  URLEncoder.encode(consumerURI, StandardCharsets.UTF_8.toString());
            consumerRoleAsString = informationService.getRemoteInfo(
                    pipEndpoint + "?consumerUri="+ encodedConsumerUri);
        } catch (UnsupportedEncodingException e) {
        }

       if (!allowedRoleAsString.equals(consumerRoleAsString)) {
            throw new PolicyRestrictionException(ErrorMessages.DATA_ACCESS_INVALID_ROLE);
       }
    }

    //TECNALIA-ICT-OPTIMA: New method. 
     /**
     * Checks whether it is allowed to access the data with the consumer's Purpose.
     *
     * @param rule        The ids rule.
     * @param consumerURI The consumer URI.
     * @throws PolicyRestrictionException If it is not allowed to access the data with such Purpose.
     */
    public void validatePurpose(final Rule rule, String consumerURI)
            throws PolicyRestrictionException {
        URI allowedPurposeURI = RuleUtils.getAllowedPurpose(rule);
        if (allowedPurposeURI == null) {
            if (log.isWarnEnabled()) {
                log.warn("Purpose is null. [rule=({})]", rule);
            }
            throw new PolicyRestrictionException(ErrorMessages.DATA_ACCESS_INVALID_PURPOSE);
        }
        String allowedRoleAsString = allowedPurposeURI.toString();
        String consumerPurposeAsString = "";
        URI pipEndpoint = RuleUtils.getPipEndpoint(rule);
        
        try {
            String encodedConsumerUri =  URLEncoder.encode(consumerURI, StandardCharsets.UTF_8.toString());
            consumerPurposeAsString = informationService.getRemoteInfo(
                    pipEndpoint + "?consumerUri="+ encodedConsumerUri);
        } catch (UnsupportedEncodingException e) {
        }

       if (!allowedRoleAsString.equals(consumerPurposeAsString)) {
            throw new PolicyRestrictionException(ErrorMessages.DATA_ACCESS_INVALID_PURPOSE);
       }
    }
    

}
