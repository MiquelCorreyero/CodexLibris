package com.codexteam.codexlib.controllers.objectdetailscontrollers;

import com.codexteam.codexlib.models.Autor;
import com.codexteam.codexlib.models.Genere;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Controlador encarregat de gestionar la finestra per crear un nou llibre.
 * Permet introduir les dades bàsiques (títol, ISBN, data, autor i gènere),
 * i enviar-les al servidor mitjançant una petició POST.
 *
 * <p>Els ComboBox d'autors i gèneres es carreguen automàticament en inicialitzar la vista.</p>
 */
public class GestionarNouLlibreController {

    @FXML private TextField titolField;
    @FXML private TextField isbnField;
    @FXML private TextField dataPublicacioField;
    @FXML private ComboBox<Autor> comboAutors;
    @FXML private ComboBox<Genere> comboGeneres;
    @FXML private CheckBox availableCheckBox;

    @FXML
    public void initialize() {
        carregarAutors();
        carregarGeneres();

        comboAutors.setConverter(new StringConverter<>() {
            @Override public String toString(Autor autor) {
                return autor != null ? autor.getName() : "";
            }

            @Override public Autor fromString(String string) {
                return null;
            }
        });

        comboGeneres.setConverter(new StringConverter<>() {
            @Override public String toString(Genere genere) {
                return genere != null ? genere.getName() : "";
            }

            @Override public Genere fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Recull les dades del formulari i les envia al servidor per crear un nou llibre.
     * Si falta algun camp obligatori, mostra una alerta d’error.
     */
    @FXML
    public void enviarLlibre() {
        String titol = titolField.getText().trim();
        String isbn = isbnField.getText().trim();
        String data = dataPublicacioField.getText().trim();
        boolean disponible = availableCheckBox.isSelected();
        Autor autorSeleccionat = comboAutors.getValue();
        Genere genereSeleccionat = comboGeneres.getValue();

        if (titol.isEmpty() || isbn.isEmpty() || data.isEmpty() || autorSeleccionat == null || genereSeleccionat == null) {
            mostrarMissatge("Error", "Tots els camps són obligatoris.");
            return;
        }

        String publishedDate = data + "T00:00:00.000Z";

        String json = String.format("""
        {
          "title": "%s",
          "isbn": "%s",
          "publishedDate": "%s",
          "available": %s,
          "authorId": %d,
          "genreId": %d
        }
        """, titol, isbn, publishedDate, disponible, autorSeleccionat.getId(), genereSeleccionat.getId());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/books"))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        mostrarMissatge("Èxit", "Llibre desat correctament.");
                    } else {
                        mostrarMissatge("Error", "Error del servidor. Codi: " + response.statusCode() + "\n" + response.body());
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> mostrarMissatge("Error", "Error inesperat: " + e.getMessage()));
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Fa una petició GET a l’API per obtenir els autors disponibles
     * i els afegeix al ComboBox corresponent.
     */
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
                        List<Autor> autors = mapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<>() {});
                        Platform.runLater(() -> comboAutors.getItems().setAll(autors));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Fa una petició GET a l’API per obtenir els gèneres disponibles
     * i els afegeix al ComboBox corresponent.
     */
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
                        List<Genere> generes = mapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<>() {});
                        Platform.runLater(() -> comboGeneres.getItems().setAll(generes));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Mostra una finestra emergent amb un missatge informatiu o d’error.
     *
     * @param titol Títol de l’alerta.
     * @param missatge Missatge a mostrar a l’usuari.
     */
    private void mostrarMissatge(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }
}