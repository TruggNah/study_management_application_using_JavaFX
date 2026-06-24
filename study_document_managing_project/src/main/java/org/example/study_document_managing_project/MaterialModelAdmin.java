package org.example.study_document_managing_project;

public class MaterialModelAdmin {
    private int id;
    private String title;
    private String fileUrl;
    private String subjectName;
    private String categoryName;
    private int userId;       // THÊM MỚI
    private String deptName;   // THÊM MỚI

    // Constructor cập nhật đầy đủ tham số
    public MaterialModelAdmin(int id, String title, String fileUrl, String subjectName, String categoryName, int userId, String deptName) {
        this.id = id;
        this.title = title;
        this.fileUrl = fileUrl;
        this.subjectName = subjectName;
        this.categoryName = categoryName;
        this.userId = userId;
        this.deptName = deptName;
    }

    // Các hàm Getter/Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getUserId() { return userId; } // THÊM MỚI
    public void setUserId(int userId) { this.userId = userId; }

    public String getDeptName() { return deptName; } // THÊM MỚI
    public void setDeptName(String deptName) { this.deptName = deptName; }
}