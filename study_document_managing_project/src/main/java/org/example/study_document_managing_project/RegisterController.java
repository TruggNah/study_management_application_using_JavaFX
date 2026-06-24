package org.example.study_document_managing_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label lblMessage;
    @FXML private ComboBox<DepartmentItem> cbDepartment;

    // Hàm initialize() chạy tự động khi màn hình Đăng ký được nạp lên
    @FXML
    public void initialize() {
        // Đổ dữ liệu lựa chọn vào ComboBox theo dạng Tiếng Anh chuẩn của Database
        cbRole.setItems(FXCollections.observableArrayList("student", "teacher"));
        cbRole.setValue("student"); // Để mặc định là Student
        loadDepartmentsToCombo();
    }

    @FXML
    public void handleSignUp() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();
        String email = txtEmail.getText().trim();
        String role = cbRole.getValue();
        DepartmentItem selectedDept = cbDepartment.getValue(); // Lấy đối tượng Viện được chọn

        // 1. Kiểm tra rỗng dữ liệu (Bổ sung thêm kiểm tra role và department)
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || role == null || selectedDept == null) {
            lblMessage.setText("Alert: All fields must be filled and selections made!");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        // 2. Kiểm tra mật khẩu khớp nhau
        if (!password.equals(confirmPassword)) {
            lblMessage.setText("Alert: Passwords do not match!");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        // 3. Thực hiện INSERT vào bảng users (Bổ sung thêm cột department_id)
        String sql = "INSERT INTO users (username, password, email, role, department_id, status) VALUES (?, ?, ?, ?, ?, 'active')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, role);
            pstmt.setInt(5, selectedDept.getId()); // Truyền trực tiếp ID của Viện đã chọn vào đây

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                lblMessage.setText("Alert: User registered successfully!");
                lblMessage.setStyle("-fx-text-fill: green;");

                // Xóa trống các ô nhập và lựa chọn để người dùng biết đã thành công
                txtUsername.clear();
                txtPassword.clear();
                txtConfirmPassword.clear();
                txtEmail.clear();
                cbRole.setValue(null);
                cbDepartment.setValue(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Báo lỗi nếu trùng username hoặc email do ràng buộc UNIQUE trong DB
            lblMessage.setText("Alert: Username or Email already exists!");
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void handleBackToLogin() {
        try {
            // Nạp quay lại giao diện đăng nhập cũ
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("STUDENT DOCUMENT MANAGEMENT SYSTEMS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDepartmentsToCombo() {
        ObservableList<DepartmentItem> depts = FXCollections.observableArrayList();
        String sql = "SELECT department_id, dept_name FROM departments ORDER BY dept_name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                depts.add(new DepartmentItem(rs.getInt("department_id"), rs.getString("dept_name")));
            }
            cbDepartment.setItems(depts);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}