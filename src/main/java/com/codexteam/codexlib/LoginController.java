package com.codexteam.codexlib;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;

/**
 * Controlador de la pantalla de login.
 * S'encarrega de gestionar l'inici de sessió de l'usuari, validar credencials
 * i obrir la interfície corresponent segons el seu rol.
 * També permet accedir a la pantalla de registre per crear un nou compte.
 */
public class LoginController {

    @FXML private AnchorPane paneLogin;
    @FXML private Button nouCompteButton;
    @FXML private TextField campNomUsuari;
    @FXML private PasswordField campContrasenya;
    @FXML private Button loginButton;

    /**
     * Inicialitza el controlador. Desactiva el focus inicial en els camps i
     * configura el botó de "Nou compte".
     */
    @FXML
    public void initialize() {
        // Evitar que hi hagi focus a algun camp de text
        Platform.runLater(() -> paneLogin.requestFocus());

        // Obrir finestra de registre
        nouCompteButton.setOnAction(event -> crearNouCompte());
    }

    /**
     * Gestiona l'inici de sessió de l'usuari. Comprova les credencials
     * i obre el panell d'administrador o d'usuari segons el seu rol.
     */
    @FXML
    private void iniciarSessio() {
        String username = campNomUsuari.getText();
        String password = campContrasenya.getText();

        // PASSOS PER A IMPLEMENTAR EL LOGIN:
        // 1. Enviar usuari i contrasenya al servidor mitjançant una sol·licitud HTTP.
        // 2. Rebre el codi de sessió o token del servidor.
        // 3. Desar el codi en memòria per a futures sol·licituds.
        // 4. Incloure el codi a totes les peticions futures al servidor.

        if (ConnexioServidor.login(username, password)) {

            if (ConnexioServidor.getTipusUsuari() == 1) {
                // Obrir el panell d'administració
                obrirNovaFinestra("/com/codexteam/codexlib/fxml/adminView.fxml", "CodexLibris - Administració");
            } else {
                // Obrir el panell d'usuari no administrador
                obrirNovaFinestra("/com/codexteam/codexlib/fxml/userView.fxml", "CodexLibris");
            }

            // Tancar la finestra del login si s'ha iniciat sessió correctament
            tancarFinestraActual();
        } else {
            mostrarAlerta("Error", "Usuari o contrasenya incorrectes");
        }
    }

    /**
     * Obre una nova finestra segons el tipus d'usuari
     *
     * @param fxmlPath Ruta del fitxer FXML a carregar.
     * @param title Títol de la finestra.
     */
    private void obrirNovaFinestra(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1200, 825));
            // Icona de la finestra
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/book_w.png")));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Tancar la finestra del login
    private void tancarFinestraActual() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Mostra un missatge d'error si el login no és correcte
     *
     * @param title Títol de l'alerta.
     * @param message Contingut del missatge.
     */
    private void mostrarAlerta(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Obre la finestra de registre per crear un nou compte d'usuari.
     */
    @FXML
    private void crearNouCompte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/registerView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Crear nou compte");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}