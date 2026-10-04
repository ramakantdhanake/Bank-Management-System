package com.bank.exception;

public class AuthenticationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AuthenticationException(String masg) {
		super(masg);
	}
}
