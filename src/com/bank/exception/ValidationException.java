package com.bank.exception;

public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ValidationException(String masg) {
		super(masg);
	}

}
