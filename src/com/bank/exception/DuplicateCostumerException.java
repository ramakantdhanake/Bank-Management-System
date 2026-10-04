package com.bank.exception;

public class DuplicateCostumerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateCostumerException(String masg) {
		super(masg);
	}

}
