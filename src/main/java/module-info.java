module com.codexteam.codexlib {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;


    opens com.codexteam.codexlib to javafx.fxml;
    exports com.codexteam.codexlib;
}