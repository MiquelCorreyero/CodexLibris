package com.codexteam.codexlib.controllers;

import com.codexteam.codexlib.services.ConnexioServidor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

import static com.codexteam.codexlib.services.ConnexioServidor.getNomUsuariActual;

/**
 * Controlador del panell d'administració, només visible per a usuaris de tipus admin.
 * Gestiona la navegació entre diferents seccions com llibres, usuaris, reserves i esdeveniments.
 * També s'encarrega de carregar les dades dels diferents apartats des del servidor i mostrar-les a les taules corresponents.
 */
public class AdminController {

    //=====================================================
    //            ELEMENTS DE LA INTERFÍCIE
    //=====================================================

    // PANELLS
    @FXML private AnchorPane paneInici;
    @FXML private AnchorPane paneLlibres;
    @FXML private AnchorPane paneAutors;
    @FXML private AnchorPane paneGeneres;
    @FXML private AnchorPane paneUsuaris;
    @FXML private AnchorPane paneReserves;
    @FXML private AnchorPane paneEsdeveniments;

    // LABEL USUARI ACTIU
    @FXML private Label textBenvinguda;

    // IMATGES CLICABLES
    @FXML private ImageView configButton;
    @FXML private ImageView bellButton;

    // BOTONS
    @FXML private Button logoutButton; // Logout

    //=====================================================
    //                VISIBILITAT PANELLS
    //=====================================================
    private void hideAllPanes() {
        paneInici.setVisible(false);
        paneLlibres.setVisible(false);
        paneUsuaris.setVisible(false);
        paneAutors.setVisible(false);
        paneGeneres.setVisible(false);
        paneReserves.setVisible(false);
        paneEsdeveniments.setVisible(false);
    }

    /**
     * Amaga tots els panells i mostra el panell d'inici.
     */
    @FXML
    private void showInici() {
        hideAllPanes();
        paneInici.setVisible(true);
    }

    /**
     * Mostra el panell d'usuaris i amaga la resta.
     */
    @FXML
    private void showUsuaris() {
        hideAllPanes();
        paneUsuaris.setVisible(true);

        if (paneUsuaris.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/panellUsuarisView.fxml"));
                Parent usuarisContent = loader.load();
                paneUsuaris.getChildren().setAll(usuarisContent);
                AnchorPane.setTopAnchor(usuarisContent, 0.0);
                AnchorPane.setBottomAnchor(usuarisContent, 0.0);
                AnchorPane.setLeftAnchor(usuarisContent, 0.0);
                AnchorPane.setRightAnchor(usuarisContent, 0.0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mostra el panell de llibres (catàleg) i amaga la resta.
     */
    @FXML
    private void showLlibres() {
        hideAllPanes();
        paneLlibres.setVisible(true);

        if (paneLlibres.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/panellLlibresView.fxml"));
                Parent llibresContent = loader.load();
                paneLlibres.getChildren().setAll(llibresContent);
                AnchorPane.setTopAnchor(llibresContent, 0.0);
                AnchorPane.setBottomAnchor(llibresContent, 0.0);
                AnchorPane.setLeftAnchor(llibresContent, 0.0);
                AnchorPane.setRightAnchor(llibresContent, 0.0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mostra el panell d'autors i amaga la resta.
     */
    @FXML
    private void showAutors() {
        hideAllPanes();
        paneAutors.setVisible(true);

        if (paneAutors.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/panellAutorsView.fxml"));
                Parent autorsContent = loader.load();
                paneAutors.getChildren().setAll(autorsContent);
                AnchorPane.setTopAnchor(autorsContent, 0.0);
                AnchorPane.setBottomAnchor(autorsContent, 0.0);
                AnchorPane.setLeftAnchor(autorsContent, 0.0);
                AnchorPane.setRightAnchor(autorsContent, 0.0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mostra el panell de gèneres i amaga la resta.
     */
    @FXML
    private void showGeneres() {
        hideAllPanes();
        paneGeneres.setVisible(true);
    }

    /**
     * Mostra el panell de reserves i amaga la resta.
     */
    @FXML
    private void showReserves() {
        hideAllPanes();
        paneReserves.setVisible(true);

        if (paneReserves.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/panellReservesView.fxml"));
                Parent reservesContent = loader.load();
                paneReserves.getChildren().setAll(reservesContent);
                AnchorPane.setTopAnchor(reservesContent, 0.0);
                AnchorPane.setBottomAnchor(reservesContent, 0.0);
                AnchorPane.setLeftAnchor(reservesContent, 0.0);
                AnchorPane.setRightAnchor(reservesContent, 0.0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mostra el panell d'esdeveniments i amaga la resta.
     */
    @FXML
    private void showEsdeveniments() {
        hideAllPanes();
        paneEsdeveniments.setVisible(true);
    }

    //=====================================================
    //             INICIALITZAR ELS COMPONENTS
    //=====================================================
    /**
     * Inicialitza el controlador després de carregar l'FXML.
     * Configura el comportament dels botons i carrega la llista de llibres.
     */
    @FXML
    public void initialize() {

        // Mostrar el nom de l'usuari que inicia sessió
        textBenvinguda.setText("Benvingut" + formatNomUsuari(getNomUsuariActual()));

        // Mostrar missatge al clicar sobre les notificacions
        bellButton.setOnMouseClicked(event -> mostrarMissatge("Alerta", "Ep! Sóc una notificació!"));
        bellButton.setCursor(javafx.scene.Cursor.HAND);

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
    /**
     * Obre una nova finestra modal amb el FXML, títol i icona especificats.
     *
     * @param fxml Ruta del fitxer FXML.
     * @param nomFinestra Títol de la finestra.
     * @param icona Ruta de la icona de la finestra.
     */
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
    //                       LOGOUT
    //=====================================================
    /**
     * Tanca la sessió de l'usuari actual i retorna al panell de login.
     */
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

    /**
     * Tanca la finestra actual de l'aplicació.
     */
    private void tancarFinestraActual() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Mostra una finestra de confirmació per tancar la sessió.
     *
     * @return true si l’usuari accepta tancar la sessió, false si ho cancel·la.
     */
    private boolean confirmarTancarSessio() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Tancar la sessió");
        confirmAlert.setHeaderText("Segur que vols tancar la sessió?");

        // Obtenim la resposta de l'usuari
        Optional<ButtonType> result = confirmAlert.showAndWait();

        // Retorna true si l'usuari clica "OK", false si clica "Cancel"
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    //=====================================================
    //            MOSTRAR MISSATGES INFORMATIUS
    //=====================================================
    /**
     * Mostra un missatge d’informació amb el títol i contingut especificats.
     *
     * @param title Títol de la finestra d’alerta.
     * @param message Missatge que es mostrarà a l’usuari.
     */
    private void mostrarMissatge(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}