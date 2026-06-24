package org.example.study_document_managing_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class MaterialDetailController {
    @FXML private Label lblTitle;
    @FXML private Label lblDepartment;
    @FXML private Label lblSubject;
    @FXML private Label lblCategory;
    @FXML private Label lblOwner;
    @FXML private Label lblDownloads;
    @FXML private Label lblRating;
    @FXML private Label lblUrl;
    @FXML private Button btnClose;
    @FXML private Button btnBookmark;
    // new component
    @FXML private ComboBox<Integer> cbRatingValue;
    @FXML private VBox vboxComments;
    @FXML private TextField txtCommentContent;

    private Material currentMaterial;
    private String downloaderUsername;

    @FXML
    public void initialize() {
        if (cbRatingValue != null) {
            cbRatingValue.getItems().clear();
            cbRatingValue.getItems().addAll(1, 2, 3, 4, 5);
            cbRatingValue.getSelectionModel().select(4); // Mặc định hiển thị mức 5 sao
        }
    }

    public void setMaterialData(Material material, String username) {
        this.currentMaterial = material;
        this.downloaderUsername = username;

        lblTitle.setText("Title: " + material.getTitle());
        lblSubject.setText("Subject: " + material.getSubjectName());
        lblCategory.setText("Category: " + material.getCategoryName());
        lblOwner.setText("Owner: " + material.getUploadedBy());
        lblDownloads.setText("Total Downloads: " + material.getDownloadCount());
        lblRating.setText("Rating: " + material.getAvgRating() + " ⭐");
        lblUrl.setText("File URL: " + material.getFileUrl());

        // Gọi nạp dữ liệu comment lên giao diện mạng xã hội
        loadComments();
    }

    // ================= CHỨC NĂNG 1: SUBMIT RATING (CÓ THỂ ĐỂ TRỐNG COMMENT) =================
    @FXML
    public void handleRatingSubmit() {
        Integer score = cbRatingValue.getValue();
        if (score == null) return;

        // Lấy nội dung comment hiện tại trong ô nhập (nếu có)
        String commentText = txtCommentContent.getText().trim();
        saveRatingAndCommentToDB(score, commentText.isEmpty() ? null : commentText);

        // Reset ô nhập text sau khi lưu thành công điểm số
        txtCommentContent.clear();
    }

    // ================= CHỨC NĂNG 2: BẤM NÚT SEND COMMENT =================
    @FXML
    public void handleCommentSubmit() {
        String commentText = txtCommentContent.getText().trim();
        if (commentText.isEmpty()) {
            return; // Không nhập gì thì không gửi
        }

        // Khi người dùng bấm Send comment độc lập, ta lấy luôn số sao đang hiển thị ở ComboBox phối hợp đi kèm
        Integer score = cbRatingValue.getValue();
        if (score == null) score = 5; // Dự phòng

        saveRatingAndCommentToDB(score, commentText);
        txtCommentContent.clear(); // Xóa chữ trong hộp văn bản sau khi gửi xong
    }

    private void saveRatingAndCommentToDB(int score, String comment) {
        String getUserIdSql = "SELECT user_id FROM users WHERE username = ?;";

        // Câu lệnh UPSERT vào bảng ratings của bạn
        String upsertRatingSql = "INSERT INTO ratings (user_id, material_id, rating_score, comment, rating_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (user_id, material_id) " +
                "DO UPDATE SET rating_score = EXCLUDED.rating_score, comment = EXCLUDED.comment, rating_date = CURRENT_TIMESTAMP;";

        // ĐÃ SỬA CHỖ NÀY: ÉP KIỂU SANG NUMERIC ĐỂ TRÁNH LỖI PHÉP CHIA CHẶT GÓC INT TRONG POSTGRESQL VÀ LẤY CHUẨN 1 CHỮ SỐ THẬP PHÂN
        String updateAvgRatingSql = "UPDATE materials " +
                "SET avg_rating = ( " +
                "    SELECT COALESCE(ROUND(AVG(rating_score)::numeric, 1), 0.0) " +
                "    FROM ratings " +
                "    WHERE material_id = ? " +
                ") " +
                "WHERE material_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Bật Transaction

            int userId = -1;
            try (PreparedStatement pstmtUser = conn.prepareStatement(getUserIdSql)) {
                pstmtUser.setString(1, downloaderUsername);
                try (ResultSet rs = pstmtUser.executeQuery()) {
                    if (rs.next()) userId = rs.getInt("user_id");
                }
            }

            if (userId != -1) {
                // 1. Ghi nhận lượt đánh giá của User hiện tại (Thêm mới hoặc ghi đè nếu trùng)
                try (PreparedStatement pstmtUpsert = conn.prepareStatement(upsertRatingSql)) {
                    pstmtUpsert.setInt(1, userId);
                    pstmtUpsert.setInt(2, currentMaterial.getMaterialId());
                    pstmtUpsert.setInt(3, score);
                    if (comment != null) {
                        pstmtUpsert.setString(4, comment);
                    } else {
                        pstmtUpsert.setNull(4, java.sql.Types.VARCHAR);
                    }
                    pstmtUpsert.executeUpdate();
                }

                // 2. Tính lại trung bình cộng của TẤT CẢ các user đã đánh giá file này và cập nhật vào bảng materials
                try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateAvgRatingSql)) {
                    pstmtUpdate.setInt(1, currentMaterial.getMaterialId());
                    pstmtUpdate.setInt(2, currentMaterial.getMaterialId());
                    pstmtUpdate.executeUpdate();
                }

                conn.commit(); // Xác nhận lưu thay đổi xuống Database

                // 3. Đọc ngược lại điểm số trung bình tổng mới từ DB lên giao diện
                String getNewAvgSql = "SELECT avg_rating FROM materials WHERE material_id = ?;";
                try (PreparedStatement pstmtGetAvg = conn.prepareStatement(getNewAvgSql)) {
                    pstmtGetAvg.setInt(1, currentMaterial.getMaterialId());
                    try (ResultSet rs = pstmtGetAvg.executeQuery()) {
                        if (rs.next()) {
                            double newAvg = rs.getDouble("avg_rating");
                            lblRating.setText("Rating: " + newAvg + " ⭐");
                            currentMaterial.setAvgRating(newAvg); // Đồng bộ sang đối tượng cục bộ
                        }
                    }
                }

                // Gọi hàm callback (nếu có) để cập nhật hiển thị bảng lớn bên ngoài lập tức
                if (onCloseCallback != null) {
                    onCloseCallback.run();
                }

                // Tải lại khung comment mạng xã hội
                loadComments();

            } else {
                conn.rollback();
                System.out.println("ERORR! CANT NOT FIND THE USERS!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= TẢI BÌNH LUẬN RA GIAO DIỆN KIỂU MẠNG XÃ HỘI =================
    private void loadComments() {
        if (vboxComments == null) return;
        vboxComments.getChildren().clear(); // Dọn sạch giao diện cũ trước khi vẽ

        // Chỉ lấy những bản ghi có ghi nội dung bình luận cụ thể (comment IS NOT NULL)
        String sql = "SELECT u.username, r.rating_score, r.comment, r.rating_date " +
                "FROM ratings r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "WHERE r.material_id = ? AND r.comment IS NOT NULL " +
                "ORDER BY r.rating_date ASC;"; // Sắp xếp thời gian tăng dần từ trên xuống dưới

        SimpleDateFormat df = new SimpleDateFormat("dd/MM HH:mm");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentMaterial.getMaterialId());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String user = rs.getString("username");
                    int score = rs.getInt("rating_score");
                    String text = rs.getString("comment");
                    Timestamp time = rs.getTimestamp("rating_date");

                    // Tạo khung chứa hiển thị thông tin dạng khối mạng xã hội bài bản
                    TextFlow commentNode = new TextFlow();
                    commentNode.setStyle("-fx-background-color: #f1f2f6; -fx-padding: 8; -fx-background-radius: 10;");
                    commentNode.setPrefWidth(430.0);

                    // Tên tài khoản in đậm màu xanh lam
                    Text txtUser = new Text("@" + user + " ");
                    txtUser.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
                    txtUser.setFill(Color.web("#2980b9"));

                    // Thời gian gửi bình luận dạng nhỏ nhạt màu
                    Text txtTime = new Text("(" + df.format(time) + ") ");
                    txtTime.setFont(Font.font("System", javafx.scene.text.FontWeight.NORMAL, 10));
                    txtTime.setFill(Color.GRAY);

                    // Số sao người dùng đó chấm cho tài liệu
                    Text txtStars = new Text("[" + score + "⭐]: ");
                    txtStars.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 11));
                    txtStars.setFill(Color.web("#e67e22"));

                    // Nội dung văn bản ý kiến đóng góp
                    Text txtContent = new Text(text);
                    txtContent.setFont(Font.font("System", 13));

                    commentNode.getChildren().addAll(txtUser, txtTime, txtStars, txtContent);
                    vboxComments.getChildren().add(commentNode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- LOGIC 1: CHỈ TẢI TÀI LIỆU (Giữ nguyên như yêu cầu cũ của bạn) ---
    @FXML
    public void handleDownloadAction() {
        String getUserIdSql = "SELECT user_id FROM users WHERE username = ?";
        String insertDownloadSql = "INSERT INTO downloads (user_id, material_id) VALUES (?, ?)";
        String updateCountSql = "UPDATE materials SET download_count = download_count + 1 WHERE material_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            int userId = -1;
            try (PreparedStatement pstmtUser = conn.prepareStatement(getUserIdSql)) {
                pstmtUser.setString(1, downloaderUsername);
                try (ResultSet rs = pstmtUser.executeQuery()) {
                    if (rs.next()) userId = rs.getInt("user_id");
                }
            }

            if (userId != -1) {
                try (PreparedStatement pstmtInsert = conn.prepareStatement(insertDownloadSql)) {
                    pstmtInsert.setInt(1, userId);
                    pstmtInsert.setInt(2, currentMaterial.getMaterialId());
                    pstmtInsert.executeUpdate();
                }
                try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateCountSql)) {
                    pstmtUpdate.setInt(1, currentMaterial.getMaterialId());
                    pstmtUpdate.executeUpdate();
                }
                conn.commit();

                int newCount = currentMaterial.getDownloadCount() + 1;
                lblDownloads.setText("Total Downloads: " + newCount);

                showSuccessAlert("Download Notification", "Successfully downloaded this document!");
            } else {
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- LOGIC 2: THÊM VÀO THƯ VIỆN YÊU THÍCH (Ghi nhận bảng bookmarks) ---
    @FXML
    public void handleBookmarkAction() {
        String getUserIdSql = "SELECT user_id FROM users WHERE username = ?";
        // Khóa unique (user_id, material_id) ngăn chặn hành vi lưu trùng lặp tài liệu giống nhau
        String insertBookmarkSql = "INSERT INTO bookmarks (user_id, material_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int userId = -1;
            try (PreparedStatement pstmtUser = conn.prepareStatement(getUserIdSql)) {
                pstmtUser.setString(1, downloaderUsername);
                try (ResultSet rs = pstmtUser.executeQuery()) {
                    if (rs.next()) userId = rs.getInt("user_id");
                }
            }

            if (userId != -1) {
                try (PreparedStatement pstmtBookmark = conn.prepareStatement(insertBookmarkSql)) {
                    pstmtBookmark.setInt(1, userId);
                    pstmtBookmark.setInt(2, currentMaterial.getMaterialId());
                    pstmtBookmark.executeUpdate();
                }

                showSuccessAlert("Bookmark Success", "Added to your personal bookmarks library!");
            }
        } catch (   org.postgresql.util.PSQLException e) {
            // Bắt lỗi vi phạm ràng buộc UNIQUE nếu người dùng cố tình bookmark 2 lần
            if (e.getSQLState().equals("23505")) {
                showWarningAlert("Already Bookmarked", "This document is already in your library!");
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSuccessAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private Runnable onCloseCallback;

    // 2. Thêm hàm này để DashboardController truyền lệnh làm mới vào
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    public void handleClose() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleOpenReportPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("report-view.fxml"));
            javafx.scene.Parent root = loader.load();

            ReportController reportController = loader.getController();

            // Truyền toàn bộ dữ liệu hiện tại của tài liệu sang màn hình Report
            reportController.setReportData(
                    currentMaterial.getMaterialId(),
                    currentMaterial.getTitle(),
                    currentMaterial.getCategoryName(), // Thay bằng getter Category thực tế của bạn
                    currentMaterial.getSubjectName(),  // Thay bằng getter Subject thực tế của bạn
                    currentMaterial.getFileUrl(),
                    downloaderUsername // Tài khoản người đang xem và bấm report
            );

            Stage stage = new Stage();
            stage.setTitle("Report Document Violation");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // Giữ popup ở trên cùng
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}