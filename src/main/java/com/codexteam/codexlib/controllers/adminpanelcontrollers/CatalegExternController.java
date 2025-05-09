package com.codexteam.codexlib.controllers.adminpanelcontrollers;

import com.codexteam.codexlib.controllers.objectdetailscontrollers.GestionarLlibresController;
import com.codexteam.codexlib.controllers.objectdetailscontrollers.IntroduirApiKeyAIController;
import com.codexteam.codexlib.models.LlibreExtern;
import com.codexteam.codexlib.services.ClientFactory;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.codexteam.codexlib.models.RespostaExtern;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador per gestionar la cerca de llibres externs mitjançant l'API d'Open Library.
 * Mostra els resultats obtinguts a través d’un endpoint propi que fa servir l’API externa.
 */
public class CatalegExternController {

    @FXML private TextField campCercaExtern;
    @FXML private Button botoCercaExtern;
    @FXML private Button netejaExternButton;
    @FXML private Button importarLlibreButton;
    @FXML private Button importarIAButton;

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

        // Botó importar
        importarLlibreButton.setOnAction(e -> {
            LlibreExtern seleccionat = taulaExtern.getSelectionModel().getSelectedItem();
            if (seleccionat == null) {
                mostrarMissatge("Error", "Has de seleccionar un llibre per importar.");
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarLlibresView.fxml"));
                Parent root = loader.load();

                GestionarLlibresController controller = loader.getController();
                controller.importarLlibreExtern(seleccionat);

                Stage stage = new Stage();
                stage.setTitle("Importar llibre");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

            } catch (IOException ex) {
                ex.printStackTrace();
                mostrarMissatge("Error", "No s'ha pogut obrir la finestra d'importació.");
            }
        });

        // Botó importar amb IA
        importarIAButton.setOnAction(e -> {
            System.out.println("Botó 'Importar amb IA' clicat");
            mostrarFinestraApiKey();
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

        String url = "https://localhost/external-books/search?q=" + query.replace(" ", "%20");

        HttpClient client = ClientFactory.getClient();
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

    private void mostrarFinestraApiKey() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/introduirApiKeyAIView.fxml"));
            Parent root = loader.load();

            IntroduirApiKeyAIController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Introduir API Key d'OpenAI");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            stage.setOnShown(event -> Platform.runLater(() -> root.requestFocus()));
            stage.showAndWait();

            // Recollim la clau un cop es tanca la finestra
            String apiKey = controller.getApiKey();
            if (apiKey != null && !apiKey.isBlank()) {
                System.out.println("API KEY introduïda: " + apiKey);
            } else {
                System.out.println("No s’ha introduït cap clau d’API.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarMissatge("Error", "No s'ha pogut obrir la finestra per introduir la clau API.");
        }
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

