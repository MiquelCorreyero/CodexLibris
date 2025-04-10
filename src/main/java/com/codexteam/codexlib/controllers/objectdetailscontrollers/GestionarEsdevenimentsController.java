package com.codexteam.codexlib.controllers.objectdetailscontrollers;

import com.codexteam.codexlib.models.Esdeveniment;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Controlador per crear, editar o eliminar esdeveniments.
 */
public class GestionarEsdevenimentsController {

    @FXML private TextField titolField;
    @FXML private TextArea contingutArea;
    @FXML private TextField adrecaField;
    @FXML private TextField dataField;
    @FXML private TextField horaIniciField;
    @FXML private Button botoGuardar;
    @FXML private Button botoEliminar;

    private Esdeveniment esdevenimentActual;

    @FXML
    public void initialize() {
        botoGuardar.setOnAction(e -> guardarEsdeveniment());
        botoEliminar.setOnAction(e -> eliminarEsdeveniment());
    }

    /**
     * Estableix l'esdeveniment a editar o prepara la vista per crear-ne un de nou.
     *
     * @param esdeveniment Esdeveniment a editar, o null si és un de nou.
     */
    public void setEsdeveniment(Esdeveniment esdeveniment) {
        this.esdevenimentActual = esdeveniment;

        if (esdeveniment != null) {
            titolField.setText(esdeveniment.getTitol());
            contingutArea.setText(esdeveniment.getContingut());
            adrecaField.setText(esdeveniment.getAdreca());
            dataField.setText(esdeveniment.getData());
            horaIniciField.setText(esdeveniment.getHoraInici());
            botoEliminar.setDisable(false);
        } else {
            botoEliminar.setDisable(true);
        }
    }

    private void guardarEsdeveniment() {
        String titol = titolField.getText().trim();
        String contingut = contingutArea.getText().trim();
        String adreca = adrecaField.getText().trim();
        String data = dataField.getText().trim();
        String horaInici = horaIniciField.getText().trim();

        if (titol.isEmpty() || contingut.isEmpty() || adreca.isEmpty() || data.isEmpty() || horaInici.isEmpty()) {
            mostrarMissatge("Error", "Tots els camps són obligatoris.");
            return;
        }

        Esdeveniment esdeveniment = new Esdeveniment();
        if (esdevenimentActual != null) {
            esdeveniment.setId(esdevenimentActual.getId());
        }
        esdeveniment.setTitol(titol);
        esdeveniment.setContingut(contingut);
        esdeveniment.setAdreca(adreca);
        esdeveniment.setData(data);
        esdeveniment.setHoraInici(horaInici);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(esdeveniment);

            HttpRequest request;
            if (esdeveniment.getId() > 0) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/events/" + esdeveniment.getId()))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            } else {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/events"))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            }

            HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                mostrarMissatge("Èxit", "Esdeveniment desat correctament.");
                                tancarFinestra();
                            } else {
                                mostrarMissatge("Error", "Error al desar. Codi: " + response.statusCode());
                            }
                        });
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarMissatge("Error", "Error en guardar l'esdeveniment.");
        }
    }

    private void eliminarEsdeveniment() {
        if (esdevenimentActual == null || esdevenimentActual.getId() <= 0) {
            mostrarMissatge("Error", "Cap esdeveniment seleccionat.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar esdeveniment");
        confirm.setHeaderText("Segur que vols eliminar aquest esdeveniment?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/events/" + esdevenimentActual.getId()))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .DELETE()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 204) {
                            mostrarMissatge("Èxit", "Esdeveniment eliminat correctament.");
                            tancarFinestra();
                        } else {
                            mostrarMissatge("Error", "No s'ha pogut eliminar. Codi: " + response.statusCode());
                        }
                    });
                });
    }

    private void tancarFinestra() {
        Stage stage = (Stage) botoGuardar.getScene().getWindow();
        stage.close();
    }

    private void mostrarMissatge(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }
}

