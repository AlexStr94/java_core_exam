package org.alex_strunskiy.database;

import java.sql.*;
import java.util.UUID;


public class Database {
    private String databaseUrl;

    public Database(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void initDatabase() throws SQLException {
        Connection conn = DriverManager.getConnection(this.databaseUrl);
        Statement st = conn.createStatement();
        st.execute("CREATE TABLE IF NOT EXISTS public.users (\n" +
                   "\tid uuid NOT NULL,\n" +
                   "\tCONSTRAINT users_pk PRIMARY KEY (id)\n" +
                   ");");
        st.execute(
                "CREATE TABLE IF NOT EXISTS public.links (\n" +
                "\tid uuid NOT NULL,\n" +
                "\tlong_link varchar NOT NULL,\n" +
                "\tshort_link varchar NOT NULL,\n" +
                "\t\"user\" uuid NOT NULL,\n" +
                "\tcreate_date timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,\n" +
                "\tusage_limit int4 DEFAULT 0 NOT NULL,\n" +
                "\tCONSTRAINT links_pk PRIMARY KEY (id),\n" +
                "\tCONSTRAINT fk_user FOREIGN KEY (\"user\") REFERENCES public.users(id)\n" +
                ");"
        );
        st.close();
    }

    public String createUser() {
        String sql = "INSERT INTO public.users\n" +
                     "(id)\n" +
                     "VALUES(gen_random_uuid()) RETURNING id;";
        try (Connection connection = DriverManager.getConnection(databaseUrl);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getString(1);
            } else {
                throw new SQLException();
            }
        } catch (SQLException e) {
            return "Не удалось создать пользователя, ошибка базы данных.";
        }
    }

    public String createLink(String user, String longLink, String shortLink, int limit){
        String sql = "INSERT INTO public.links (id, long_link, short_link, \"user\", usage_limit) " +
                     "VALUES (gen_random_uuid(), ?, ?, ?, ?) " +
                     "ON CONFLICT (long_link, \"user\") DO NOTHING";

        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            UUID userUUID = UUID.fromString(user);
            preparedStatement.setString(1, longLink);
            preparedStatement.setString(2, shortLink);
            preparedStatement.setObject(3, userUUID);
            preparedStatement.setInt(4, limit);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                return "Ссылка добавлена. По ней можно перейти по ссылки: " + shortLink;
            } else {
                return "Ссылка уже существует в ссылках пользователя.";
            }
        } catch (SQLException e) {
            return "Не удалось создать короткую ссылку, ошибка базы данных.";
        }
    }

    public void updateLinkUsageLimit(UUID id, int newLimit){
        String sql = "UPDATE public.links SET usage_limit=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, newLimit);
            preparedStatement.setObject(2, id);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteLink(UUID id){
        String sql = "DELETE FROM public.links WHERE id=?";
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setObject(1, id);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Object[] getLink(String user, String shortLink) {
        String sql = "SELECT * FROM public.links WHERE \"user\" = ? AND short_link = ?";
        Object[] link = new Object[4];
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            UUID userUUID = UUID.fromString(user);
            preparedStatement.setObject(1, userUUID);
            preparedStatement.setString(2, shortLink);

            ResultSet result = preparedStatement.executeQuery();

            if (result.next()){
                UUID linkUUID = (UUID) result.getObject("id");
                int limit = result.getInt("usage_limit");
                Timestamp linkCreated = result.getTimestamp("create_date");
                String url = result.getString("long_link");
                link[0] = linkUUID;
                link[1] = limit;
                link[2] = linkCreated;
                link[3] = url;
            }
            return link;

        } catch (SQLException e) {
            return link;
        }
    }

}
