package com.bank.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	private static final String URL = "enter your jdbc url";
	
	private static final String USERNAME = "enter your username";
	
	private static final String PASSWORD = "enter your passoword";
	
	public static Connection getConnection() throws SQLException {
		
		return DriverManager.getConnection(URL,USERNAME,PASSWORD);
	}

}
