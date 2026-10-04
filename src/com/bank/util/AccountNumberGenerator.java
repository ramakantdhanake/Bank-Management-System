package com.bank.util;

import java.util.Random;

public class AccountNumberGenerator {
	
	private static final Random random = new Random();
	
	public static String generateAccountNumber() {
		
		 long number = 100000000000L
				 + (long) (random.nextDouble() * 900000000000L);
		 
		 return String.valueOf(number);
	}

}
