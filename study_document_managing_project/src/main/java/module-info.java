module org.example.study_document_managing_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens org.example.study_document_managing_project to javafx.fxml;
    exports org.example.study_document_managing_project;
}