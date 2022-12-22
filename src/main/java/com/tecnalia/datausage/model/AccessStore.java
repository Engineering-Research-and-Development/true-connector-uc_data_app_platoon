package com.tecnalia.datausage.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "AccessStore")

@IdClass(AccessId.class)
public class AccessStore {

	@Id
	@JsonProperty("targetUri")
	private String targetUri;

	@Id
	@JsonProperty("consumerUri")
	private String consumerUri;

	@JsonProperty("numAccess")
	private int numAccess;

	/**
	 * @return the targetUri
	 */
	public String getTargetUri() {
		return targetUri;
	}

	/**
	 * @param targetUri the targetUri to set
	 */
	public void setTargetUri(String targetUri) {
		this.targetUri = targetUri;
	}

	/**
	 * @return the consumerUri
	 */
	public String getConsumerUri() {
		return consumerUri;
	}

	/**
	 * @param consumerUri the consumerUri to set
	 */
	public void setConsumerUri(String consumerUri) {
		this.consumerUri = consumerUri;
	}

	/**
	 * @return the numAccess
	 */
	public int getNumAccess() {
		return numAccess;
	}

	/**
	 * @param numAccess the numAccess to set
	 */
	public void setNumAccess(int numAccess) {
		this.numAccess = numAccess;
	}

}
