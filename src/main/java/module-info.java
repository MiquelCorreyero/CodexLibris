module com.codexteam.codexlib {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.net.http;
    requires org.junit.jupiter.api;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    opens com.codexteam.codexlib to javafx.fxml;
    exports com.codexteam.codexlib;
    exports com.codexteam.codexlib.controllers;
    opens com.codexteam.codexlib.controllers to javafx.fxml;
    exports com.codexteam.codexlib.models;
    opens com.codexteam.codexlib.models to javafx.fxml;
    exports com.codexteam.codexlib.services;
    opens com.codexteam.codexlib.services to javafx.fxml;
    exports com.codexteam.codexlib.controllers.adminpanelcontrollers;
    opens com.codexteam.codexlib.controllers.adminpanelcontrollers to javafx.fxml;
    exports com.codexteam.codexlib.controllers.objectdetailscontrollers;
    opens com.codexteam.codexlib.controllers.objectdetailscontrollers to javafx.fxml;
}