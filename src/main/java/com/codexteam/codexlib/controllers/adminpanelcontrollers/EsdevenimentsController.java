package com.codexteam.codexlib.controllers.adminpanelcontrollers;

import com.codexteam.codexlib.controllers.objectdetailscontrollers.GestionarEsdevenimentsController;
import com.codexteam.codexlib.models.Esdeveniment;
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
 * Controlador del panell d'administració encarregat de gestionar els esdeveniments de la biblioteca.
 * Aquesta classe mostra una taula amb els esdeveniments registrats i permet afegir-ne, editar-los o visualitzar-ne els detalls.
 *
 * <p>Les dades es carreguen des del servidor mitjançant una crida HTTP GET i es mostren a la taula.
 * L'usuari pot obrir una finestra emergent per gestionar un esdeveniment mitjançant doble clic o el botó de "Nou esdeveniment".</p>
 *
 * <p>Les accions d'interfície estan vinculades amb el fitxer FXML <code>gestionarEsdevenimentsView.fxml</code>.</p>
 *
 * @author Miquel Correyero
 */
public class EsdevenimentsController {

    @FXML private TableView<Esdeveniment> taulaEsdeveniments;
    @FXML private TableColumn<Esdeveniment, Integer> colId;
    @FXML private TableColumn<Esdeveniment, String> colTitol;
    @FXML private TableColumn<Esdeveniment, String> colContingut;
    @FXML private TableColumn<Esdeveniment, String> colAdreca;
    @FXML private TableColumn<Esdeveniment, String> colData;
    @FXML private TableColumn<Esdeveniment, String> colHora;

    @FXML private Button nouEsdevenimentButton;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitol.setCellValueFactory(new PropertyValueFactory<>("titol"));
        colContingut.setCellValueFactory(new PropertyValueFactory<>("contingut"));
        colAdreca.setCellValueFactory(new PropertyValueFactory<>("adreca"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("horaInici"));

        carregarEsdeveniments();

        taulaEsdeveniments.setRowFactory(tv -> {
            TableRow<Esdeveniment> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !fila.isEmpty()) {
                    Esdeveniment seleccionat = fila.getItem();
                    obrirGestioEsdeveniment(seleccionat);
                }
            });
            return fila;
        });

        nouEsdevenimentButton.setOnAction(e -> obrirGestioEsdeveniment(null));
    }

    /**
     * Carrega el llistat d'esdeveniments des del servidor i els mostra a la taula.
     */
    private void carregarEsdeveniments() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/events"))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());

                        List<Esdeveniment> esdeveniments = mapper.readValue(
                                response,
                                new TypeReference<List<Esdeveniment>>() {}
                        );

                        Platform.runLater(() -> taulaEsdeveniments.getItems().setAll(esdeveniments));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> mostrarMissatge("Error", "No s'han pogut carregar els esdeveniments."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarMissatge("Error", "Error en la connexió amb el servidor."));
                    return null;
                });
    }

    /**
     * Obre la finestra per gestionar un esdeveniment (nou o existent).
     *
     * @param esdeveniment L'esdeveniment a editar o null si és un de nou.
     */
    private void obrirGestioEsdeveniment(Esdeveniment esdeveniment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarEsdevenimentsView.fxml"));
            Parent root = loader.load();

            GestionarEsdevenimentsController controller = loader.getController();
            controller.setEsdeveniment(esdeveniment);

            Stage stage = new Stage();
            stage.setTitle(esdeveniment == null ? "Nou esdeveniment" : "Editar esdeveniment");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/calendar_icon.png")));
            stage.showAndWait();

            carregarEsdeveniments(); // Refrescar taula
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra un missatge informatiu a l'usuari.
     *
     * @param title   Títol de la finestra d'alerta.
     * @param message Missatge que es mostrarà.
     */
    private void mostrarMissatge(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

