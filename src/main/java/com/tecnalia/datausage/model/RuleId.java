/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.model;

/**
 *
 * @author root
 */
import java.io.Serializable;
import java.util.Objects;

public class RuleId implements Serializable {

	private static final long serialVersionUID = 1L;
	private String contractUuid;
	private String ruleUuid;

	public RuleId() {
	}

	public RuleId(String contractUuid, String ruleUuid) {
		this.contractUuid = contractUuid;
		this.ruleUuid = ruleUuid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		RuleId ruleId = (RuleId) o;
		return contractUuid.equals(ruleId.contractUuid) && ruleUuid.equals(ruleId.ruleUuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(contractUuid, ruleUuid);
	}
}
