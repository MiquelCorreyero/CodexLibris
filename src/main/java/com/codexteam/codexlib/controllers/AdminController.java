package com.codexteam.codexlib.controllers;

import com.codexteam.codexlib.services.ClientFactory;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import static com.codexteam.codexlib.services.ConnexioServidor.getNomUsuariActual;

/**
 * Controlador del panell d'administració, només visible per a usuaris de tipus admin.
 * Gestiona la navegació entre diferents seccions com llibres, usuaris, reserves i esdeveniments.
 *
 * @author Miquel Correyero
 */
public class AdminController {

    // PANELLS
    @FXML private AnchorPane paneInici;
    @FXML private AnchorPane paneLlibres;
    @FXML private AnchorPane paneCatalegExtern;
    @FXML private AnchorPane paneAutors;
    @FXML private AnchorPane paneGeneres;
    @FXML private AnchorPane paneUsuaris;
    @FXML private AnchorPane paneReserves;
    @FXML private AnchorPane paneEsdeveniments;

    // LABEL USUARI ACTIU
    @FXML private Label textBenvinguda;

    // LABEL ESTADÍSTIQUES
    @FXML private Label labelTotalLlibres;
    @FXML private Label labelTotalUsuaris;
    @FXML private Label labelTotalAutors;
    @FXML private Label labelTotalReserves;

    // IMATGES CLICABLES
    @FXML private ImageView configButton;
    @FXML private ImageView bellButton;

    // BOTONS
    @FXML private Button logoutButton; // Logout

    // VISIBILITAT DELS PANELLS
    private void hideAllPanes() {
        paneInici.setVisible(false);
        paneLlibres.setVisible(false);
        paneCatalegExtern.setVisible(false);
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
     * Mostra cada secció dins de l'aplicació carregant el seu contingut des d'un fitxer FXML.
     *
     * <p>Abans de mostrar el panell especificat, aquest mètode amaga tots els altres panells
     * mitjançant {@code hideAllPanes()} i estableix la visibilitat del panell desitjat a {@code true}.</p>
     *
     * @param panell El {@link AnchorPane} que es vol mostrar i al qual s'ha d'afegir el contingut.
     * @param rutaFXML La ruta relativa al fitxer FXML que conté el disseny del contingut a carregar.
     */
    private void mostrarPanell(AnchorPane panell, String rutaFXML) {
        hideAllPanes();
        panell.setVisible(true);

        if (panell.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
                Parent contingut = loader.load();
                panell.getChildren().setAll(contingut);
                AnchorPane.setTopAnchor(contingut, 0.0);
                AnchorPane.setBottomAnchor(contingut, 0.0);
                AnchorPane.setLeftAnchor(contingut, 0.0);
                AnchorPane.setRightAnchor(contingut, 0.0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mostra el panell d'usuaris i amaga la resta.
     */
    @FXML
    private void showUsuaris() {
        mostrarPanell(paneUsuaris, "/com/codexteam/codexlib/fxml/panells/panellUsuarisView.fxml");
    }

    /**
     * Mostra el panell de llibres (catàleg) i amaga la resta.
     */
    @FXML
    private void showLlibres() {
        mostrarPanell(paneLlibres, "/com/codexteam/codexlib/fxml/panells/panellLlibresView.fxml");
    }

    /**
     * Mostra el panell de llibres (catàleg) i amaga la resta.
     */
    @FXML
    private void showCatalegExtern() {
        mostrarPanell(paneCatalegExtern, "/com/codexteam/codexlib/fxml/panells/panellCatalegExternView.fxml");
    }

    /**
     * Mostra el panell d'autors i amaga la resta.
     */
    @FXML
    private void showAutors() {
        mostrarPanell(paneAutors, "/com/codexteam/codexlib/fxml/panells/panellAutorsView.fxml");
    }

    /**
     * Mostra el panell de gèneres i amaga la resta.
     */
    @FXML
    private void showGeneres() {
        mostrarPanell(paneGeneres, "/com/codexteam/codexlib/fxml/panells/panellGeneresView.fxml");
    }

    /**
     * Mostra el panell de reserves i amaga la resta.
     */
    @FXML
    private void showReserves() {
        mostrarPanell(paneReserves, "/com/codexteam/codexlib/fxml/panells/panellReservesView.fxml");
    }

    /**
     * Mostra el panell d'esdeveniments i amaga la resta.
     */
    @FXML
    private void showEsdeveniments() {
        mostrarPanell(paneEsdeveniments, "/com/codexteam/codexlib/fxml/panells/panellEsdevenimentsView.fxml");
    }

    /**
     * Inicialitza el controlador després de carregar l'FXML.
     * Configura el comportament dels botons de la interfície general.
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

        carregarEstadistiques();

    }

    /**
     * Mostrar el nom de l'usuari que inicia sessió
     */
    private String formatNomUsuari(String nomUsuari) {
        return (nomUsuari != null && !nomUsuari.trim().isEmpty()) ? ", " + nomUsuari : ".";
    }

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

    /** Llença peticions als principals endpoints i mostra el recompte a les etiquetes del dashboard. */
    private void carregarEstadistiques() {
        obtenirEstadistica("https://localhost/books", labelTotalLlibres);
        obtenirEstadistica("https://localhost/users", labelTotalUsuaris);
        obtenirEstadistica("https://localhost/authors", labelTotalAutors);
        obtenirEstadistica("https://localhost/loans", labelTotalReserves);
    }

    /** Fa una crida GET asíncrona a l’endpoint i posa al label la mida de la llista retornada. */
    private void obtenirEstadistica(String endpoint, Label label) {
        try {
            HttpClient client = ClientFactory.getClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            List<?> dades = mapper.readValue(response, List.class);
                            Platform.runLater(() -> label.setText(String.valueOf(dades.size())));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> label.setText("Error"));
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            label.setText("Error");
        }
    }


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