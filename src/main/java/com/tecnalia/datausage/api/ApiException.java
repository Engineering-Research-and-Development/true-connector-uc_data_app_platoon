package com.tecnalia.datausage.api;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-29T09:57:40.792Z[GMT]")
public class ApiException extends Exception {
	private static final long serialVersionUID = 8519469330187380921L;
	private int code;

	public ApiException(int code, String msg) {
		super(msg);
		this.code = code;
	}


}
