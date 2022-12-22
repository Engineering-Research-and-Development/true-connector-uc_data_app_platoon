/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tecnalia.datausage.model.AccessStore;
import com.tecnalia.datausage.model.ContractStore;
import com.tecnalia.datausage.model.RuleStore;
import com.tecnalia.datausage.repository.AccessRepository;
import com.tecnalia.datausage.repository.ContractRepository;
import com.tecnalia.datausage.repository.RuleRepository;
import com.tecnalia.datausage.usagecontrol.PersonalDataEnforcement;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.Prohibition;
import de.fraunhofer.iais.eis.Rule;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.dataspaceconnector.services.usagecontrol.DataAccessVerifier;
import io.dataspaceconnector.services.usagecontrol.DataProvisionVerifier;
import io.dataspaceconnector.services.usagecontrol.PolicyPattern;
import io.dataspaceconnector.services.usagecontrol.VerificationInput;
import io.dataspaceconnector.services.usagecontrol.VerificationResult;
import io.dataspaceconnector.utils.RuleUtils;

/**
 *
 * @author usuario
 */
@Service
public class EnforcementService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EnforcementService.class);

	@Autowired
	private ContractRepository contractRepository;

	@Autowired
	private RuleRepository ruleRepository;

	@Autowired
	private AccessRepository accessRepository;

	@Autowired
	PersonalDataEnforcement personalDataEnforcement;

	/**
	 * The verifier for the data access from provider side.
	 */
	@Autowired
	DataProvisionVerifier provisionVerifier;

	/**
	 * The verifier for the data access from consumer side.
	 */
	@Autowired
	DataAccessVerifier accessVerifier;
	
	private Serializer serializer = new Serializer();

	public ResponseEntity<Object> enforce(String targetDataUri, String providerUri, String consumerUri,
			boolean consuming, String body) {
		// Get contracts from ContractAgreement table applied to this providerURI &
		// consumerUri
		Iterable<ContractStore> contractList = this.contractRepository.findAllByProviderIdAndConsumerId(providerUri,
				consumerUri);
		// Get contracts that apply to targetUri and which start-end dates are valid
		// according to current date, and get the most recent Contract
		ContractStore validContractStore = getValidContracts(contractList, targetDataUri);
		if (validContractStore == null) {
			LOGGER.info("No valid contracts found");
			return new ResponseEntity<>("No valid contracts found", HttpStatus.BAD_REQUEST);
		}
		String contractTxt = validContractStore.getContractAsString();
		Date validContractStart = null;
		try {
//			Serializer serializer = new Serializer();
			Contract contract = serializer.deserialize(contractTxt, Contract.class);
			validContractStart = contract.getContractStart().toGregorianCalendar().getTime();
		} catch (Exception e) {
			LOGGER.info("Incorrect contract format");
			return new ResponseEntity<>("Incorrect contract format", HttpStatus.BAD_REQUEST);
		}

		// Get rules from rule table applied to the contract, targetDataUri
		Iterable<RuleStore> ruleList = this.ruleRepository
				.findAllByContractUuidAndTargetId(validContractStore.getContractUuid(), targetDataUri);
		ArrayList<Rule> ruleArrayList = new ArrayList<>();
		for (RuleStore ruleStore : ruleList) {
			String ruleTxt = ruleStore.getRuleContent();
			String ruleType = getRuleType(ruleTxt);
			try {
				if (ruleType.compareToIgnoreCase("Permission") == 0) {
					Permission perm = serializer.deserialize(ruleTxt, Permission.class);
					ruleArrayList.add(perm);
				} else if (ruleType.compareToIgnoreCase("Prohibition") == 0) {
					Prohibition prohib = serializer.deserialize(ruleTxt, Prohibition.class);
					ruleArrayList.add((Prohibition) prohib);
				}
			} catch (Exception e) {
				LOGGER.info("Invalid rule format");
				return new ResponseEntity<>("Invalid rule format", HttpStatus.BAD_REQUEST);
			}
		}

		// Apply enforcement
		boolean allowAccess = false;
		String filteredDataObject = body;
		final var input = new VerificationInput(targetDataUri, ruleArrayList, consumerUri, validContractStart);
		if (consuming) {
			// For each rule, apply enforcement
			if (accessVerifier.verify(input) == VerificationResult.ALLOWED) {
				allowAccess = true;
				// Check for Personal Data Rule
				for (Rule rule : ruleArrayList) {
					final var pattern = RuleUtils.getPatternByRule(rule);
					if (pattern == PolicyPattern.PERSONAL_DATA) {
						filteredDataObject = personalDataEnforcement.enforce((Permission) rule, providerUri,
								consumerUri, targetDataUri, body);
						break;
					}

				}
			}

			if (allowAccess)
				incrementAccessFrequency(targetDataUri, consumerUri);
		} else {
			// For each rule, apply enforcement
			if (provisionVerifier.verify(input) == VerificationResult.ALLOWED) {
				allowAccess = true;
			}
		}

		if (allowAccess) {
			LOGGER.info("Allowed access for target {}", targetDataUri);
			return new ResponseEntity<>(filteredDataObject, HttpStatus.OK);
		} else {
			LOGGER.info("PDP decided to inhibit the usage: Event is not allowed according to policy for target {}", targetDataUri);
			return new ResponseEntity<>("PDP decided to inhibit the usage: Event is not allowed according to policy",
					HttpStatus.FORBIDDEN);
		}
	}
	
	public ResponseEntity<Object> enforceAgreement(String contractAgreement, boolean consuming, String body) {
		// Get contracts from ContractAgreement table applied to this providerURI &
		// consumerUri
		Optional<ContractStore> contractStoreOptional = this.contractRepository.findByContractId(contractAgreement);
		// Get contracts that apply to targetUri and which start-end dates are valid
		// according to current date, and get the most recent Contract
		if(contractStoreOptional.isEmpty()) {
			LOGGER.info("No valid contract found");
			return new ResponseEntity<>("No valid contract found", HttpStatus.BAD_REQUEST);
		}
		ContractStore contractStore = contractStoreOptional.get();
		Contract contract = null;
		Date validContractStart = null;
		try {
			contract = serializer.deserialize(contractStore.getContractAsString(), Contract.class);
			validContractStart = contract.getContractStart().toGregorianCalendar().getTime();
		} catch (Exception e) {
			LOGGER.info("Incorrect contract format");
			return new ResponseEntity<>("Incorrect contract format", HttpStatus.BAD_REQUEST);
		}

		boolean isActive = isContractActive(contract);
		if (!isActive) {
			LOGGER.info("Contract is not active or expired");
			return new ResponseEntity<>("Contract is not active or expired", HttpStatus.BAD_REQUEST);
		}

		// Get rules from rule table applied to the contract, targetDataUri
		String targetDataUri = contract.getPermission().get(0).getTarget().toString();
		String providerUri =  contractStore.getProviderId();
		String consumerUri =  contractStore.getConsumerId();
		
		Iterable<RuleStore> ruleList = this.ruleRepository
				.findAllByContractUuidAndTargetId(contractStore.getContractUuid(), targetDataUri);
		ArrayList<Rule> ruleArrayList = new ArrayList<>();
		for (RuleStore ruleStore : ruleList) {
			String ruleTxt = ruleStore.getRuleContent();
			String ruleType = getRuleType(ruleTxt);
			try {
				if (ruleType.compareToIgnoreCase("Permission") == 0) {
					Permission perm = serializer.deserialize(ruleTxt, Permission.class);
					ruleArrayList.add(perm);
				} else if (ruleType.compareToIgnoreCase("Prohibition") == 0) {
					Prohibition prohib = serializer.deserialize(ruleTxt, Prohibition.class);
					ruleArrayList.add((Prohibition) prohib);
				}
			} catch (Exception e) {
				LOGGER.info("Invalid rule format");
				return new ResponseEntity<>("Invalid rule format", HttpStatus.BAD_REQUEST);
			}
		}

		
		// Apply enforcement
		boolean allowAccess = false;
		String filteredDataObject = body;
		final var input = new VerificationInput(targetDataUri, ruleArrayList, consumerUri, validContractStart);
		if (consuming) {
			// For each rule, apply enforcement
			if (accessVerifier.verify(input) == VerificationResult.ALLOWED) {
				allowAccess = true;
				// Check for Personal Data Rule
				for (Rule rule : ruleArrayList) {
					final var pattern = RuleUtils.getPatternByRule(rule);
					if (pattern == PolicyPattern.PERSONAL_DATA) {
						filteredDataObject = personalDataEnforcement.enforce((Permission) rule, providerUri,
								consumerUri, targetDataUri, body);
						break;
					}

				}
			}

			if (allowAccess)
				incrementAccessFrequency(targetDataUri, consumerUri);
		} else {
			// For each rule, apply enforcement
			if (provisionVerifier.verify(input) == VerificationResult.ALLOWED) {
				allowAccess = true;
			}
		}

		if (allowAccess) {
			LOGGER.info("Allowed access for target {}", targetDataUri);
			return new ResponseEntity<>(filteredDataObject, HttpStatus.OK);
		} else {
			LOGGER.info("PDP decided to inhibit the usage: Event is not allowed according to policy for target {}", targetDataUri);
			return new ResponseEntity<>("PDP decided to inhibit the usage: Event is not allowed according to policy",
					HttpStatus.FORBIDDEN);
		}
	}

	private boolean isContractActive(Contract contract) {
		boolean isValid = false;
		Date contractStart = contract.getContractStart().toGregorianCalendar().getTime();
		Date contractEnd = null;
		XMLGregorianCalendar contractEndGregCal = contract.getContractEnd();
		if (contractEndGregCal != null) {
			contractEnd = contractEndGregCal.toGregorianCalendar().getTime();
		}
//		Date contractDate = contract.getContractDate().toGregorianCalendar().getTime();
		Date date = new Date();
		if (date.after(contractStart)
				&& ((contractEnd == null) || (contractEnd != null && date.before(contractEnd)))) {
			isValid = true;
			LOGGER.debug("Contract is active");
		}
		return isValid;
	}
	
	ContractStore getValidContracts(Iterable<ContractStore> contractList, String targetDataUri) {
		// Get contracts that apply to targetUri and which start-end dates are valid
		// according to current date, and get the most recent Contract
		ContractStore validContractStore = null;
		Date latestContractDate = null;
		for (ContractStore contractStore : contractList) {
			// Firstly check if conract applies to targetDataUri
			Iterable<RuleStore> ruleList = this.ruleRepository
					.findAllByContractUuidAndTargetId(contractStore.getContractUuid(), targetDataUri);
			if (!ruleList.iterator().hasNext())
				// If contract not applies to targetDataUri, do not consider it
				continue;
			String contractTxt = contractStore.getContractAsString();
//			Serializer serializer = new Serializer();
			try {
				Contract contract = serializer.deserialize(contractTxt, Contract.class);
				Date contractStart = contract.getContractStart().toGregorianCalendar().getTime();
				Date contractEnd = null;
				XMLGregorianCalendar contractEndGregCal = contract.getContractEnd();
				if (contractEndGregCal != null) {
					contractEnd = contractEndGregCal.toGregorianCalendar().getTime();
				}
				Date contractDate = contract.getContractDate().toGregorianCalendar().getTime();
				Date date = new Date();
				if (date.after(contractStart)
						&& ((contractEnd == null) || (contractEnd != null && date.before(contractEnd)))) {
					if ((latestContractDate == null)
							|| ((latestContractDate != null) && (contractDate.after(latestContractDate)))) {
						validContractStore = contractStore;
						latestContractDate = contractDate;
					}
				}
			} catch (Exception e) {
			}
		}
		return validContractStore;
	}

	String getRuleType(String ruleTxt) {
		String ruleType = "";
//		Serializer serializer = new Serializer();
		try {
			Permission rule = serializer.deserialize(ruleTxt, Permission.class);
			ruleType = "Permission";
		} catch (Exception ex) {

		}

		try {
			Prohibition rule = serializer.deserialize(ruleTxt, Prohibition.class);
			ruleType = "Prohibition";
		} catch (Exception ex) {

		}

		return ruleType;
	}

	@Transactional
	void incrementAccessFrequency(String targetDataUri, String consumerUri) {
		// Increment by 1 the access frequency
		Optional<AccessStore> bCheckExistsAccess = this.accessRepository.findByConsumerUriAndTargetUri(consumerUri,
				targetDataUri);
		// if exists update
		if (bCheckExistsAccess.isPresent()) {
			AccessStore accessStore = (AccessStore) bCheckExistsAccess.get();
			accessStore.setNumAccess(accessStore.getNumAccess() + 1);
			this.accessRepository.saveAndFlush(accessStore);
		} else {
			// insert
			AccessStore accessStore = new AccessStore();
			accessStore.setConsumerUri(consumerUri);
			accessStore.setTargetUri(targetDataUri);
			accessStore.setNumAccess(1);
			this.accessRepository.saveAndFlush(accessStore);
		}
		return;
	}
}
