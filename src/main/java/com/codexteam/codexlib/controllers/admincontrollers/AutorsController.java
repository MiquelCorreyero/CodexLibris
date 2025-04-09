package com.codexteam.codexlib.controllers.admincontrollers;

import com.codexteam.codexlib.controllers.GestionarAutorsController;
import com.codexteam.codexlib.models.Autor;
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

public class AutorsController {

    // COLUMNES DE LA TAULA D'AUTORS
    @FXML
    private TableView<Autor> taulaAutors;
    @FXML private TableColumn<Autor, String> colIdAutor;
    @FXML private TableColumn<Autor, String> colNomAutor;
    @FXML private TableColumn<Autor, String> colNacionalitatAutor;
    @FXML private TableColumn<Autor, String> colDataAutor;

    // BOTONS
    @FXML private Button inserirNouAutorButton; // Inserir o editar autor

    @FXML
    public void initialize() {

        // CARREGAR LLISTAT D'AUTORS
        colIdAutor.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomAutor.setCellValueFactory(new PropertyValueFactory<>("name"));
        colNacionalitatAutor.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        colDataAutor.setCellValueFactory(new PropertyValueFactory<>("birth_date"));

        carregarAutors();

        // EDITA L'AUTOR EN FER DOBLE CLIC SOBRE UNA FILA
        taulaAutors.setRowFactory(tv -> {
            TableRow<Autor> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !fila.isEmpty()) {
                    Autor autorSeleccionat = fila.getItem();
                    obrirGestionarAutors(autorSeleccionat);
                }
            });
            return fila;
        });

        // OBRE LA FINESTRA PER CREAR UN NOU AUTOR
        inserirNouAutorButton.setOnAction(e -> obrirGestionarAutors(null));

    }

    //=====================================================
    //            OBTENIR LLISTAT D'AUTORS
    //=====================================================
    private void carregarAutors() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/authors"))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        // Si hay fechas tipo LocalDate, registrar módulo:
                        mapper.registerModule(new JavaTimeModule());

                        List<Autor> autors = mapper.readValue(
                                response,
                                new TypeReference<List<Autor>>() {}
                        );

                        // Añadir los autores a la tabla desde el hilo principal
                        Platform.runLater(() -> {
                            taulaAutors.getItems().setAll(autors);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> mostrarMissatge("Error", "No s'han pogut carregar els autors."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarMissatge("Error", "Error en la connexió amb el servidor."));
                    return null;
                });
    }

    //=====================================================
    //           OBRIR FINESTRA DETALLS AUTORS
    //=====================================================
    private void obrirGestionarAutors(Autor autor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarAutorsView.fxml"));
            Parent root = loader.load();

            GestionarAutorsController controller = loader.getController();
            controller.setAutor(autor); // null si és nou

            Stage stage = new Stage();
            stage.setTitle(autor == null ? "Nou autor" : "Editar autor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/author.png")));
            stage.showAndWait();

            carregarAutors(); // Refresca la taula
        } catch (IOException e) {
            e.printStackTrace();
        }
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
