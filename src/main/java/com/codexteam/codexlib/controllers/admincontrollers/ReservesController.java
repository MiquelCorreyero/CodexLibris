package com.codexteam.codexlib.controllers.admincontrollers;

import com.codexteam.codexlib.models.Reserva;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ReservesController {

    // COLUMNES DE LA TAULA DE RESERVES
    @FXML
    private TableView<Reserva> taulaReserves;
    @FXML private TableColumn<Reserva, String> colIdReserva;
    @FXML private TableColumn<Reserva, String> colNomReserva;
    @FXML private TableColumn<Reserva, String> colCognomsReserva;
    @FXML private TableColumn<Reserva, String> colEmailReserva;
    @FXML private TableColumn<Reserva, String> colLlibreReserva;
    @FXML private TableColumn<Reserva, String> colDataReserva;
    @FXML private TableColumn<Reserva, String> colDataRetorn;

    @FXML
    public void initialize() {

        // CARREGAR LLISTAT DE RESERVES
        colIdReserva.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomReserva.setCellValueFactory(new PropertyValueFactory<>("user_first_name"));
        colCognomsReserva.setCellValueFactory(new PropertyValueFactory<>("user_name"));
        colEmailReserva.setCellValueFactory(new PropertyValueFactory<>("user_email"));
        colLlibreReserva.setCellValueFactory(new PropertyValueFactory<>("book_title"));
        colDataReserva.setCellValueFactory(new PropertyValueFactory<>("loan_date"));
        colDataRetorn.setCellValueFactory(new PropertyValueFactory<>("return_date"));

        carregarReserves();

    }

    //=====================================================
    //            OBTENIR LLISTAT DE RESERVES
    //=====================================================
    /**
     * Obté el llistat de les reserves del servidor mitjançant una petició HTTP
     * i les mostra a la taula de reserves.
     */
    private void carregarReserves() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/loans"))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());
                        mapper.findAndRegisterModules(); // Perquè reconegui dates de tipus LocalDate
                        List<Reserva> reserves = mapper.readValue(
                                response,
                                new TypeReference<List<Reserva>>() {}
                        );
                        Platform.runLater(() -> taulaReserves.getItems().setAll(reserves));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

}
