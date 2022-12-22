/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tecnalia.datausage.model;

import java.io.Serializable;
import java.util.Objects;

public class AccessId implements Serializable {
 
	private static final long serialVersionUID = 1L;
	private String targetUri;
    private String consumerUri;

    public AccessId() {
    }

    public AccessId(String targetUri, String consumerUri) {
        this.targetUri = targetUri;
        this.consumerUri = consumerUri;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessId accessId = (AccessId) o;
        return targetUri.equals(accessId.targetUri) &&
                consumerUri.equals(accessId.consumerUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetUri, consumerUri);
    }
}
