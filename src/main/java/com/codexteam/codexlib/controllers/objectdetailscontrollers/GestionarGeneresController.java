package com.codexteam.codexlib.controllers.objectdetailscontrollers;

import com.codexteam.codexlib.models.Genere;
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
 * Controlador per a la finestra modal dels gèneres literaris.
 *
 * <p>Aquesta classe permet crear, editar i eliminar gèneres mitjançant una finestra modal.
 * Efectua les operacions CRUD mitjançant peticions HTTP al backend (POST per crear, PUT per
 * actualitzar i DELETE per eliminar).</p>
 *
 * <p>Està associada al fitxer FXML <strong>gestionarGeneresView.fxml</strong></p>
 *
 * @author Miquel Correyero
 */
public class GestionarGeneresController {

    @FXML private TextField nomField;
    @FXML private TextArea descripcioArea;
    @FXML private Button guardarGenereButton;
    @FXML private Button eliminarGenereButton;

    private Genere genereActual;

    /**
     * Mètode que s’executa després de carregar l’FXML.
     * Assigna accions als botons de guardar i eliminar.
     */
    @FXML
    public void initialize() {
        guardarGenereButton.setOnAction(event -> guardarGenere());
        eliminarGenereButton.setOnAction(event -> eliminarGenere());
    }

    /**
     * Estableix el gènere a editar o prepara per crear-ne un de nou.
     *
     * @param genere el gènere a editar, o null si es vol crear un de nou.
     */
    public void setGenere(Genere genere) {
        this.genereActual = genere;
        if (genere != null) {
            nomField.setText(genere.getName());
            descripcioArea.setText(genere.getDescription());
        }
    }

    /**
     * Envia les dades del formulari a l’API per guardar el gènere.
     * Si és un gènere existent, fa una petició PUT. Si és nou, fa una POST.
     */
    private void guardarGenere() {
        String nom = nomField.getText().trim();
        String descripcio = descripcioArea.getText().trim();

        if (nom.isEmpty() || descripcio.isEmpty()) {
            mostrarMissatge("Error", "Cal omplir tots els camps.");
            return;
        }

        Genere genere = new Genere();
        if (genereActual != null) genere.setId(genereActual.getId());
        genere.setName(nom);
        genere.setDescription(descripcio);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(genere);

            HttpRequest request;
            if (genere.getId() > 0) {
                // EDITAR GÈNERE EXISTENT
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/genres/" + genere.getId()))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            } else {
                // CREAR NOU GÈNERE
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/genres"))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            }

            HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            mostrarMissatge("Èxit", "El gènere s’ha guardat correctament.");
                            tancarFinestra();
                        });
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        Platform.runLater(() ->
                                mostrarMissatge("Error", "No s’ha pogut guardar el gènere."));
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarMissatge("Error", "Error en convertir el gènere a JSON.");
        }
    }

    /**
     * Elimina el gènere actual del servidor, després de confirmar-ho amb l'usuari.
     * Envia una petició DELETE a l'API.
     */
    private void eliminarGenere() {
        if (genereActual == null || genereActual.getId() <= 0) {
            mostrarMissatge("Error", "No s'ha seleccionat cap gènere vàlid per eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar gènere");
        confirm.setHeaderText("Segur que vols eliminar aquest gènere?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/genres/" + genereActual.getId()))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .DELETE()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 204) {
                            mostrarMissatge("Èxit", "Gènere eliminat correctament.");
                            tancarFinestra();
                        } else {
                            mostrarMissatge("Error", "No s'ha pogut eliminar el gènere. Codi: " + response.statusCode());
                        }
                    });
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarMissatge("Error", "Error inesperat en eliminar el gènere."));
                    return null;
                });
    }

    /**
     * Mostra un diàleg informatiu amb el títol i missatge especificats.
     *
     * @param titol Títol de l'alerta.
     * @param missatge Missatge de contingut.
     */
    private void mostrarMissatge(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }

    /**
     * Tanca la finestra actual.
     */
    private void tancarFinestra() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}