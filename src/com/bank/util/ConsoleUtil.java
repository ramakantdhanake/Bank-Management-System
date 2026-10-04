package com.bank.util;

import java.util.Scanner;

public class ConsoleUtil {

    // ANSI Color Codes
    public static final String RESET   = "\u001B[0m";
    public static final String BOLD    = "\u001B[1m";
    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String BLUE    = "\u001B[34m";
    public static final String CYAN    = "\u001B[36m";
    public static final String WHITE   = "\u001B[37m";

    public static final String B_CYAN  = "\u001B[96m";
    public static final String B_GREEN = "\u001B[92m";
    public static final String B_RED   = "\u001B[91m";

   
    private static final int WIDTH = 62;

    public static void printHeader(String title) {
        if (title.length() > WIDTH - 4) {
            title = title.substring(0, WIDTH - 7) + "...";
        }
        int totalSpaces = WIDTH - 2 - title.length();
        int leftPad = totalSpaces / 2;
        int rightPad = totalSpaces - leftPad;

        System.out.println("\n" + B_CYAN + "╔" + "═".repeat(WIDTH - 2) + "╗");
        System.out.println("║" + " ".repeat(leftPad) + BOLD + WHITE + title + RESET + B_CYAN + " ".repeat(rightPad) + "║");
        System.out.println("╚" + "═".repeat(WIDTH - 2) + "╝" + RESET);
    }

  
    public static void printSuccessBox(String title, String message) {
        System.out.println("\n" + B_GREEN + "┌" + "─".repeat(WIDTH - 2) + "┐");
        printBoxLine("✔ " + title, B_GREEN, BOLD + WHITE);
        printBoxLine(message, B_GREEN, WHITE);
        System.out.println(B_GREEN + "└" + "─".repeat(WIDTH - 2) + "┘" + RESET);
    }


    public static void printError(String message) {
        System.out.println("\n" + B_RED + "┌─── ERROR " + "─".repeat(WIDTH - 12) + "┐");
        printBoxLine("✖ " + message, B_RED, WHITE);
        System.out.println(B_RED + "└" + "─".repeat(WIDTH - 2) + "┘" + RESET);
    }

    public static boolean confirmAction(Scanner sc, String question) {
        while (true) {
            System.out.println("\n" + YELLOW + "┌─── CONFIRMATION " + "─".repeat(WIDTH - 19) + "┐");
            printBoxLine(question, YELLOW, BOLD + WHITE);
            printBoxLine("1. Yes (Proceed)", YELLOW, B_GREEN);
            printBoxLine("2. No  (Cancel)", YELLOW, B_RED);
            System.out.println(YELLOW + "└" + "─".repeat(WIDTH - 2) + "┘" + RESET);
            System.out.print(B_CYAN + "  ➤ Select Option (1/2) : " + RESET);

            String choice = sc.nextLine().trim();
            if ("1".equals(choice)) {
                return true;
            } else if ("2".equals(choice)) {
                return false;
            } else {
                printError("Invalid selection. Please choose 1 or 2.");
            }
        }
    }

  
    private static void printBoxLine(String text, String borderColor, String textColor) {
       
        if (text.length() > WIDTH - 6) {
            text = text.substring(0, WIDTH - 9) + "...";
        }
        int padding = WIDTH - 4 - text.length();
        System.out.println(borderColor + "│  " + textColor + text + RESET + borderColor + " ".repeat(Math.max(0, padding)) + "│" + RESET);
    }
}