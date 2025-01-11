package org.alex_strunskiy;

import org.alex_strunskiy.database.Database;
import org.alex_strunskiy.utils.RandomStringGenerator;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;


public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres";
    private static final Database DATABASE = new Database(POSTGRES_URL);
    private static final int LINKS_DAYS_LIVE_TIME = 1;

    public static void main(String[] args) throws SQLException {
        DATABASE.initDatabase();
        System.out.println("""
                Приветсвуем Вас в программе Великий сокращатель или просто Чик-чик. Вам доступны три команды:
                user - создать пользователя.
                add link <url> - сократить ссылку.
                go to <short_url> - перейти по короткой ссылке.
                """);

        while (true) {
            String input = SCANNER.nextLine();

            if (input.equals("user")) {
                createUser();
            } else if (isGoToShortLinkCommand(input)) {
                goToLink(input);
            } else if (isAddLinkCommand(input)) {
                addLink(input);
            }
        }
    }

    private static boolean isAddLinkCommand(String input) {
        String addLinkRegex = "add link (https?://\\S+)";
        Pattern pattern = Pattern.compile(addLinkRegex);
        Matcher matcher = pattern.matcher(input);

        return matcher.matches();
    }

    private static boolean isGoToShortLinkCommand(String input) {
        String addLinkRegex = "go to (https://chick-chick.ru/\\S+)";
        Pattern pattern = Pattern.compile(addLinkRegex);
        Matcher matcher = pattern.matcher(input);

        return matcher.matches();

    }

    private static void createUser() {
        String result = DATABASE.createUser();
        System.out.println(result);
    }

    private static void addLink(String input){
        System.out.println("Введите uuid пользователя:");
        String user = SCANNER.nextLine();
        System.out.println("Задайте лимит переходов. Если задаете 0, то кол-во переходов не ограничено.");
        int usageLimit = SCANNER.nextInt();
        String randomString = RandomStringGenerator.generateRandomString(8);
        String[] command = input.split(" ");
        String longLink = command[2];
        String shortLink = "https://chick-chick.ru/" + randomString;
        String result = DATABASE.createLink(user, longLink, shortLink, usageLimit);
        System.out.println(result);
    }

    private static void goToLink(String input){
        System.out.println("Введите uuid пользователя:");
        String user = SCANNER.nextLine();
        String[] command = input.split(" ");
        String shortLink = command[2];
        try {
            Object[] result = DATABASE.getLink(user, shortLink);
            if (result[0] == null) {
                System.out.println("Ссылка не найдена, ошибка базы данных.");
                return;
            }

            UUID linkUUID = (UUID) result[0];
            int limit = (int) result[1];
            if (limit > 1 ) {
                int newLimit = limit-1;
                DATABASE.updateLinkUsageLimit(linkUUID, newLimit);
                System.out.println("Осталось переходов: " + (newLimit));
            } else if (limit == 1) {
                DATABASE.updateLinkUsageLimit(linkUUID, -1);
                System.out.println("Осталось переходов: 0");
            } else if (limit == -1) {
                System.out.println("Лимит использования ссылки исчерпан.");
                return;
            }

            Timestamp linkCreated = (Timestamp) result[2];
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            long differenceInMillis = currentTimestamp.getTime() - linkCreated.getTime();
            long linkLiveTime = LINKS_DAYS_LIVE_TIME * 24 * 60 * 60 * 1000;
            if (differenceInMillis > linkLiveTime) {
                DATABASE.deleteLink(linkUUID);
                System.out.println("Срок действия ссылки истек. Она удалена.");
                return;
            }

            String link = (String) result[3];
            Desktop.getDesktop().browse(new URI(link));
        } catch (IOException | URISyntaxException e) {
            System.out.println("Не удалось перейти по ссылке.");
        }

    }
}