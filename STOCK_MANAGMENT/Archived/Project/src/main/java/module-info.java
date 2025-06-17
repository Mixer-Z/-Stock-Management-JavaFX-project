module com.example.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.controlsfx.controls;


    exports controller to javafx.graphics;
    opens controller to javafx.fxml;
    opens model.entities to javafx.base;
}