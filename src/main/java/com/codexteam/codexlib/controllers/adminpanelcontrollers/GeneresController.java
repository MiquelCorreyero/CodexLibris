package com.codexteam.codexlib.controllers.adminpanelcontrollers;

import com.codexteam.codexlib.controllers.objectdetailscontrollers.GestionarGeneresController;
import com.codexteam.codexlib.models.Genere;
import com.codexteam.codexlib.services.ClientFactory;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Controlador del panell d’administració per gestionar els gèneres literaris disponibles a la biblioteca.
 * Aquesta classe permet visualitzar, afegir, editar i actualitzar els gèneres mitjançant comunicació amb el servidor.
 *
 * <p>Els gèneres es mostren en una taula que es carrega automàticament des del backend mitjançant una petició HTTP GET.
 * L’usuari pot fer doble clic sobre un gènere per editar-ne les dades o utilitzar el botó Nou gènere per crear-ne un de nou.</p>
 *
 * <p>Les accions de gestió s’obren en una finestra modal utilitzant la vista <code>gestionarGeneresView.fxml</code></p>
 *
 * @author Miquel Correyero
 */
public class GeneresController {

    @FXML private TableView<Genere> taulaGeneres;
    @FXML private TableColumn<Genere, String> colIdGenere;
    @FXML private TableColumn<Genere, String> colNomGenere;
    @FXML private TableColumn<Genere, String> colDescripcioGenere;

    @FXML private Button inserirNouGenereButton;

    /**
     * Inicialitza el panell de gèneres:
     * - Assigna els valors a les columnes de la taula.
     * - Carrega la llista de gèneres des del servidor.
     * - Defineix el comportament en fer doble clic sobre una fila.
     * - Configura el botó per afegir un nou gènere.
     */
    @FXML
    public void initialize() {
        colIdGenere.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomGenere.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescripcioGenere.setCellValueFactory(new PropertyValueFactory<>("description"));

        carregarGeneres();

        taulaGeneres.setRowFactory(tv -> {
            TableRow<Genere> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !fila.isEmpty()) {
                    Genere genereSeleccionat = fila.getItem();
                    obrirGestionarGenere(genereSeleccionat);
                }
            });
            return fila;
        });

        inserirNouGenereButton.setOnAction(e -> obrirGestionarGenere(null));
    }

    /**
     * Envia una petició HTTP GET al servidor per obtenir tots els gèneres.
     * Si la resposta és correcta, actualitza la taula amb els resultats.
     */
    private void carregarGeneres() {
        HttpClient client = ClientFactory.getClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost/genres"))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());

                        List<Genere> generes = mapper.readValue(response, new TypeReference<List<Genere>>() {});
                        Platform.runLater(() -> taulaGeneres.getItems().setAll(generes));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> mostrarMissatge("Error", "No s'han pogut carregar els gèneres."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarMissatge("Error", "Error en la connexió amb el servidor."));
                    return null;
                });
    }

    /**
     * Obre una finestra modal per gestionar un gènere.
     *
     * @param genere El gènere a editar, o {@code null} si es vol crear un de nou.
     */
    private void obrirGestionarGenere(Genere genere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarGeneresView.fxml"));
            Parent root = loader.load();

            GestionarGeneresController controller = loader.getController();
            controller.setGenere(genere); // null si és nou

            Stage stage = new Stage();
            stage.setTitle(genere == null ? "Nou gènere" : "Editar gènere");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/library_icon_.png")));

            // Desactivar el focus per defecte als inputs
            stage.setOnShown(e -> Platform.runLater(() -> root.requestFocus()));

            stage.showAndWait();

            carregarGeneres();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra un missatge informatiu a l'usuari.
     *
     * @param title   Títol de la finestra.
     * @param message Missatge de contingut.
     */
    private void mostrarMissatge(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}