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
package io.dataspaceconnector.utils;

import java.net.URI;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.ConstraintImpl;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.Prohibition;
import de.fraunhofer.iais.eis.Rule;
import io.dataspaceconnector.exceptions.InvalidInputException;
import io.dataspaceconnector.model.TimeInterval;
import io.dataspaceconnector.services.usagecontrol.PolicyPattern;
import lombok.extern.log4j.Log4j2;

//TECNALIA-ICT-OPTIMA: Remove unused methods
/**
 * Contains utility methods for validating the content of ids rules.
 */
@Log4j2
public final class RuleUtils {

    /**
     * Constructor without params.
     */
    private RuleUtils() {
        // not used
    }

    /**
     * Read the properties of an ids rule to automatically recognize the policy pattern.
     *
     * @param rule The ids rule.
     * @return The recognized policy pattern.
     */
    public static PolicyPattern getPatternByRule(final Rule rule) {
        PolicyPattern detectedPattern = null;

        if (rule instanceof Prohibition) {
            detectedPattern = PolicyPattern.PROHIBIT_ACCESS;
        } else if (rule instanceof Permission) {
            final var constraints = rule.getConstraint();
            final var postDuties = ((Permission) rule).getPostDuty();

            //TECNALIA-ICT-OPTIMA: Check for new rule related to Personal Data
            Map <String, Object> propsMap = rule.getProperties();
            if(propsMap != null) {
                Object persDataCat = propsMap.get("http://www.w3.org/ns/dpv#hasPersonalDataCategory");
                if(persDataCat != null) {
                    return PolicyPattern.PERSONAL_DATA;
                }
            }
            
            if (constraints != null && constraints.get(0) != null) {
                if (constraints.size() > 1) {
                    if (postDuties != null && postDuties.get(0) != null) {
                        detectedPattern = PolicyPattern.USAGE_UNTIL_DELETION;
                    } else {
                        detectedPattern = PolicyPattern.USAGE_DURING_INTERVAL;
                    }
                } else {
                    final var firstConstraint = (ConstraintImpl) constraints.get(0);
                    final var leftOperand = firstConstraint.getLeftOperand();
                    final var operator = firstConstraint.getOperator();
                    if (leftOperand == LeftOperand.COUNT) {
                        detectedPattern = PolicyPattern.N_TIMES_USAGE;
                    } else if (leftOperand == LeftOperand.ELAPSED_TIME) {
                        detectedPattern = PolicyPattern.DURATION_USAGE;
                    } else if (leftOperand == LeftOperand.SYSTEM
                            && operator == BinaryOperator.SAME_AS) {
                        detectedPattern = PolicyPattern.CONNECTOR_RESTRICTED_USAGE;
                    //TECNALIA-ICT-OPTIMA: Check for new rule
                    } else if ((leftOperand == LeftOperand.USER )
                            && (((Constraint)constraints.get(0)).getOperator() == BinaryOperator.HAS_MEMBERSHIP))  {
                        detectedPattern = PolicyPattern.ROLE_RESTRICTED_USAGE;
                    //TECNALIA-ICT-OPTIMA: Check for new rule
                    } else if ((leftOperand == LeftOperand.PURPOSE )
                            && (((Constraint)constraints.get(0)).getOperator() == BinaryOperator.SAME_AS))  {
                        detectedPattern = PolicyPattern.PURPOSE_RESTRICTED_USAGE;
                    } else {
                        detectedPattern = null;
                    }
                }
            } else {
                if (postDuties != null && postDuties.get(0) != null) {
                    final var action = postDuties.get(0).getAction().get(0);
                    if (action == Action.NOTIFY) {
                        detectedPattern = PolicyPattern.USAGE_NOTIFICATION;
                    } else if (action == Action.LOG) {
                        detectedPattern = PolicyPattern.USAGE_LOGGING;
                    } else {
                        detectedPattern = null;
                    }
                } else {
                    detectedPattern = PolicyPattern.PROVIDE_ACCESS;
                }
            }
        }

        return detectedPattern;
    }

    //TECNALIA-ICT-OPTIMA: Use java.util.Date instead of java.time.ZonedDateTime
    /**
     * Checks whether the current date is later than the specified one.
     *
     * @param dateNow   the current date.
     * @param maxAccess the target date.
     * @return true, if the current date is later than the target date; false otherwise.
     */
    public static boolean checkDate(final Date dateNow, final Date maxAccess) {
        return !dateNow.after(maxAccess);
    }

    /**
     * Gets the endpoint value to send notifications to defined in a policy.
     *
     * @param rule The ids rule.
     * @return The endpoint value.
     */
    public static String getEndpoint(final Rule rule) throws NullPointerException {
        final var constraint = rule.getConstraint().get(0);
        return ((ConstraintImpl) constraint).getRightOperand().getValue();
    }

