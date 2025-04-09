package com.codexteam.codexlib.controllers.admincontrollers;

import com.codexteam.codexlib.controllers.GestionarUsuarisController;
import com.codexteam.codexlib.models.Usuari;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Controlador del panell de gestió d'usuaris dins de l'àrea d'administració.
 * Permet visualitzar, crear, editar i gestionar la llista d'usuaris obtinguda del servidor.
 */
public class UsuarisController {

    // COLUMNES DE LA TAULA D'USUARIS
    @FXML private TableView<Usuari> taulaUsuaris;
    @FXML private TableColumn<Usuari, String> colIdUsuari;
    @FXML private TableColumn<Usuari, String> colUsernameUsuari;
    @FXML private TableColumn<Usuari, String> colNomUsuari;
    @FXML private TableColumn<Usuari, String> colEmailUsuari;
    @FXML private TableColumn<Usuari, String> colPasswordUsuari;
    @FXML private TableColumn<Usuari, String> colRolUsuari;

    // BOTONS
    @FXML private Button nouUsuariButton;

    /**
     * Inicialitza el controlador després de carregar l'FXML.
     * Configura les columnes de la taula, carrega els usuaris.
     */
    @FXML
    public void initialize() {

        // CARREGAR LLISTAT D'USUARIS
        colIdUsuari.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsernameUsuari.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNomUsuari.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        colEmailUsuari.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPasswordUsuari.setCellValueFactory(new PropertyValueFactory<>("password"));
        colRolUsuari.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole().getName()));

        carregarUsuaris();

        // EDITA L'USUARI EN FER DOBLE CLIC SOBRE UNA FILA
        taulaUsuaris.setRowFactory(tv -> {
            TableRow<Usuari> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !fila.isEmpty()) {
                    Usuari usuariSeleccionat = fila.getItem();
                    obrirFinestraEditarUsuari(usuariSeleccionat);
                }
            });
            return fila;
        });

        // OBRE LA FINESTRA PER CREAR UN NOU USUARI
        nouUsuariButton.setOnAction(e -> obrirFinestraEditarUsuari(null));

    }

    /**
     * Obté el llistat d’usuaris del servidor mitjançant una petició HTTP GET
     * i actualitza la taula amb els resultats.
     */
    private void carregarUsuaris() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());
                        List<Usuari> usuaris = mapper.readValue(response, new TypeReference<List<Usuari>>() {});
                        Platform.runLater(() -> taulaUsuaris.getItems().setAll(usuaris));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> mostrarMissatge("Error", "No s'han pogut carregar els usuaris."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarMissatge("Error", "Error en la connexió amb el servidor."));
                    return null;
                });
    }

    /**
     * Obre la finestra de detall per editar o crear un nou usuari.
     *
     * @param usuari Usuari a editar, o null si es vol crear-ne un de nou.
     */
    private void obrirFinestraEditarUsuari(Usuari usuari) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/detall-items/detallsUsuariView.fxml"));
            Parent root = loader.load();

            GestionarUsuarisController controller = loader.getController();
            controller.seleccionarUsuari(usuari); // null si és nou

            Stage stage = new Stage();
            stage.setTitle(usuari == null ? "Nou usuari" : "Editar usuari");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Desactivar el focus per defecte als inputs
            stage.setOnShown(e -> Platform.runLater(() -> root.requestFocus()));

            stage.showAndWait();

            carregarUsuaris(); // Refrescar
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra una alerta informativa amb el títol i missatge especificats.
     *
     * @param title   Títol de l'alerta.
     * @param message Missatge a mostrar a l'usuari.
     */
    private void mostrarMissatge(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}