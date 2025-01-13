package org.alex_strunskiy;

import org.alex_strunskiy.database.Database;
import org.alex_strunskiy.dataclass.Link;
import org.alex_strunskiy.utils.RandomStringGenerator;
import org.alex_strunskiy.utils.UserInputHandler;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.sql.*;
import java.util.*;


public class Main {
    private static final UserInputHandler userInputHandler = new UserInputHandler();
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres";
    private static final Database DATABASE = new Database(POSTGRES_URL);
    private static final int LINKS_DAYS_LIVE_TIME = 1;


    public static void main(String[] args) throws SQLException {
        DATABASE.initDatabase();
        System.out.println("""
                Приветсвуем Вас в программе Великий сокращатель или просто Чик-чик. Вам доступны три команды:
                user - создать пользователя.
                add link - сократить ссылку.
                go to - перейти по короткой ссылке.
                """);

        while (true) {
            String command = userInputHandler.getCommand();

            switch (command) {
                case "user" -> createUser();
                case "add link" -> addLink();
                case "go to" -> goToLink();
                case "" -> System.out.println("Введите команду.");
                default -> System.out.println("Команда не распознана. Повторите попытку.");
            }
        }
    }

    private static void createUser() {
        String result = DATABASE.createUser();
        System.out.println(result);
    }

    private static void addLink(){
        System.out.println("Введите uuid пользователя:");
        UUID userUUID = userInputHandler.getUserUUID();

        System.out.println("Введите ссылку, которую следует сократить:");
        String longLink = userInputHandler.getLink();

        System.out.println("Задайте лимит переходов. Если задаете 0, то кол-во переходов не ограничено.");
        int usageLimit = userInputHandler.getLinkLimit();

        String randomString = RandomStringGenerator.generateRandomString(8);
        String shortLink = "https://chick-chick.ru/" + randomString;

        String result = DATABASE.createLink(userUUID, longLink, shortLink, usageLimit);
        System.out.println(result);
    }

    private static void goToLink(){
        System.out.println("Введите uuid пользователя:");
        UUID userUUID = userInputHandler.getUserUUID();
        System.out.println("Введите короткую ссылку:");
        String shortLink = userInputHandler.getShortLink();
        try {
            Link link = DATABASE.getLink(userUUID, shortLink);
            if (link == null) {
                System.out.println("Ссылка не найдена, ошибка базы данных.");
                return;
            }

            UUID linkUUID = link.getUuid();
            int limit = link.getLimit();
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

            Timestamp linkCreated = link.getCreated();
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            long differenceInMillis = currentTimestamp.getTime() - linkCreated.getTime();
            long linkLiveTime = LINKS_DAYS_LIVE_TIME * 24 * 60 * 60 * 1000;
            if (differenceInMillis > linkLiveTime) {
                DATABASE.deleteLink(linkUUID);
                System.out.println("Срок действия ссылки истек. Она удалена.");
                return;
            }

            String url = link.getLongLink();
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            System.out.println("Не удалось перейти по ссылке.");
        }

    }
}