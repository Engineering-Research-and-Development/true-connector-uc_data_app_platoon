package com.tecnalia.datausage.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ContractStore
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-29T07:44:48.999Z[GMT]")

@Entity
@Table(name = "ContractStore")
public class ContractStore {
	@JsonProperty("contractAsString")
	@Column(columnDefinition = "TEXT")
	private String contractAsString;

	@Id
	@JsonProperty("contractUuid")
	private String contractUuid;

	@JsonProperty("contractId")
	private String contractId;

	@JsonProperty("consumerId")
	private String consumerId;

	@JsonProperty("providerId")
	private String providerId;

	public ContractStore contractAsString(String contractAsString) {
		this.contractAsString = contractAsString;
		return this;
	}

	/**
	 * Get contractAsString
	 * 
	 * @return contractAsString
	 **/
	@Schema(description = "")

	public String getContractAsString() {
		return contractAsString;
	}

	public void setContractAsString(String contractAsString) {
		this.contractAsString = contractAsString;
	}

	public ContractStore contractUuid(String contractUuid) {
		this.contractUuid = contractUuid;
		return this;
	}

	/**
	 * Get contractUuid
	 * 
	 * @return contractUuid
	 **/
	@Schema(description = "")
	public String getContractUuid() {
		return contractUuid;
	}

	public ContractStore contractId(String contractId) {
		this.contractId = contractId;
		return this;
	}

	/**
	 * Get contractId
	 * 
	 * @return contractId
	 **/
	@Schema(description = "")
	public String getContractId() {
		return contractId;
	}

	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	/*
	 * public ContractStore targetId(String targetId) { this.targetId = targetId;
	 * return this; }
	 */

	/*
	 * @Schema(description = "")
	 * 
	 * public String getTargetId() { return targetId; }
	 * 
	 * public void setTargetId(String targetId) { this.targetId = targetId; }
	 */

	public ContractStore consumerId(String consumerId) {
		this.consumerId = consumerId;
		return this;
	}

	/**
	 * Get consumerId
	 * 
	 * @return consumerId
	 **/
	@Schema(description = "")
	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	public ContractStore providerId(String providerId) {
		this.providerId = providerId;
		return this;
	}

	/**
	 * Get providerId
	 * 
	 * @return providerId
	 **/
	@Schema(description = "")

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ContractStore contractStore = (ContractStore) o;
		return Objects.equals(this.contractAsString, contractStore.contractAsString)
				&& Objects.equals(this.contractUuid, contractStore.contractUuid)
				&& Objects.equals(this.contractId, contractStore.contractId) &&
				// Objects.equals(this.targetId, contractStore.targetId) &&
				Objects.equals(this.consumerId, contractStore.consumerId)
				&& Objects.equals(this.providerId, contractStore.providerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(contractAsString, contractUuid, contractId, consumerId, providerId);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ContractStore {\n");

		sb.append("    contractAsString: ").append(toIndentedString(contractAsString)).append("\n");
		sb.append("    contractUuid: ").append(toIndentedString(contractUuid)).append("\n");
		sb.append("    contractId: ").append(toIndentedString(contractId)).append("\n");
		// sb.append(" targetId: ").append(toIndentedString(targetId)).append("\n");
		sb.append("    consumerId: ").append(toIndentedString(consumerId)).append("\n");
		sb.append("    providerId: ").append(toIndentedString(providerId)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
