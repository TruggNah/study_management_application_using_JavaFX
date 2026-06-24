package org.example.study_document_managing_project;

public class Material {
    private int materialId;
    private String title;
    private String fileUrl;
    private String uploadedBy;   // Chuỗi chữ (username)
    private String subjectName;  // Chuỗi chữ (subject_name)
    private String categoryName;
    private int downloadCount;
    private double avgRating;
    private String status;

    // Constructor bắt buộc phải có đúng 8 tham số này
    public Material(int materialId, String title, String fileUrl, String uploadedBy,
                    String subjectName, String categoryName, int downloadCount, double avgRating) {
        this.materialId = materialId;
        this.title = title;
        this.fileUrl = fileUrl;
        this.uploadedBy = uploadedBy;
        this.subjectName = subjectName;
        this.categoryName = categoryName;
        this.downloadCount = downloadCount;
        this.avgRating = avgRating;
    }

    // Các hàm Getters giữ nguyên...
    public int getMaterialId() { return materialId; }
    public String getTitle() { return title; }
    public String getFileUrl() { return fileUrl; }
    public String getUploadedBy() { return uploadedBy; }
    public String getSubjectName() { return subjectName; }
    public String getCategoryName() { return categoryName; }
    public int getDownloadCount() { return downloadCount; }
    public double getAvgRating() { return avgRating; }
    public String getStatus() {
        return status;
    }
    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}