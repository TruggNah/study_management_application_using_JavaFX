package org.example.study_document_managing_project;

public class DepartmentItem {
    private final int id;
    private final String name;

    public DepartmentItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name;
    }
}
