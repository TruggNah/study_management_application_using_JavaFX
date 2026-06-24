package org.example.study_document_managing_project;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage; // Nối với cái nhãn thông báo lỗi

    @FXML
    private Button btnSignIn; // Đổi từ btnLogin thành btnSignIn cho khớp FXML

    @FXML
    private Button btnSignUp; // Khai báo thêm nút Sign up nếu cần dùng trong code Java

    @FXML
    public void handleSwitchToSignUp() {
        try {
            // Hãy chắc chắn file register-view.fxml nằm đúng trong thư mục resources cùng cấp với hello-view.fxml
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Lấy Stage hiện tại từ nút bấm btnSignUp của bạn
            Stage stage = (Stage) btnSignUp.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("USER REGISTRATION - STUDENT DOCUMENT MANAGEMENT SYSTEMS");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Trường hợp 1: Người dùng để trống tài khoản hoặc mật khẩu
        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Alert: Please enter both username and password!");
            lblMessage.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            lblMessage.setVisible(true);
            return;
        }

        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'active'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Trường hợp 2: Đăng nhập thành công
                    String role = rs.getString("role");
                    lblMessage.setText("Login successful! Role: " + role.toUpperCase());
                    lblMessage.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    lblMessage.setVisible(true);

                    // THỰC HIỆN CHUYỂN MÀN HÌNH SANG DASHBOARD TẠI ĐÂY
                    javafx.application.Platform.runLater(() -> {
                        try {
                            // Nạp file giao diện Dashboard mới
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dashboard-view.fxml"));
                            Scene scene = new Scene(fxmlLoader.load());

                            // Lấy instance của DashboardController vừa được nạp
                            DashboardController dashboardController = fxmlLoader.getController();
                            // Truyền username thực tế (ví dụ: trungnah) vào hàm thiết lập
                            dashboardController.setCurrentUser(username);

                            Stage stage = (Stage) btnSignIn.getScene().getWindow();
                            stage.setScene(scene);
                            stage.setTitle("DASHBOARD - STUDENT DOCUMENT MANAGEMENT SYSTEM");
                            stage.centerOnScreen(); // Đưa cửa sổ ra chính giữa màn hình
                            stage.setMaximized(true); // Tự động phóng to toàn màn hình cho chuyên nghiệp

                        } catch (Exception e) {
                            e.printStackTrace();
                            lblMessage.setText("Alert: Cannot load Dashboard interface!");
                            lblMessage.setStyle("-fx-text-fill: red;");
                        }
                    });

                } else {
                    // Trường hợp 3: Nhập sai tài khoản hoặc mật khẩu
                    lblMessage.setText("Alert: wrong password or username. Please try again!");
                    lblMessage.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    lblMessage.setVisible(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Alert: Database connection error!");
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setVisible(true);
        }
    }
}