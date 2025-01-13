package org.alex_strunskiy.utils;

import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInputHandler {
    private static Scanner SCANNER;

    public UserInputHandler() {
        SCANNER = new Scanner(System.in);
    }

    private String getString() {
        return SCANNER.nextLine();
    }

    private UUID getUUID() {
        while (true) {
            String input = SCANNER.nextLine();
            try {
                return UUID.fromString(input);
            } catch (IllegalArgumentException _) {
                System.out.println("Введенное значение не соответствует формату UUID. Попробуйте еще раз.");
            }
        }
    }

    private int getInt(){
        while (true) {
            if (SCANNER.hasNextInt()) {
                return SCANNER.nextInt();
            } else {
                System.out.println("Введенное значение не является целочисленным. Попробуйте еще раз.");
            }
        }
    }

    private boolean checkString(String input, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        return matcher.matches();
    }


    public String getCommand() {
        return getString();
    }

    public UUID getUserUUID(){
        return getUUID();
    }

    public String getLink(){
        String regex = "(https?://\\S+)";
        while (true) {
            String input = SCANNER.nextLine();
            if (checkString(input, regex)){
                return input;
            } else {
                System.out.println("Неверный формат ссылки, попробуйте еще раз.");
            }
        }
    }

    public String getShortLink(){
        String regex = "(https://chick-chick.ru/\\S+)";
        while (true) {
            String input = SCANNER.nextLine();
            if (checkString(input, regex)){
                return input;
            } else {
                System.out.println("Неверный формат ссылки, попробуйте еще раз.");
            }
        }
    }

    public int getLinkLimit() {
        return getInt();
    }

}
