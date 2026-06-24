package org.example.study_document_managing_project;

public class UserModelAdmin {
    private int userId;
    private String username;
    private String email;
    private String role;
    private int contributedCount;
    private String status;

    public UserModelAdmin(int userId, String username, String email, String role, int contributedCount, String status) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.contributedCount = contributedCount;
        this.status = status;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public int getContributedCount() { return contributedCount; }
    public String getStatus() { return status; }
}