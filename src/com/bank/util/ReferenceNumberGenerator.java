package com.bank.util;

import java.util.UUID;

public class ReferenceNumberGenerator {
	
	public static String generateRefferenceNumber() {
		
		return "TXN"+ UUID.randomUUID().toString().replace("-", "").substring(0,20);
	}

}
