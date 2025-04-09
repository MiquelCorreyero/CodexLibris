package com.codexteam.codexlib.controllers.admincontrollers;

import com.codexteam.codexlib.models.Reserva;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
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
 * Controlador del panell de reserves dins de l’àrea d’administració.
 * S'encarrega de mostrar totes les reserves registrades, amb informació rellevant com usuari, llibre i dates.
 */
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

    // BOTONS
    @FXML private javafx.scene.control.Button inserirNovaReservaButton;

    /**
     * Inicialitza la taula de reserves assignant les propietats a cada columna
     * i carrega les dades des del servidor. També configura els esdeveniments de la taula i del botó.
     */
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

        taulaReserves.setRowFactory(tv -> {
            TableRow<Reserva> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !fila.isEmpty()) {
                    Reserva reservaSeleccionada = fila.getItem();
                    obrirFinestraGestionarReserva(reservaSeleccionada);
                }
            });
            return fila;
        });

        inserirNovaReservaButton.setOnAction(e -> obrirFinestraGestionarReserva(null));

    }

    /**
     * Obté el llistat de les reserves del servidor mitjançant una petició HTTP GET
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

    /**
     * Obre la finestra per gestionar una reserva (nova o existent).
     *
     * @param reserva La reserva a editar, o null si és una nova reserva.
     */
    private void obrirFinestraGestionarReserva(Reserva reserva) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarReservesView.fxml"));
            Parent root = loader.load();

            com.codexteam.codexlib.controllers.GestionarReservesController controller = loader.getController();
            controller.setReserva(reserva);

            Stage stage = new Stage();
            stage.setTitle(reserva == null ? "Nova reserva" : "Editar reserva");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recarrega les dades després de tancar la finestra
            carregarReserves();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}