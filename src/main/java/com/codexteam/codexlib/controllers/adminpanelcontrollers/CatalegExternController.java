package com.codexteam.codexlib.controllers.adminpanelcontrollers;

import com.codexteam.codexlib.models.LlibreExtern;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.codexteam.codexlib.models.RespostaExtern;

/**
 * Controlador per gestionar la cerca de llibres externs mitjançant Open Library.
 * Mostra els resultats obtinguts a través d’un endpoint propi que consumeix l’API externa.
 */
public class CatalegExternController {

    @FXML private TextField campCercaExtern;
    @FXML private Button botoCercaExtern;
    @FXML private Button netejaExternButton;
    @FXML private Button importarLlibreButton;

    @FXML private TableView<LlibreExtern> taulaExtern;
    @FXML private TableColumn<LlibreExtern, String> colTitolExtern;
    @FXML private TableColumn<LlibreExtern, String> colAutorExtern;
    @FXML private TableColumn<LlibreExtern, String> colIsbnExtern;
    @FXML private TableColumn<LlibreExtern, String> colAnyExtern;

    @FXML
    public void initialize() {

        // Configura columnes
        colTitolExtern.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        colAutorExtern.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAuthor()));
        colIsbnExtern.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIsbn()));
        colAnyExtern.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getYear()))
        );
        // Cerca al prémer enter
        campCercaExtern.setOnAction(e -> ferCercaExtern());

        // Botó de cerca
        botoCercaExtern.setOnAction(e -> ferCercaExtern());

        // Botó de neteja
        netejaExternButton.setOnAction(e -> {
            campCercaExtern.clear();
            taulaExtern.getItems().clear();
        });

        // Desactiva neteja si no hi ha text
        campCercaExtern.textProperty().addListener((obs, oldVal, newVal) -> {
            netejaExternButton.setDisable(newVal.trim().isEmpty());
        });

        // Botó importar (pendiente de implementar)
        importarLlibreButton.setOnAction(e -> {
            LlibreExtern seleccionat = taulaExtern.getSelectionModel().getSelectedItem();
            if (seleccionat != null) {
                mostrarMissatge("Importació", "Funcionalitat d'importació encara no implementada.\nLlibre: " + seleccionat.getTitle());
            } else {
                mostrarMissatge("Error", "Has de seleccionar un llibre per importar.");
            }
        });
    }

    /**
     * Realitza una cerca de llibres externs a través de l'API pròpia que consulta Open Library.
     * Mostra els resultats en la taula si la resposta és correcta o un missatge d’error si falla.
     */
    private void ferCercaExtern() {
        String query = campCercaExtern.getText().trim();
        if (query.isEmpty()) {
            mostrarMissatge("Cerca buida", "Escriu una paraula clau per fer la cerca.");
            return;
        }

        String url = "http://localhost:8080/external-books/search?q=" + query.replace(" ", "%20");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    try {
                        int status = response.statusCode();

                        if (status < 200 || status >= 300) {
                            System.err.println("Error " + status + ": El servidor ha retornat un error durant la cerca.");
                            System.err.println("Cos de la resposta:\n" + response.body());
                            return;
                        }

                        ObjectMapper mapper = new ObjectMapper();
                        RespostaExtern resultat = mapper.readValue(response.body(), RespostaExtern.class);

                        Platform.runLater(() -> {
                            taulaExtern.getItems().clear();
                            taulaExtern.getItems().addAll(resultat.getResults());
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> mostrarMissatge("Error", "No s'han pogut interpretar les dades."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarMissatge("Error", "Error en la connexió amb el servidor."));
                    return null;
                });
    }


    /**
     * Mostra una alerta d’informació a l’usuari.
     */
    private void mostrarMissatge(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }

}

