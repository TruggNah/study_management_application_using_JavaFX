package org.example.study_document_managing_project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReportController {

    @FXML private Label lblTitle;
    @FXML private Label lblCategory;
    @FXML private Label lblSubject;
    @FXML private Label lblFileUrl;
    @FXML private Label lblReporter;
    @FXML private TextArea txtReason;
    @FXML private Label lblMessage;

    private int currentMaterialId;
    private String currentUsername;

    // Hàm nhận dữ liệu truyền sang từ MaterialDetailController
    public void setReportData(int materialId, String title, String category, String subject, String fileUrl, String username) {
        this.currentMaterialId = materialId;
        this.currentUsername = username;

        lblTitle.setText(title);
        lblCategory.setText(category);
        lblSubject.setText(subject);
        lblFileUrl.setText(fileUrl);
        lblReporter.setText(username);
    }

    @FXML
    private void handleSubmitReport() {
        String reason = txtReason.getText().trim();

        if (reason.isEmpty()) {
            lblMessage.setText("❌ Please enter a reason for reporting!");
            return;
        }

        // Logic tương tác Database: Lấy user_id và insert trực tiếp vào bảng reports
        String getUserIdSql = "SELECT user_id FROM users WHERE username = ?;";
        String insertReportSql = "INSERT INTO reports (user_id, material_id, reason) VALUES (?, ?, ?);";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int userId = -1;

            // 1. Tìm user_id của người đang thực hiện report
            try (PreparedStatement pstmtUser = conn.prepareStatement(getUserIdSql)) {
                pstmtUser.setString(1, currentUsername);
                try (ResultSet rs = pstmtUser.executeQuery()) {
                    if (rs.next()) userId = rs.getInt("user_id");
                }
            }

            if (userId != -1) {
                // 2. Ghi nhận thông tin vào bảng reports
                try (PreparedStatement pstmtReport = conn.prepareStatement(insertReportSql)) {
                    pstmtReport.setInt(1, userId);
                    pstmtReport.setInt(2, currentMaterialId);
                    pstmtReport.setString(3, reason);
                    pstmtReport.executeUpdate();
                }

                // Thông báo thành công và tự đóng popup
                lblMessage.setStyle("-fx-text-fill: #27ae60;");
                lblMessage.setText("✅ Document reported successfully!");
                txtReason.setDisable(true);

                // Đóng popup sau 1.5 giây
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(event -> handleCancel());
                delay.play();

            } else {
                lblMessage.setText("Error: User session not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Database error occurred!");
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtReason.getScene().getWindow();
        stage.close();
    }
}