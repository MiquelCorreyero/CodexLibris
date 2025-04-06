module com.codexteam.codexlib {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.codexteam.codexlib to javafx.fxml;
    exports com.codexteam.codexlib;
}