module com.system.ltm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires java.management;
    requires jdk.management;


    opens com.system.ltm to javafx.fxml;
    exports com.system.ltm;
}