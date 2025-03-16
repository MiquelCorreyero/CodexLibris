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
    //                       BOTONS
    //=====================================================
    @FXML
    private Button inserirNouLlibreButton; // Cercar llibre per ISBN

    //=====================================================
    //                ELEMENTS PRINCIPALS
    //=====================================================
    @FXML
    public void initialize() {

        // Mostrar el nom de l'usuari que inicia sessió
        textBenvinguda.setText("Benvingut" + formatNomUsuari(getNomUsuariActual()));

        // Mostrar missatge al clicar sobre les notificacions
        bellButton.setOnMouseClicked(event -> mostrarMissatge("Alerta", "Ep! Sóc una notificació!"));
        bellButton.setCursor(javafx.scene.Cursor.HAND);

        // Mostrar finestra per a cercar llibre per ISBN
        inserirNouLlibreButton.setOnAction(event ->
                obrirNovaFinestra("/com/codexteam/codexlib/fxml/isbnView.fxml", "Cercar llibre per ISBN", "/com/codexteam/codexlib/images/isbn.png")
        );

        // Mostrar finestra de configuració
        configButton.setOnMouseClicked(event ->
                obrirNovaFinestra("/com/codexteam/codexlib/fxml/configView.fxml", "Configuració", "/com/codexteam/codexlib/images/config_.png")
        );
        configButton.setCursor(javafx.scene.Cursor.HAND);

    }

    // Mostrar el nom de l'usuari que inicia sessió
    private String formatNomUsuari(String nomUsuari) {
        return (nomUsuari != null && !nomUsuari.trim().isEmpty()) ? ", " + nomUsuari : ".";
    }

    //=====================================================
    //              OBRIR UNA NOVA FINESTRA
    //=====================================================
    private void obrirNovaFinestra(String fxml, String nomFinestra, String icona) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(nomFinestra);
            stage.setScene(new Scene(root));

            // Icona de la finestra
            stage.getIcons().add(new Image(getClass().getResourceAsStream(icona)));

            // BloqueJa la finestra principal fins a tancar aquesta
            stage.initModality(Modality.APPLICATION_MODAL);
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
        obrirNovaFinestra("/com/codexteam/codexlib/fxml/loginView.fxml", "Inici de sessió", "/com/codexteam/codexlib/images/enter.png");
    }

    // Tancar la finestra actual un cop s'ha fet logout
    private void tancarFinestraActual() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
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
