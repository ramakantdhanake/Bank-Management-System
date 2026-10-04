package com.bank.exception;

public class BusinessRuleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BusinessRuleException(String masg) {
		super(masg);
	}

}