    /**
     * Gets the allowed number of accesses defined in a policy.
     *
     * @param rule the policy rule object.
     * @return the number of allowed accesses.
     */
    public static Integer getMaxAccess(final Rule rule) throws NumberFormatException {
        final var constraint = rule.getConstraint().get(0);
        final var value = ((ConstraintImpl) constraint).getRightOperand().getValue();
        final var operator = ((ConstraintImpl) constraint).getOperator();

        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to parse value to integer. [exception=({})]",
                        e.getMessage(), e);
            }
            throw e;
        }

        if (number < 0) {
            number = 0;
        }

        switch (operator) {
            case EQ:
            case LTEQ:
                return number;
            case LT:
                return number - 1;
            default:
                return 0;
        }
    }

    /**
     * Gets the time interval defined in a policy.
     *
     * @param rule the policy rule object.
     * @return the time interval.
     * @throws ParseException exception
     */
    public static TimeInterval getTimeInterval(final Rule rule) throws ParseException {
        final var interval = new TimeInterval();

        for (var constraint : rule.getConstraint()) {
            final var operator = ((ConstraintImpl) constraint).getOperator();
            if (operator == BinaryOperator.AFTER) {
                final var value = ((ConstraintImpl) constraint).getRightOperand().getValue();
                final var start = MappingUtils.getDateOf(value);
                interval.setStart(start);
            } else if (operator == BinaryOperator.BEFORE) {
                final var value = ((ConstraintImpl) constraint).getRightOperand().getValue();
                final var end = MappingUtils.getDateOf(value);
                interval.setEnd(end);
            }
        }
        return interval;
    }

    /**
     * Gets the PIP endpoint path value defined in a policy.
     *
     * @param rule the policy rule object.
     * @return the pip endpoint value.
     */
    public static URI getPipEndpoint(final Rule rule) {
        final var constraint = rule.getConstraint().get(0);
        return ((ConstraintImpl) constraint).getPipEndpoint();
    }

    /**
     * Gets the date value defined in a policy.
     *
     * @param rule the policy constraint object.
     * @return the date or null.
     */
    public static ZonedDateTime getDate(final Rule rule) throws DateTimeParseException {
        final var constraint = rule.getConstraint().get(0);
        final var date = ((ConstraintImpl) constraint).getRightOperand().getValue();

        return MappingUtils.getDateOf(date);
    }

    //TECNALIA-ICT-OPTIMA: Use javax.xml.datatype.Duration instead of java.time.Duration
    /**
     * Gets the duration value defined in a policy.
     *
     * @param rule the policy constraint object.
     * @return the duration or null.
     * @throws javax.xml.datatype.DatatypeConfigurationException if the duration cannot be parsed.
     */
    public static javax.xml.datatype.Duration getDuration(Rule rule) throws DatatypeConfigurationException {
        Constraint constraint = (Constraint)rule.getConstraint().get(0);
//        if (constraint.getRightOperand().getType().equals("xsd:duration")) {
        if (constraint.getRightOperand().getType().equals("http://www.w3.org/2001/XMLSchema#duration")) {
            String duration = constraint.getRightOperand().getValue();
            return DatatypeFactory.newInstance().newDuration(duration);
        } else {
            return null;
        }
    }

    //TECNALIA-ICT-OPTIMA: Use java.util.Date instead of java.time.ZonedDateTime
    //TECNALIA-ICT-OPTIMA: Use javax.xml.datatype.Duration instead of java.time.Duration
    /**
     * Add duration to a date to calculate a new date.
     *
     * @param original The previous date.
     * @param duration The duration to add.
     * @return The new date.
     */
    public static Date getCalculatedDate(
            final Date original, final javax.xml.datatype.Duration duration) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(original);
        cal.add(Calendar.SECOND, duration.getSeconds());
        cal.add(Calendar.MINUTE, duration.getMinutes());
        cal.add(Calendar.HOUR_OF_DAY, duration.getHours());
        cal.add(Calendar.DAY_OF_MONTH, duration.getDays());
        cal.add(Calendar.MONTH, duration.getMonths());
        cal.add(Calendar.YEAR, duration.getYears());

        return cal.getTime();
    }

    /**
     * Check if each rule contains a target.
     *
     * @param ruleList The ids rule list.
     * @throws InvalidInputException If a target is missing.
     */
    public static void validateRuleTarget(final List<? extends Rule> ruleList)
            throws InvalidInputException {
        for (final var rule : ruleList) {
            final var target = rule.getTarget();
            if (target == null || target.toString().equals("")) {
                throw new InvalidInputException(ErrorMessages.MISSING_TARGET.toString());
            }
        }
    }

    /**
     * Get current system date.
     *
     * @return The date object.
     */
    public static ZonedDateTime getCurrentDate() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
    
    //TECNALIA-ICT-OPTIMA: New function for new rule
    /**
     * Returns the allowed Role.
     *
     * @param rule the policy constraint object
     * @return the URI of the Role
     */
    public static URI getAllowedRole(Rule rule) {
        Constraint constraint = (Constraint)rule.getConstraint().get(0);
        URI allowedRole = constraint.getRightOperandReference();
        return allowedRole;
    }

    //TECNALIA-ICT-OPTIMA: New function for new rule
    /**
     * Returns the allowed Purpose.
     *
     * @param rule the policy constraint object
     * @return the URI of the Purpose
     */
    public static URI getAllowedPurpose(Rule rule) {
        Constraint constraint = (Constraint)rule.getConstraint().get(0);
        URI allowedPurpose = constraint.getRightOperandReference();
        return allowedPurpose;
    }  
    
}
