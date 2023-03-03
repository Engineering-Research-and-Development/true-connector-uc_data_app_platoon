package com.tecnalia.datausage.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ContractStore
 */
/*
 * @Validated
 * @javax.annotation.Generated(value =  * "io.swagger.codegen.v3.generators.java.SpringCodegen", date = * "2021-03-29T07:44:48.999Z[GMT]")
 */

@Entity
@Table(name = "RuleStore")
//@IdClass(RuleId.class)
public class RuleStore {

	/**
	 * @return the contractUuid
	 */
	public String getContractUuid() {
		return contractUuid;
	}

	/**
	 * @param contractUuid the contractUuid to set
	 */
	public void setContractUuid(String contractUuid) {
		this.contractUuid = contractUuid;
	}

	/**
	 * @return the ruleUuid
	 */
	public String getRuleUuid() {
		return ruleUuid;
	}

	/**
	 * @param ruleUuid the ruleUuid to set
	 */
	public void setRuleUuid(String ruleUuid) {
		this.ruleUuid = ruleUuid;
	}

	@JsonProperty("contractUuid")
	private String contractUuid;

	@Id
	@JsonProperty("ruleUuid")
	private String ruleUuid;

//  @JsonProperty("contractId")
	private String contractId;

	// @JsonProperty("ruleId")
	private String ruleId;

	// @JsonProperty("targetId")
	private String targetId;

	// @JsonProperty("ruleContent")
	@Column(columnDefinition = "TEXT")
	private String ruleContent;

	/**
	 * @return the contractId
	 */
	public String getContractId() {
		return contractId;
	}

	/**
	 * @param contractId the contractId to set
	 */
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	/**
	 * @return the ruleId
	 */
	public String getRuleId() {
		return ruleId;
	}

	/**
	 * @param ruleId the ruleId to set
	 */
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	/**
	 * @return the targetId
	 */
	public String getTargetId() {
		return targetId;
	}

	/**
	 * @param targetId the targetId to set
	 */
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	/**
	 * @return the ruleAction
	 */
	public String getRuleContent() {
		return ruleContent;
	}

	/**
	 * @param ruleContent the ruleContent to set
	 */
	public void setRuleContent(String ruleContent) {
		this.ruleContent = ruleContent;
	}

}
