package org.example.study_document_managing_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserSearchController {

    @FXML private TextField txtUserSearch;
    @FXML private TableView<UserRowModel> tableUsers;
    @FXML private TableColumn<UserRowModel, String> colUsername;
    @FXML private TableColumn<UserRowModel, String> colEmail;
    @FXML private TableColumn<UserRowModel, String> colDepartment;
    @FXML private TableColumn<UserRowModel, String> colRole;

    @FXML private Label lblUserMaterialsTitle;
    @FXML private TableView<Material> tableUserMaterials;
    @FXML private TableColumn<Material, String> colMatTitle;
    @FXML private TableColumn<Material, String> colMatSubject;
    @FXML private TableColumn<Material, String> colMatCategory;
    @FXML private TableColumn<Material, Double> colMatRating;
    @FXML private TableColumn<Material, Integer> colMatDownloads;

    private ObservableList<UserRowModel> userList = FXCollections.observableArrayList();
    private ObservableList<Material> materialList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Cấu hình các cột của bảng User
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("deptName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Cấu hình các cột của bảng Tài liệu đóng góp
        colMatTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colMatSubject.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colMatCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colMatRating.setCellValueFactory(new PropertyValueFactory<>("avgRating"));
        colMatDownloads.setCellValueFactory(new PropertyValueFactory<>("downloadCount"));

        // Lắng nghe sự kiện click chọn dòng trên bảng User để tải tài liệu tương ứng
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                lblUserMaterialsTitle.setText("Contributed Materials by " + newSelection.getUsername() + ":");
                loadUserMaterials(newSelection.getUserId());
            }
        });

        // Tải toàn bộ danh sách user ban đầu khi vừa mở bảng
        loadAllUsers("");
    }

    @FXML
    private void handleSearchUsers() {
        String keyword = txtUserSearch.getText().trim();
        loadAllUsers(keyword);
    }

    private void loadAllUsers(String keyword) {
        userList.clear();
        materialList.clear();

        // SQL JOIN sang bảng departments thông qua thông tin lưu vết (ví dụ qua subjects/materials hoặc trực tiếp nếu bảng users có department_id)
        // Trường hợp này để chính xác và tổng quát nhất, lấy viện (department) của môn học gần nhất họ đăng tải hoặc liên kết mặc định:
        String sql = "SELECT u.user_id, u.username, u.email, u.role, " +
                "COALESCE((SELECT d.dept_name FROM materials m " +
                "          JOIN subjects s ON m.subject_id = s.subject_id " +
                "          JOIN departments d ON s.department_id = d.department_id " +
                "          WHERE m.uploaded_by = u.user_id LIMIT 1), 'No Department') AS dept_name " +
                "FROM users u WHERE u.status = 'active'";

        if (!keyword.isEmpty()) {
            sql += " AND (u.username ILIKE ? OR u.email ILIKE ?)";
        }
        sql += " ORDER BY u.username ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (!keyword.isEmpty()) {
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userList.add(new UserRowModel(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("dept_name")
                    ));
                }
            }
            tableUsers.setItems(userList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUserMaterials(int userId) {
        materialList.clear();
        String sql = "SELECT m.material_id, m.title, m.file_url, u.username AS owner_name, " +
                "s.subject_name, c.category_name, m.download_count, m.avg_rating " +
                "FROM materials m " +
                "JOIN users u ON m.uploaded_by = u.user_id " +
                "JOIN subjects s ON m.subject_id = s.subject_id " +
                "JOIN categories c ON m.category_id = c.category_id " +
                "WHERE m.uploaded_by = ? AND m.status = 'approved' ORDER BY m.material_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    materialList.add(new Material(
                            rs.getInt("material_id"),
                            rs.getString("title"),
                            rs.getString("file_url"),
                            rs.getString("owner_name"),
                            rs.getString("subject_name"),
                            rs.getString("category_name"),
                            rs.getInt("download_count"),
                            rs.getDouble("avg_rating")
                    ));
                }
            }
            tableUserMaterials.setItems(materialList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Model class phụ trợ chỉ dùng trong màn hình này để map dữ liệu hiển thị bảng User
    public static class UserRowModel {
        private final int userId;
        private final String username;
        private final String email;
        private final String role;
        private final String deptName;

        public UserRowModel(int userId, String username, String email, String role, String deptName) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
            this.deptName = deptName;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getDeptName() { return deptName; }
    }
}