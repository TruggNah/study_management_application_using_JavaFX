package org.example.study_document_managing_project;

public class ReportModelAdmin {
    private int reportId;
    private int materialId;
    private String materialTitle;
    private String reporterName;
    private String reason;
    private String reportDate;
    private int userId;           // THÊM MỚI
    private String categoryName;  // THÊM MỚI
    private String subjectName;   // THÊM MỚI
    private String deptName;      // THÊM MỚI

    // Constructor cập nhật đầy đủ tham số
    public ReportModelAdmin(int reportId, int materialId, String materialTitle, String reporterName, String reason, String reportDate, int userId, String categoryName, String subjectName, String deptName) {
        this.reportId = reportId;
        this.materialId = materialId;
        this.materialTitle = materialTitle;
        this.reporterName = reporterName;
        this.reason = reason;
        this.reportDate = reportDate;
        this.userId = userId;
        this.categoryName = categoryName;
        this.subjectName = subjectName;
        this.deptName = deptName;
    }

    // Các hàm Getter/Setter
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getMaterialId() { return materialId; }
    public void setMaterialId(int materialId) { this.materialId = materialId; }

    public String getMaterialTitle() { return materialTitle; }
    public void setMaterialTitle(String materialTitle) { this.materialTitle = materialTitle; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }

    public int getUserId() { return userId; } // THÊM MỚI
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategoryName() { return categoryName; } // THÊM MỚI
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSubjectName() { return subjectName; } // THÊM MỚI
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getDeptName() { return deptName; } // THÊM MỚI
    public void setDeptName(String deptName) { this.deptName = deptName; }
}