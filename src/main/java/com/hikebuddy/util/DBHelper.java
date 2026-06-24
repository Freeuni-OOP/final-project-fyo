package com.hikebuddy.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBHelper {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        try (InputStream input = DBHelper.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException("db.properties not found in classpath");
            }

            Properties props = new Properties();
            props.load(input);

            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}