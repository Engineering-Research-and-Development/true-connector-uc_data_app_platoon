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

import de.fraunhofer.iais.eis.Rule;
import de.fraunhofer.iais.eis.SecurityProfile;
import io.dataspaceconnector.exceptions.PolicyRestrictionException;
import io.dataspaceconnector.utils.RuleUtils;

import java.util.*;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * A {@link PolicyVerifier} implementation that checks whether data provision should be allowed.
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class DataProvisionVerifier implements PolicyVerifier<VerificationInput> {

    /**
     * The policy execution point.
     */
    private final @NonNull RuleValidator ruleValidator;

    //TECNALIA-ICT-OPTIMA: Different input parameters: 
    // - consumerUri instead of issuerConnector
    // - created: ContractAgreement start date
    // - rules instead of agreement
    /**
     * Policy check on data provision on provider side.
     *
     * @param target          The requested element.
     * @param consumerUri     The URI of the consumer connector.
     * @param created         The start date of the ContractAgreement.
     * @param rules           The ids rule list.
     * @param securityProfile
     * @throws PolicyRestrictionException If a policy restriction has been detected.
     */
    public void checkPolicy(final String target,
                            final String consumerUri,
                            final Date created,
                            final ArrayList<Rule> rules, Optional<SecurityProfile> securityProfile) throws PolicyRestrictionException {
        //TECNALIA-ICT-OPTIMA: Use variable instead of the ConnectorConfigurator
        final var ignoreUnsupportedPatterns = false;
        //TECNALIA-ICT-OPTIMA: Remove some Policy Patterns
      /*  final var patternsToCheck = Arrays.asList(
                PolicyPattern.PROVIDE_ACCESS,
                PolicyPattern.PROHIBIT_ACCESS,
                PolicyPattern.USAGE_DURING_INTERVAL);*

       */
        final var patternsToCheck = Arrays.asList(
                PolicyPattern.PROVIDE_ACCESS,
                PolicyPattern.PROHIBIT_ACCESS,
                PolicyPattern.USAGE_DURING_INTERVAL,
                PolicyPattern.USAGE_UNTIL_DELETION,
                PolicyPattern.CONNECTOR_RESTRICTED_USAGE,
                PolicyPattern.SECURITY_PROFILE_RESTRICTED_USAGE);
        try {
            checkForAccess(patternsToCheck, target, consumerUri, created, rules,securityProfile);
        } catch (PolicyRestrictionException exception) {
            // Unknown patterns cause an exception. Ignore if unsupported patterns are allowed.
            if (!ignoreUnsupportedPatterns) {
                throw exception;
            }
        }
    }

    //TECNALIA-ICT-OPTIMA: Different input parameters: 
    // - consumerUri instead of issuerConnector
    // - created: ContractAgreement start date
    // - rules instead of agreement
    /**
     * Checks the contract content for data access (on provider side).
     *
     * @param patterns    List of patterns that should be enforced.
     * @param target      The requested element.
     * @param consumerUri The URI of the consumer connector.
     * @param created     The start date of the ContractAgreement.
     * @param rules       The ids rule list.
     * @throws PolicyRestrictionException If a policy restriction has been detected.
     */
    public void checkForAccess(final List<PolicyPattern> patterns,
                               final String target, final String consumerUri, final Date created,
                               final ArrayList<Rule> rules,final Optional<SecurityProfile> profile)
            throws PolicyRestrictionException {

        // Check the policy of each rule.
        for (final var rule : rules) {
            final var pattern = RuleUtils.getPatternByRule(rule);
            // Enforce only a set of patterns.
            if (patterns.contains(pattern)) {
                ruleValidator.validatePolicy(pattern, rule, target, consumerUri, created, profile);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VerificationResult verify(final VerificationInput input) {
        try {
            this.checkPolicy(input.getTarget(), input.getConsumerUri(), input.getCreated(), input.getRules(), input.getSecurityProfile());
            return VerificationResult.ALLOWED;
        } catch (PolicyRestrictionException exception) {
            if (log.isDebugEnabled()) {
                log.debug("Data access denied. [input=({})]", input, exception);
            }
            return VerificationResult.DENIED;
        }
    }
}
