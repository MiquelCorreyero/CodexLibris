package com.codexteam.codexlib;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/*
* _____    _____   ______    _____   __   __   _        _____   ______   ______    _____    _____
/  __ \  |  _  |  |  _  \  |  ___|  \ \ / /  | |      |_   _|  | ___ \  | ___ \  |_   _|  /  ___|
| /  \/  | | | |  | | | |  | |__     \ V /   | |        | |    | |_/ /  | |_/ /    | |    \ `--.
| |      | | | |  | | | |  |  __|    /   \   | |        | |    | ___ \  |    /     | |     `--. \
| \__/\  | |_| |  | |/ /   | |___   / /^\ \  | |____   _| |_   | |_/ /  | |\ \    _| |_   /\__/ /
 \____/  |_____|  |___/    \____/   \/   \/  \_____/  \_____/  \____/   |_| \_\  \_____/  \____/
 *
 * Projecte final DAM
 * CodexLibris - Aplicació d'escriptori (client)
 *
 * Frontend: Java + JavaFX + SceneBuilder
 * Backend: Spring Boot (API)
 * BBDD: PostgreSQL
 * Contenidors: Docker + Docker Compose
 * Comunicacions: API REST + JSON + JWT
 *
* */

/**
 * Classe principal que inicia l'aplicació CodexLibris.
 * Aquesta classe carrega la vista de login i configura l'escenari inicial.
 */
public class PantallaInicial extends Application {

    /**
     * Mètode que s'executa en iniciar l'aplicació.
     * Carrega l'FXML de la pantalla de login i mostra la finestra principal.
     *
     * @param stage L'escenari principal de l'aplicació.
     * @throws IOException Si hi ha un error en carregar l'FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PantallaInicial.class.getResource("fxml/loginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 500);
        stage.setTitle("Inici de sessió");
        stage.setScene(scene);

        // Icona de la finestra
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/enter.png")));
        stage.show();
    }

    /**
     * Mètode principal que inicia l'aplicació.
     *
     * @param args Arguments de la línia de comandament (no utilitzats).
     */
    public static void main(String[] args) {
        launch();
    }
}