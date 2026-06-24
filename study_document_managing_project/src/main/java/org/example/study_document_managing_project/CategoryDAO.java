package org.example.study_document_managing_project;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CategoryDAO {
    public static void testFetchCategories() {
        String query = "SELECT * FROM categories ORDER BY category_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n--- DANH SÁCH DANH MỤC TRONG DATABASE ---");
            while (rs.next()) {
                int id = rs.getInt("category_id");
                String name = rs.getString("category_name");
                System.out.println("ID: " + id + " | Tên: " + name);
            }
            System.out.println("----------------------------------------");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}