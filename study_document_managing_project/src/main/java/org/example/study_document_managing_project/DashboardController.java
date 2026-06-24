package org.example.study_document_managing_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardController {
    // Các nút Sidebar
    @FXML private Button btnTabBrowse;
    @FXML private Button btnTabUpload;
    @FXML private Button btnTabPersonal;
    @FXML private Button btnLogout;

    // Các vùng giao diện
    @FXML private VBox paneBrowse;
    @FXML private VBox paneUpload;
    @FXML private VBox panePersonal;

    // Các thành phần tra cứu tài liệu (Tab 1)
    @FXML private TextField txtSearch;
    @FXML private TextField txtSearchTitle;
    @FXML private ComboBox<String> cbSubjectSearch;
    @FXML private ComboBox<String> cbCategorySearch;

    @FXML private TableView<Material> tableMaterials;
    @FXML private TableView<Material> tableBookmarks;
    @FXML private TableColumn<Material, String> colBookmarkTitle;
    @FXML private TableColumn<Material, String> colBookmarkOwner;
    @FXML private TableColumn<Material, String> colBookmarkSubject;
    @FXML private TableColumn<Material, String> colBookmarkCategory;// Đảm bảo fx:id cột Title của bảng này là colBookmarkTitle

    private ObservableList<Material> bookmarkList = FXCollections.observableArrayList();
    private ObservableList<Material> materialsList = FXCollections.observableArrayList();

    // 2. KHU VỰC KHAI BÁO CÁC CỘT (Kiểm tra xem đã có đủ dòng này chưa)
    @FXML private TableColumn<Material, String> colTitle;
    @FXML private TableColumn<Material, String> colFileUrl;
    @FXML private TableColumn<Material, String> colUploadedBy;
    @FXML private TableColumn<Material, String> colSubjectName;
    @FXML private TableColumn<Material, String> colCategoryName;
    @FXML private TableColumn<Material, Integer> colDownloadCount;
    @FXML private TableColumn<Material, Double> colAvgRating;
    @FXML private Label lblCurrentUser;
    @FXML private Label lblUserRole;
    // Khai báo các thành phần giao diện Upload từ file FXML
    @FXML private TextField txtUploadTitle;
    @FXML private ComboBox<String> cbUploadSubject;
    @FXML private ComboBox<String> cbUploadCategory;
    @FXML private TextField txtUploadUrl;
    // khai bao myUpload
    @FXML private TableView<Material> tableMyDocuments;
    @FXML private TableColumn<Material, String> colMyDocTitle;
    @FXML private TableColumn<Material, String> colMyDocSubject;
    @FXML private TableColumn<Material, String> colMyDocCategory;
    @FXML private TableColumn<Material, Double> colMyDocRating;
    @FXML private TableColumn<Material, String> colMyDocStatus;

    @FXML private ComboBox<String> cbSortBy;
    @FXML private ComboBox<String> cbTimeFilter;

    private ObservableList<Material> myUploadedList = FXCollections.observableArrayList();


    public void setCurrentUser(String username) {
        if (lblCurrentUser != null) {
            lblCurrentUser.setText("User: " + username);
        }

        // THÊM LOGIC NẠP ROLE CHO LBLUSERROLE (GIỮ NGUYÊN TÊN HÀM)
        if (lblUserRole != null) {
            String sql = "SELECT role FROM users WHERE username = ?;";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("role");

                        if (role != null) {
                            // Hiển thị vai trò dạng viết hoa cạnh Username (Ví dụ: [STUDENT])
                            lblUserRole.setText("[" + role.toUpperCase() + "]");

                            // Đổi màu sắc nhãn phân quyền cho trực quan
                            if ("admin".equalsIgnoreCase(role)) {
                                lblUserRole.setTextFill(javafx.scene.paint.Color.RED); // Admin màu đỏ
                            } else if ("teacher".equalsIgnoreCase(role)) {
                                lblUserRole.setTextFill(javafx.scene.paint.Color.BLUE); // Teacher màu xanh dương
                            } else {
                                lblUserRole.setTextFill(javafx.scene.paint.Color.GRAY); // Student màu xám
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                lblUserRole.setText("[UNKNOWN]");
            }
        }
    }
    // Danh sách lưu trữ tài liệu hiển thị trên bảng
    private ObservableList<Material> materialList = FXCollections.observableArrayList();

    // Hàm initialize chạy tự động khi màn hình Dashboard được nạp
    @FXML
    public void initialize() {

        cbSortBy.getItems().addAll("Normal", "Most Downloads", "Highest Rating");
        cbSortBy.getSelectionModel().selectFirst(); // Mặc định là Normal

        // 2. Khởi tạo danh sách mốc thời gian lọc
        cbTimeFilter.getItems().addAll("All Time", "Past 24 Hours", "Past Week", "Past Month");
        cbTimeFilter.getSelectionModel().selectFirst(); // Mặc định là Toàn thời gian

        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colFileUrl.setCellValueFactory(new PropertyValueFactory<>("fileUrl"));
        colUploadedBy.setCellValueFactory(new PropertyValueFactory<>("uploadedBy"));
        colSubjectName.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colCategoryName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colDownloadCount.setCellValueFactory(new PropertyValueFactory<>("downloadCount"));
        colAvgRating.setCellValueFactory(new PropertyValueFactory<>("avgRating"));
        if (colBookmarkTitle != null) {
            colBookmarkTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (colBookmarkTitle != null) colBookmarkTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colBookmarkOwner != null) colBookmarkOwner.setCellValueFactory(new PropertyValueFactory<>("uploadedBy"));
        if (colBookmarkSubject != null) colBookmarkSubject.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        if (colBookmarkCategory != null) colBookmarkCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        if (colMyDocTitle != null) colMyDocTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (colMyDocSubject != null) colMyDocSubject.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        if (colMyDocCategory != null) colMyDocCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        if (colMyDocRating != null) colMyDocRating.setCellValueFactory(new PropertyValueFactory<>("avgRating"));
        if (colMyDocStatus != null) colMyDocStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableMaterials.setRowFactory(tv -> {
            TableRow<Material> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // Kiểm tra nếu click đúp chuột (2 lần) và dòng đó chứa dữ liệu hợp lệ
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Material selectedMaterial = row.getItem();
                    openMaterialDetailWindow(selectedMaterial); // Gọi hàm mở màn hình chi tiết
                }
            });
            return row;
        });
        loadSearchComboBoxes();
        loadMaterialsData("");
    }

    private void loadSearchComboBoxes() {
        if (cbSubjectSearch == null || cbCategorySearch == null) return;

        cbSubjectSearch.getItems().clear();
        cbCategorySearch.getItems().clear();

        // Thêm lựa chọn mặc định hiển thị tất cả
        cbSubjectSearch.getItems().add("All Subjects");
        cbCategorySearch.getItems().add("All Categories");

        // Lấy dữ liệu môn học từ DB
        String querySubjects = "SELECT subject_name FROM subjects";
        String queryCategories = "SELECT category_name FROM categories";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement pst = conn.prepareStatement(querySubjects); ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    cbSubjectSearch.getItems().add(rs.getString("subject_name"));
                }
            }
            try (PreparedStatement pst = conn.prepareStatement(queryCategories); ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    cbCategorySearch.getItems().add(rs.getString("category_name"));
                }
            }

            // Chọn phần tử mặc định ban đầu
            cbSubjectSearch.getSelectionModel().selectFirst();
            cbCategorySearch.getSelectionModel().selectFirst();

        } catch (Exception e) {
            e.printStackTrace();
        }
        cbSubjectSearch.setOnAction(e -> handleSearchAction());
        cbCategorySearch.setOnAction(e -> handleSearchAction());
    }

    private void openMaterialDetailWindow(Material material) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("material-detail-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            MaterialDetailController detailController = fxmlLoader.getController();

            // LẤY USERNAME ĐANG ĐĂNG NHẬP từ nhãn lblCurrentUser (ví dụ: "User: trungnah" -> cắt chuỗi lấy "trungnah")
            String currentUsername = lblCurrentUser.getText().replace("User: ", "").trim();

            // Truyền cả thông tin tài liệu VÀ username người tải sang màn hình chi tiết
            detailController.setMaterialData(material, currentUsername);
            detailController.setOnCloseCallback(() -> {
                System.out.println(">>> Reloading the table of details..");

                // Lấy từ khóa hiện tại trong ô tìm kiếm (nếu có) để lọc cho chuẩn
                String keyword = (txtSearchTitle != null) ? txtSearchTitle.getText() : "";

                // Gọi lại hàm load dữ liệu chính của bạn để nạp điểm số trung bình mới
                loadMaterialsData(keyword);
            });
            Stage detailStage = new Stage();
            detailStage.setScene(scene);
            detailStage.setTitle("DOCUMENT INFORMATION - " + material.getTitle());
            detailStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            detailStage.centerOnScreen();
            detailStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearch() {
        // Lấy chữ người dùng gõ, loại bỏ khoảng trắng thừa ở 2 đầu
        String keyword = txtSearch.getText().trim();

        // Gọi hàm nạp dữ liệu với từ khóa này
        loadMaterialsData(keyword);
    }

    @FXML
    public void handleSearchAction() {
        String keyword = (txtSearchTitle != null) ? txtSearchTitle.getText() : "";

        System.out.println("Now is Searching...");
        // Gọi hàm lọc tổng hợp
        loadMaterialsData(keyword);
    }

    @FXML
    public void handleMenuLibraryClick(ActionEvent event) {
        // 1. Ẩn các Pane khác, hiển thị Pane cá nhân lên
        paneBrowse.setVisible(false);
        paneUpload.setVisible(false);
        panePersonal.setVisible(true); // Pane chứa 2 bảng của bạn

        // 2. BẮT BUỘC KHỞI CHẠY HÀM NÀY ĐỂ QUÉT DATABASE NGAY KHI MỞ TAB
        loadUserBookmarks();
    }

    public void loadMaterialsData(String searchText) {
        if (tableMaterials == null) return;

        materialsList.clear();

        // 1. Lấy giá trị đang được chọn từ các ComboBox lọc trên thanh công cụ
        String selectedSubject = (cbSubjectSearch != null) ? cbSubjectSearch.getValue() : "All Subjects";
        String selectedCategory = (cbCategorySearch != null) ? cbCategorySearch.getValue() : "All Categories";

        // Thêm: Lấy giá trị từ 2 ComboBox mới lọc nâng cao (Tránh NullPointerException)
        String selectedSort = (cbSortBy != null && cbSortBy.getValue() != null) ? cbSortBy.getValue() : "Normal";
        String selectedTime = (cbTimeFilter != null && cbTimeFilter.getValue() != null) ? cbTimeFilter.getValue() : "All Time";

        // 2. Xây dựng câu lệnh SQL động gốc
        StringBuilder sql = new StringBuilder(
                "SELECT m.material_id, m.title, m.file_url, u.username, s.subject_name, c.category_name, m.download_count, m.avg_rating " +
                        "FROM materials m " +
                        "LEFT JOIN users u ON m.uploaded_by = u.user_id " +
                        "LEFT JOIN subjects s ON m.subject_id = s.subject_id " +
                        "LEFT JOIN categories c ON m.category_id = c.category_id " +
                        "WHERE m.status = 'approved' "
        );

        // --- BỘ LỌC CŨ CỦA BẠN ---
        // Lọc theo từ khóa ô Text
        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND LOWER(m.title) LIKE ? ");
        }

        // Lọc theo Môn học
        if (selectedSubject != null && !selectedSubject.equals("All Subjects")) {
            sql.append(" AND s.subject_name = ? ");
        }

        // Lọc theo Danh mục
        if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
            sql.append(" AND c.category_name = ? ");
        }

        // --- BỘ LỌC THỜI GIAN MỚI TÍCH HỢP ---
        // Sử dụng subquery quét bảng ghi nhận lịch sử downloads để lọc khoảng thời gian PostgreSQL
        if ("Past 24 Hours".equals(selectedTime)) {
            sql.append(" AND m.material_id IN (SELECT DISTINCT material_id FROM downloads WHERE download_date >= NOW() - INTERVAL '1 day') ");
        } else if ("Past Week".equals(selectedTime)) {
            sql.append(" AND m.material_id IN (SELECT DISTINCT material_id FROM downloads WHERE download_date >= NOW() - INTERVAL '1 week') ");
        } else if ("Past Month".equals(selectedTime)) {
            sql.append(" AND m.material_id IN (SELECT DISTINCT material_id FROM downloads WHERE download_date >= NOW() - INTERVAL '1 month') ");
        }

        // --- TIÊU CHÍ SẮP XẾP MỚI TÍCH HỢP (Thay thế cho ORDER BY mặc định) ---
        if ("Most Downloads".equals(selectedSort)) {
            sql.append(" ORDER BY m.download_count DESC, m.material_id DESC ");
        } else if ("Highest Rating".equals(selectedSort)) {
            sql.append(" ORDER BY m.avg_rating DESC, m.material_id DESC ");
        } else {
            // "Normal": Sắp xếp ID mới nhất lên đầu như code ban đầu của bạn
            sql.append(" ORDER BY m.material_id DESC ");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            // Gán tham số từ khóa Text (Cũ)
            if (searchText != null && !searchText.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + searchText.trim().toLowerCase() + "%");
            }

            // Gán tham số Môn học (Cũ)
            if (selectedSubject != null && !selectedSubject.equals("All Subjects")) {
                pstmt.setString(paramIndex++, selectedSubject);
            }

            // Gán tham số Danh mục (Cũ)
            if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
                pstmt.setString(paramIndex++, selectedCategory);
            }

            // 3. Thực thi truy vấn và đổ vào TableView
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Material material = new Material(
                            rs.getInt("material_id"),
                            rs.getString("title"),
                            rs.getString("file_url"),
                            rs.getString("username"),
                            rs.getString("subject_name"),
                            rs.getString("category_name"),
                            rs.getInt("download_count"),
                            rs.getDouble("avg_rating")
                    );
                    materialsList.add(material);
                }
            }

            tableMaterials.setItems(materialsList);
            System.out.println("Found " + materialsList.size() + " resources matching filter criteria.");

        } catch (Exception e) {
            System.out.println("Cannot find or filter the description:");
            e.printStackTrace();
        }
    }

    // Hàm chuyển đổi các Tab nội dung
    @FXML
    public void handleSwitchTab(ActionEvent event) {
        // 1. Ẩn tất cả các Pane giao diện trước để tránh chồng đè lên nhau
        if (paneBrowse != null) paneBrowse.setVisible(false);
        if (paneUpload != null) paneUpload.setVisible(false);
        if (panePersonal != null) panePersonal.setVisible(false);
        if (paneAdminPanel != null) paneAdminPanel.setVisible(false);

        // 2. Kiểm tra xem nút nào đã kích hoạt sự kiện handleSwitchTab
        if (event.getSource() == btnTabBrowse) {
            if (paneBrowse != null) {
                paneBrowse.setVisible(true);
                loadMaterialsData(""); // Nạp dữ liệu bảng tìm kiếm chính
            }
        }
        else if (event.getSource() == btnTabUpload) {
            if (paneUpload != null) {
                paneUpload.setVisible(true);
                initUploadPaneData();
            }
        }
        // CHÍNH LÀ CHỖ NÀY: Khi người dùng bấm nút My Library (btnTabPersonal)
        else if (event.getSource() == btnTabPersonal) {
            if (panePersonal != null) {
                panePersonal.setVisible(true); // Hiển thị vùng thư viện cá nhân

                // TỰ ĐỘNG CHẠY: Quét Database và đổ dữ liệu yêu thích lên bảng Bookmarks ngay lập tức!
                System.out.println("Uploading data from database now...");
                loadUserBookmarks();
                loadMyUploadedDocuments();
            } else {
                System.out.println("Erorr! Can't connect to paneUsers from file.FXML!");
            }
        }
    }

    // Hàm Đăng xuất
    @FXML
    public void handleLogout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("STUDENT DOCUMENT MANAGEMENT SYSTEMS");
            stage.setMaximized(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadUserBookmarks() {
        if (lblCurrentUser == null || tableBookmarks == null) return;

        bookmarkList.clear();

        // Đảm bảo lấy chuẩn xác Username (Ví dụ: "User: trungnah" -> tách lấy "trungnah")
        String currentUsername = lblCurrentUser.getText().replace("User:", "").trim();

        if (currentUsername.isEmpty() || currentUsername.equalsIgnoreCase("Guest")) {
            System.out.println("Cảnh báo: Chưa xác định được tài khoản hiện tại để lấy Bookmarks!");
            return;
        }

        String sql = "SELECT m.material_id, m.title, m.file_url, u_owner.username AS owner_name, " +
                "s.subject_name, c.category_name, m.download_count, m.avg_rating " +
                "FROM bookmarks b " +
                "JOIN users u_action ON b.user_id = u_action.user_id " +
                "JOIN materials m ON b.material_id = m.material_id " +
                "LEFT JOIN users u_owner ON m.uploaded_by = u_owner.user_id " +
                "LEFT JOIN subjects s ON m.subject_id = s.subject_id " +
                "LEFT JOIN categories c ON m.category_id = c.category_id " +
                "WHERE u_action.username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, currentUsername);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookmarkList.add(new Material(
                            rs.getInt("material_id"),
                            rs.getString("title"),
                            rs.getString("file_url"),
                            rs.getString("owner_name"),    // Sẽ map vào colBookmarkOwner
                            rs.getString("subject_name"),  // Sẽ map vào colBookmarkSubject
                            rs.getString("category_name"), // Sẽ map vào colBookmarkCategory
                            rs.getInt("download_count"),
                            rs.getDouble("avg_rating")
                    ));
                }
            }

            tableBookmarks.setItems(bookmarkList);
            System.out.println("Đã nạp thành công " + bookmarkList.size() + " tài liệu yêu thích của " + currentUsername);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Hàm này phải được gọi khi người dùng bấm vào Tab "Upload Document" bên menu trái
    public void initUploadPaneData() {
        if (cbUploadSubject == null || cbUploadCategory == null) return;

        cbUploadSubject.getItems().clear();
        cbUploadCategory.getItems().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Đổ dữ liệu vào ComboBox Subject
            String sqlSubjects = "SELECT subject_name FROM subjects";
            try (PreparedStatement pst = conn.prepareStatement(sqlSubjects); ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    cbUploadSubject.getItems().add(rs.getString("subject_name"));
                }
            }

            // 2. Đổ dữ liệu vào ComboBox Category
            String sqlCategories = "SELECT category_name FROM categories";
            try (PreparedStatement pst = conn.prepareStatement(sqlCategories); ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    cbUploadCategory.getItems().add(rs.getString("category_name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SỰ KIỆN: Khi người dùng nhấn nút "Submit for Approval"


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }@FXML
    public void handleSubmitForApproval() {
        String title = txtUploadTitle.getText().trim();
        String subjectName = cbUploadSubject.getValue();
        String categoryName = cbUploadCategory.getValue();
        String fileUrl = txtUploadUrl.getText().trim();

        // Kiểm tra dữ liệu đầu vào không được bỏ trống
        if (title.isEmpty() || subjectName == null || categoryName == null || fileUrl.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields and select options!");
            return;
        }

        // Cắt chuỗi lấy username đang đăng nhập để định danh tác giả (uploaded_by)
        String currentUsername = lblCurrentUser.getText().replace("User:", "").trim();

        // KIỂM TRA TRẠNG THÁI BIẾN STATIC: Nếu true -> 'approved', nếu false -> 'pending'
        String currentStatus = isAutoApprove ? "approved" : "pending";

        // Thay thế chữ 'approved' cứng ban đầu bằng dấu ? để truyền động giá trị currentStatus
        String getIdsAndInsertSql =
                "INSERT INTO materials (title, file_url, status, uploaded_by, subject_id, category_id) " +
                        "VALUES (?, ?, ?, " +
                        "   (SELECT user_id FROM users WHERE username = ?), " +
                        "   (SELECT subject_id FROM subjects WHERE subject_name = ?), " +
                        "   (SELECT category_id FROM categories WHERE category_name = ?)" +
                        ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getIdsAndInsertSql)) {

            // Đẩy tham số vào câu lệnh SQL theo đúng thứ tự mới
            pstmt.setString(1, title);
            pstmt.setString(2, fileUrl);
            pstmt.setString(3, currentStatus); // Tham số status động thay thế chỗ cũ
            pstmt.setString(4, currentUsername);
            pstmt.setString(5, subjectName);
            pstmt.setString(6, categoryName);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                // Tùy biến thông báo dựa theo trạng thái bật/tắt để người dùng dễ theo dõi
                if ("approved".equals(currentStatus)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Your material has been successfully uploaded and approved automatically!");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Submitted", "Your material has been submitted successfully! Waiting for admin approval.");
                }

                loadMyUploadedDocuments();
                // Xóa trắng form sau khi thêm thành công để sẵn sàng nhập file tiếp theo
                txtUploadTitle.clear();
                txtUploadUrl.clear();
                cbUploadSubject.getSelectionModel().clearSelection();
                cbUploadCategory.getSelectionModel().clearSelection();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while saving to database!");
        }
    }

    public void loadMyUploadedDocuments() {
        if (tableMyDocuments == null) return;

        myUploadedList.clear();

        // Lấy tên tài khoản hiện tại an toàn
        String currentUsername = "";
        if (lblCurrentUser != null && lblCurrentUser.getText() != null) {
            currentUsername = lblCurrentUser.getText().replace("User:", "").replace("User :", "").trim();
        }
        if (currentUsername.isEmpty()) currentUsername = "student_nam_it"; // Dự phòng nick test

        String sql = "SELECT m.material_id, m.title, m.file_url, m.status, m.avg_rating, " +
                "s.subject_name, c.category_name " +
                "FROM materials m " +
                "JOIN users u ON m.uploaded_by = u.user_id " +
                "LEFT JOIN subjects s ON m.subject_id = s.subject_id " +
                "LEFT JOIN categories c ON m.category_id = c.category_id " +
                "WHERE u.username = ? " +
                "ORDER BY m.material_id DESC"; // Tài liệu mới lên sẽ xếp trên cùng

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, currentUsername);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Tạo đối tượng Material tạm thời để chứa thông tin hiển thị lên bảng
                    Material mat = new Material(
                            rs.getInt("material_id"),
                            rs.getString("title"),
                            rs.getString("file_url"),
                            currentUsername,
                            rs.getString("subject_name"),
                            rs.getString("category_name"),
                            0, // download_count tạm thời không cần hiện ở bảng này
                            rs.getDouble("avg_rating")
                    );
                    mat.setStatus(rs.getString("status")); // Đảm bảo đối tượng Material của bạn có thuộc tính setStatus()

                    myUploadedList.add(mat);
                }
            }

            tableMyDocuments.setItems(myUploadedList);
            System.out.println(">>> Đã nạp thành công " + myUploadedList.size() + " tài liệu do bạn đăng tải.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ==========================================
// KHAI BÁO BIẾN CHO TÍNH NĂNG ADMIN TOOLS (GIỮ NGUYÊN HOÀN TOÀN)
// ==========================================
    @FXML private VBox paneAdminPanel;
    @FXML private Button btnAdminTools;
    @FXML private TextField txtDeptName, txtSubjectName, txtCategoryName;
    @FXML private ComboBox<String> cbDeptSelector;

    @FXML private TableView<org.example.study_document_managing_project.DepartmentModel> tableDepartments;
    @FXML private TableColumn<org.example.study_document_managing_project.DepartmentModel, Integer> colDeptId;
    @FXML private TableColumn<org.example.study_document_managing_project.DepartmentModel, String> colDeptName;

    @FXML private TableView<org.example.study_document_managing_project.SubjectModel> tableSubjects;
    @FXML private TableColumn<org.example.study_document_managing_project.SubjectModel, Integer> colSubId;
    @FXML private TableColumn<org.example.study_document_managing_project.SubjectModel, String> colSubName;
    @FXML private TableColumn<org.example.study_document_managing_project.SubjectModel, String> colSubDept;

    @FXML private TableView<org.example.study_document_managing_project.CategoryModel> tableCategories;
    @FXML private TableColumn<org.example.study_document_managing_project.CategoryModel, Integer> colCatId;
    @FXML private TableColumn<org.example.study_document_managing_project.CategoryModel, String> colCatName;

    // --- Tab Approve Materials: Khai báo bổ sung cột colPendUserId và colPendDept ---
    @FXML private TableView<org.example.study_document_managing_project.MaterialModelAdmin> tablePendingMaterials;
    @FXML private TableColumn<org.example.study_document_managing_project.MaterialModelAdmin, Integer> colPendId;
    @FXML private TableColumn<org.example.study_document_managing_project.MaterialModelAdmin, String> colPendTitle;
    @FXML private TableColumn<org.example.study_document_managing_project.MaterialModelAdmin, String> colPendUrl;
    @FXML private TableColumn<org.example.study_document_managing_project.MaterialModelAdmin, String> colPendSubject;
    @FXML private TableColumn<org.example.study_document_managing_project.MaterialModelAdmin, String> colPendCategory;
    @FXML private TableColumn<org.example.study_document_managing_project.MaterialModelAdmin, Integer> colPendUserId;   // THÊM MỚI
    @FXML private TableColumn<org.example.study_document_managing_project.MaterialModelAdmin, String> colPendDept;       // THÊM MỚI

    // --- Tab Reports Management: Khai báo bổ sung colRepUserId, colRepCat, colRepSub, colRepDept ---
    @FXML private TableView<org.example.study_document_managing_project.ReportModelAdmin> tableReports;
    @FXML private TableColumn<org.example.study_document_managing_project.ReportModelAdmin, Integer> colRepId;
    @FXML private TableColumn<org.example.study_document_managing_project.ReportModelAdmin, String> colRepMaterialTitle;
    @FXML private TableColumn<org.example.study_document_managing_project.ReportModelAdmin, String> colRepReporter;
    @FXML private TableColumn<org.example.study_document_managing_project.ReportModelAdmin, String> colRepReason;
    @FXML private TableColumn<org.example.study_document_managing_project.ReportModelAdmin, String> colRepDate;
    @FXML private TableColumn<org.example.study_document_managing_project.ReportModelAdmin, Integer> colRepUserId;       // THÊM MỚI
    @FXML private TableColumn<org.example.study_document_managing_project.ReportModelAdmin, String> colRepCat;          // THÊM MỚI
    @FXML private TableColumn<org.example.study_document_managing_project.ReportModelAdmin, String> colRepSub;          // THÊM MỚI
    @FXML private TableColumn<org.example.study_document_managing_project.ReportModelAdmin, String> colRepDept;         // THÊM MỚI

    // ==========================================
    // LOGIC PHÂN QUYỀN VÀ KHỞI TẠO TAB ADMIN
    // ==========================================
    @FXML
    public void handleMenuAdminClick() {
        String currentRole = lblUserRole.getText().replace("[", "").replace("]", "").trim().toLowerCase();

        if (!"admin".equals(currentRole)) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Access Denied");
            alert.setHeaderText(null);
            alert.setContentText("You dont have permisson to using this application!");
            alert.showAndWait();
            return;
        }

        paneBrowse.setVisible(false);
        paneUpload.setVisible(false);
        panePersonal.setVisible(false);
        paneAdminPanel.setVisible(true);

        refreshAdminData();
    }

    private void refreshAdminData() {
        updateAutoApproveButtonUI();
        colDeptId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colDeptName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));

        colSubId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colSubName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colSubDept.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("deptName"));

        colCatId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colCatName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));

        loadAdminDepartments();
        loadAdminSubjects();
        loadAdminCategories();
        loadPendingMaterials();
        loadAdminReports();
        loadAdminUsers();
    }

    // ==========================================
    // THAO TÁC CƠ SỞ DỮ LIỆU (DATABASE CRUD)
    // ==========================================

    // 1. Quản lý Khoa / Viện (Departments)
    private void loadAdminDepartments() {
        javafx.collections.ObservableList<org.example.study_document_managing_project.DepartmentModel> list = javafx.collections.FXCollections.observableArrayList();
        cbDeptSelector.getItems().clear();
        String sql = "SELECT * FROM departments ORDER BY department_id DESC;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new org.example.study_document_managing_project.DepartmentModel(rs.getInt("department_id"), rs.getString("dept_name")));
                cbDeptSelector.getItems().add(rs.getString("dept_name"));
            }
            tableDepartments.setItems(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAddDepartment() {
        String name = txtDeptName.getText().trim();
        if (name.isEmpty()) return;
        String sql = "INSERT INTO departments (dept_name) VALUES (?);";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            txtDeptName.clear();
            loadAdminDepartments();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleDeleteDepartment() {
        org.example.study_document_managing_project.DepartmentModel selected = tableDepartments.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String sql = "DELETE FROM departments WHERE department_id = ?;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getId());
            pstmt.executeUpdate();
            loadAdminDepartments();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 2. Quản lý Môn học (Subjects)
    private void loadAdminSubjects() {
        javafx.collections.ObservableList<org.example.study_document_managing_project.SubjectModel> list = javafx.collections.FXCollections.observableArrayList();
        String sql = "SELECT s.subject_id, s.subject_name, d.dept_name FROM subjects s LEFT JOIN departments d ON s.department_id = d.department_id ORDER BY s.subject_id DESC;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new org.example.study_document_managing_project.SubjectModel(rs.getInt("subject_id"), rs.getString("subject_name"), rs.getString("dept_name")));
            }
            tableSubjects.setItems(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAddSubject() {
        String name = txtSubjectName.getText().trim();
        String deptName = cbDeptSelector.getValue();
        if (name.isEmpty() || deptName == null) return;
        String sql = "INSERT INTO subjects (subject_name, department_id) VALUES (?, (SELECT department_id FROM departments WHERE dept_name = ?));";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, deptName);
            pstmt.executeUpdate();
            txtSubjectName.clear();
            loadAdminSubjects();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleDeleteSubject() {
        org.example.study_document_managing_project.SubjectModel selected = tableSubjects.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String sql = "DELETE FROM subjects WHERE subject_id = ?;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getId());
            pstmt.executeUpdate();
            loadAdminSubjects();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 3. Quản lý Loại danh mục (Categories)
    private void loadAdminCategories() {
        javafx.collections.ObservableList<org.example.study_document_managing_project.CategoryModel> list = javafx.collections.FXCollections.observableArrayList();
        String sql = "SELECT * FROM categories ORDER BY category_id DESC;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new org.example.study_document_managing_project.CategoryModel(rs.getInt("category_id"), rs.getString("category_name")));
            }
            tableCategories.setItems(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAddCategory() {
        String name = txtCategoryName.getText().trim();
        if (name.isEmpty()) return;
        String sql = "INSERT INTO categories (category_name) VALUES (?);";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            txtCategoryName.clear();
            loadAdminCategories();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleDeleteCategory() {
        org.example.study_document_managing_project.CategoryModel selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String sql = "DELETE FROM categories WHERE category_id = ?;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getId());
            pstmt.executeUpdate();
            loadAdminCategories();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 4. Quản lý duyệt bài viết (Approve Materials) - ĐÃ THÊM USER_ID VÀ DEPARTMENTS
    @FXML
    public void loadPendingMaterials() {
        if (tablePendingMaterials == null) return;
        colPendId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colPendTitle.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("title"));
        colPendUrl.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fileUrl"));
        colPendSubject.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("subjectName"));
        colPendCategory.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("categoryName"));

        // Đăng ký Map dữ liệu cho 2 cột mới thêm
        if (colPendUserId != null) colPendUserId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("userId"));
        if (colPendDept != null) colPendDept.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("deptName"));

        javafx.collections.ObservableList<org.example.study_document_managing_project.MaterialModelAdmin> list = javafx.collections.FXCollections.observableArrayList();

        // SQL nâng cấp JOIN thêm bảng departments để lấy dept_name và lấy m.uploaded_by làm user_id
        String sql = "SELECT m.material_id, m.title, m.file_url, s.subject_name, c.category_name, m.uploaded_by, d.dept_name " +
                "FROM materials m " +
                "LEFT JOIN subjects s ON m.subject_id = s.subject_id " +
                "LEFT JOIN categories c ON m.category_id = c.category_id " +
                "LEFT JOIN departments d ON s.department_id = d.department_id " +
                "WHERE m.status = 'pending' ORDER BY m.material_id ASC;";

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new org.example.study_document_managing_project.MaterialModelAdmin(
                        rs.getInt("material_id"), rs.getString("title"), rs.getString("file_url"),
                        rs.getString("subject_name"), rs.getString("category_name"),
                        rs.getInt("uploaded_by"), rs.getString("dept_name") // Đẩy 2 biến mới vào Constructor
                ));
            }
            tablePendingMaterials.setItems(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleApproveMaterial() {
        org.example.study_document_managing_project.MaterialModelAdmin selected = tablePendingMaterials.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String sql = "UPDATE materials SET status = 'approved' WHERE material_id = ?;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getId());
            pstmt.executeUpdate();
            loadPendingMaterials();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleRejectMaterial() {
        org.example.study_document_managing_project.MaterialModelAdmin selected = tablePendingMaterials.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String sql = "UPDATE materials SET status = 'rejected' WHERE material_id = ?;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getId());
            pstmt.executeUpdate();
            loadPendingMaterials();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 5. Xử lý Tab tố cáo (Reports Management) - ĐÃ THÊM USER_ID, CATEGORY, SUBJECT, DEPARTMENT
    @FXML
    private void handleDismissReport() {
        org.example.study_document_managing_project.ReportModelAdmin selected = tableReports.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String sql = "UPDATE reports SET status = 'processed' WHERE report_id = ?;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getReportId());
            pstmt.executeUpdate();
            loadAdminReports();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleDeleteReportedMaterial() {
        org.example.study_document_managing_project.ReportModelAdmin selected = tableReports.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String deleteMaterialSql = "DELETE FROM materials WHERE material_id = ?;";
        String updateReportSql = "UPDATE reports SET status = 'processed' WHERE material_id = ?;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (java.sql.PreparedStatement pstmt1 = conn.prepareStatement(updateReportSql);
                 java.sql.PreparedStatement pstmt2 = conn.prepareStatement(deleteMaterialSql)) {
                pstmt1.setInt(1, selected.getMaterialId());
                pstmt1.executeUpdate();
                pstmt2.setInt(1, selected.getMaterialId());
                pstmt2.executeUpdate();
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
            loadAdminReports();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==========================================
// KHAI BÁO BIẾN CHO TAB QUẢN LÝ NGƯỜI DÙNG (USERS)
// ==========================================
    @FXML private TableView<org.example.study_document_managing_project.UserModelAdmin> tableUsers;
    @FXML private TableColumn<org.example.study_document_managing_project.UserModelAdmin, Integer> colUsrId;
    @FXML private TableColumn<org.example.study_document_managing_project.UserModelAdmin, String> colUsrName;
    @FXML private TableColumn<org.example.study_document_managing_project.UserModelAdmin, String> colUsrEmail;
    @FXML private TableColumn<org.example.study_document_managing_project.UserModelAdmin, String> colUsrRole;
    @FXML private TableColumn<org.example.study_document_managing_project.UserModelAdmin, Integer> colUsrUploads;
    @FXML private TableColumn<org.example.study_document_managing_project.UserModelAdmin, String> colUsrStatus;

    @FXML
    public void loadAdminReports() {
        if (tableReports == null) return;
        colRepId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("reportId"));
        colRepMaterialTitle.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("materialTitle"));
        colRepReporter.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("reporterName"));
        colRepReason.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("reason"));
        colRepDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("reportDate"));

        // Đăng ký map dữ liệu cho các cột yêu cầu thêm mới ở tab Tố cáo
        if (colRepUserId != null) colRepUserId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("userId"));
        if (colRepCat != null) colRepCat.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("categoryName"));
        if (colRepSub != null) colRepSub.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("subjectName"));
        if (colRepDept != null) colRepDept.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("deptName"));

        javafx.collections.ObservableList<org.example.study_document_managing_project.ReportModelAdmin> list = javafx.collections.FXCollections.observableArrayList();

        // SQL nâng cấp liên kết chuỗi từ reports -> materials -> categories, subjects, departments để lôi hết thông tin ra
        String sql = "SELECT r.report_id, r.material_id, m.title AS material_title, u.username AS reporter, r.reason, r.report_date, " +
                "r.user_id, c.category_name, s.subject_name, d.dept_name " +
                "FROM reports r " +
                "LEFT JOIN materials m ON r.material_id = m.material_id " +
                "LEFT JOIN users u ON r.user_id = u.user_id " +
                "LEFT JOIN categories c ON m.category_id = c.category_id " +
                "LEFT JOIN subjects s ON m.subject_id = s.subject_id " +
                "LEFT JOIN departments d ON s.department_id = d.department_id " +
                "WHERE r.status = 'pending' ORDER BY r.report_id DESC;";

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new org.example.study_document_managing_project.ReportModelAdmin(
                        rs.getInt("report_id"), rs.getInt("material_id"), rs.getString("material_title"),
                        rs.getString("reporter"), rs.getString("reason"), rs.getString("report_date"),
                        rs.getInt("user_id"), rs.getString("category_name"), rs.getString("subject_name"), rs.getString("dept_name")
                ));
            }
            tableReports.setItems(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void loadAdminUsers() {
        colUsrId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("userId"));
        colUsrName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("username"));
        colUsrEmail.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));
        colUsrRole.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("role"));
        colUsrUploads.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("contributedCount"));
        colUsrStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        javafx.collections.ObservableList<org.example.study_document_managing_project.UserModelAdmin> list = javafx.collections.FXCollections.observableArrayList();
        String sql = "SELECT u.user_id, u.username, u.email, u.role, u.status, COUNT(m.material_id) AS contributed_count " +
                "FROM users u LEFT JOIN materials m ON u.user_id = m.uploaded_by " +
                "GROUP BY u.user_id, u.username, u.email, u.role, u.status ORDER BY u.user_id ASC;";

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new org.example.study_document_managing_project.UserModelAdmin(
                        rs.getInt("user_id"), rs.getString("username"), rs.getString("email"),
                        rs.getString("role"), rs.getInt("contributed_count"), rs.getString("status")
                ));
            }
            tableUsers.setItems(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLockUser() {
        org.example.study_document_managing_project.UserModelAdmin selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String sql = "UPDATE users SET status = 'locked' WHERE user_id = ?;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getUserId());
            pstmt.executeUpdate();
            loadAdminUsers();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleUnlockUser() {
        org.example.study_document_managing_project.UserModelAdmin selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String sql = "UPDATE users SET status = 'active' WHERE user_id = ?;";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getUserId());
            pstmt.executeUpdate();
            loadAdminUsers();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private javafx.scene.control.ToggleButton btnToggleAutoApprove;

    // Biến static lưu trạng thái Auto Approve (mặc định ban đầu là false - OFF)
    private static boolean isAutoApprove = false;

    private void updateAutoApproveButtonUI() {
        if (btnToggleAutoApprove == null) return;

        // Đồng bộ giao diện dựa vào biến boolean isAutoApprove thay vì đọc DB
        btnToggleAutoApprove.setSelected(isAutoApprove);
        if (isAutoApprove) {
            btnToggleAutoApprove.setText("Auto Approve: ON");
            btnToggleAutoApprove.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;"); // Màu xanh lá
        } else {
            btnToggleAutoApprove.setText("Auto Approve: OFF");
            btnToggleAutoApprove.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;"); // Màu xám
        }
    }

    @FXML
    private void handleToggleAutoApprove() {
        // Đảo trạng thái của biến khi bấm nút
        isAutoApprove = btnToggleAutoApprove.isSelected();

        // Cập nhật lại giao diện nút
        updateAutoApproveButtonUI();
    }
    @FXML
    private void handleOpenSearchUsersPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user-search-view.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Search Users System");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // Hiện đè popup khóa màn hình chính tạm thời
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Could not open Search Users window!");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleExitApplication() {
        // Tạo một hộp thoại xác nhận nhỏ để tránh người dùng bấm nhầm
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to exit the application?");

        // Nếu người dùng chọn OK thì sẽ đóng toàn bộ chương trình một cách an toàn
        if (alert.showAndWait().get() == ButtonType.OK) {
            javafx.application.Platform.exit(); // Đóng các luồng giao diện của JavaFX
            System.exit(0);                      // Thoát tiến trình chạy của hệ thống
        }
    }
}