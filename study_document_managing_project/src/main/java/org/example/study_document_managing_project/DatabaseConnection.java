package org.example.study_document_managing_project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Điền thông tin Database của bạn vào đâyx x
    private static final String URL = "jdbc:postgresql://localhost:5432/study_management";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123@Qwe-rt";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Thực hiện kết nối
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Sucessfully connect to PostgreSQL!");
        } catch (SQLException e) {
            System.err.println("Can not connect to Database " + e.getMessage());
        }
        return connection;
    }
}