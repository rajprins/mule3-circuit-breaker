package com.mulesoft.training.demo.exceptions;

public class ConnectionNotAvailbleException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private static String msg = "Connecting to resource failed. Resource not availble. Try again later.";
	
	public ConnectionNotAvailbleException() {
		super(msg);
	}
	
}
