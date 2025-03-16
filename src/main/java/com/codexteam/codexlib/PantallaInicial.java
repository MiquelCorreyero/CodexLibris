package com.codexteam.codexlib;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class PantallaInicial extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PantallaInicial.class.getResource("fxml/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 500);
        stage.setTitle("Inici de sessió");
        stage.setScene(scene);

        // Icona de la finestra
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/enter.png")));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}