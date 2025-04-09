package com.codexteam.codexlib.controllers.admincontrollers;

import com.codexteam.codexlib.controllers.GestionarGeneresController;
import com.codexteam.codexlib.models.Genere;
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

public class GeneresController {

    @FXML private TableView<Genere> taulaGeneres;
    @FXML private TableColumn<Genere, String> colIdGenere;
    @FXML private TableColumn<Genere, String> colNomGenere;
    @FXML private TableColumn<Genere, String> colDescripcioGenere;

    @FXML private Button inserirNouGenereButton;

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

    private void carregarGeneres() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/genres"))
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
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/generes_icon.png")));
            stage.showAndWait();

            carregarGeneres();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarMissatge(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

