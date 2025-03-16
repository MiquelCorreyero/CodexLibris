package com.codexteam.codexlib;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.Optional;

import static com.codexteam.codexlib.ConnexioServidor.getNomUsuariActual;

public class AdminController {

    @FXML private AnchorPane paneInici;
    @FXML private AnchorPane paneLlibres;
    @FXML private AnchorPane paneUsuaris;
    @FXML private AnchorPane paneEsdeveniments;
    @FXML private Label textBenvinguda;
    @FXML private ImageView configButton;
    @FXML private ImageView bellButton;


    //=====================================================
    //                VISIBILITAT PANELLS
    //=====================================================
    private void hideAllPanes() {
        paneInici.setVisible(false);
        paneLlibres.setVisible(false);
        paneUsuaris.setVisible(false);
        paneEsdeveniments.setVisible(false);
    }

    @FXML
    private void showInici() {
        hideAllPanes();
        paneInici.setVisible(true);
    }

    @FXML
    private void showLlibres() {
        hideAllPanes();
        paneLlibres.setVisible(true);
    }

    @FXML
    private void showUsuaris() {
        hideAllPanes();
        paneUsuaris.setVisible(true);
    }

    @FXML
    private void showEsdeveniments() {
        hideAllPanes();
        paneEsdeveniments.setVisible(true);
    }

    //=====================================================
    //                MISSATGE DE BENVINGUDA
    //=====================================================
    @FXML
    public void initialize() {
        textBenvinguda.setText("Benvingut" + formatNomUsuari(getNomUsuariActual()));

        // Assigna una acció al botó de configuració per obrir una nova finestra
        configButton.setOnMouseClicked(event -> obrirConfiguracio());
        configButton.setCursor(javafx.scene.Cursor.HAND);

        // Mostrar missatge al clicar sobre les notificacions
        bellButton.setOnMouseClicked(event -> mostrarMissatge("Alerta", "Ep! Sóc una notificació!"));
        bellButton.setCursor(javafx.scene.Cursor.HAND);
    }

    // Mostrar el nom de l'usuari que inicia sessió
    private String formatNomUsuari(String nomUsuari) {
        return (nomUsuari != null && !nomUsuari.trim().isEmpty()) ? ", " + nomUsuari : ".";
    }

    //=====================================================
    //          MOSTRAR FINESTRA DE CONFIGURACIÓ
    //=====================================================
    private void obrirConfiguracio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/ConfigView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Configuració");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // BloqueJa la finestra principal fins a tancar aquesta
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //=====================================================
    //                      LOGOUT
    //=====================================================
    @FXML
    private Button logoutButton;

    @FXML
    private void tancarSessio() {
        // Mostrar alerta per confirmar si vol tancar la sessió
        if (!confirmarTancarSessio()) {
            return;
        }
        // Esborrar el token de sessió
        ConnexioServidor.logout();
        // Mostrar un missatge de confirmació (crec que fa més nosa que servei)
        // mostrarMissatge("Sessió tancada", "La sessió s'ha tancat correctament.");
        // Tancar la finestra actual
        tancarFinestraActual();
        // Tornar a la pantalla de login
        recarregarLogin();
    }

    // Tancar la finestra actual un cop s'ha fet logout
    private void tancarFinestraActual() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
    }

    // Tornar a obrir la pantalla de login en tancar la sessió
    private void recarregarLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(PantallaInicial.class.getResource("fxml/LoginView.fxml"));
            Parent root = fxmlLoader.load();
            String title = "Inici de sessió";

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 640, 500));

            // Icona de la finestra
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/enter.png")));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mostrar missatge
    private void mostrarMissatge(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Confirmació per tancar la sessió
    private boolean confirmarTancarSessio() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Tancar la sessió");
        confirmAlert.setHeaderText("Segur que vols tancar la sessió?");
        // confirmAlert.setContentText("Si tanques la sessió, hauràs d'iniciar sessió de nou.");

        // Obtenim la resposta de l'usuari
        Optional<ButtonType> result = confirmAlert.showAndWait();

        // Retorna true si l'usuari clica "OK", false si clica "Cancel"
        return result.isPresent() && result.get() == ButtonType.OK;
    }

}
