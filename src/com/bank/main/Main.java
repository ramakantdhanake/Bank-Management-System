package com.bank.main;

import java.util.Scanner;
import com.bank.controller.CustomerController;
import com.bank.util.ConsoleUtil;

public class Main {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		CustomerController customerController = new CustomerController();

		while (true) {
			System.out.println();
			ConsoleUtil.printHeader("RD BANK OF INDIA • SECURE BANKING");

			System.out.println(ConsoleUtil.B_CYAN +ConsoleUtil.BOLD+ "  [1] " + ConsoleUtil.WHITE +  "Customer Login");
			System.out.println(ConsoleUtil.B_CYAN + "  [2] " + ConsoleUtil.WHITE + "Customer Registration (New User)");
			System.out.println(ConsoleUtil.B_CYAN + "  [3] " + ConsoleUtil.RED + "Exit" + ConsoleUtil.RESET);
			System.out.print(ConsoleUtil.B_CYAN + "\n  ➤ Select an option : " + ConsoleUtil.RESET);

			String input = sc.nextLine().trim();

			if ("1".equals(input)) {
				// Confirmation before Login
				if (ConsoleUtil.confirmAction(sc, "Do you want to proceed to Login?")) {
					customerController.login();
				}

			} else if ("2".equals(input)) {
				// Confirmation before Registration
				if (ConsoleUtil.confirmAction(sc, "Do you want to create a new Bank Account?")) {
					customerController.registerCustomer();
				}

			} else if ("3".equals(input)) {
				if (ConsoleUtil.confirmAction(sc, "Are you sure you want to exit the application?")) {
					System.out.println("\n" + ConsoleUtil.B_GREEN
							+ "  Thank you for banking with RD Bank of India. Have a great day!" + ConsoleUtil.RESET);
					sc.close();
					return;
				}

			} else {
				ConsoleUtil.printError("Invalid choice. Please enter a valid number (1, 2, or 3).");
			}
		}
	}
}