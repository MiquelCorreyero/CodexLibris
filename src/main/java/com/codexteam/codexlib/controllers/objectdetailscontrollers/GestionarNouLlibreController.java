package com.codexteam.codexlib.controllers.objectdetailscontrollers;

import com.codexteam.codexlib.models.Autor;
import com.codexteam.codexlib.models.Genere;
import com.codexteam.codexlib.services.ClientFactory;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Controlador encarregat de gestionar la finestra per crear un nou llibre des del catàleg intern.
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
    @FXML private Button botoNouAutorLlibre;
    @FXML private Button botoNouGenereLLibre;

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

        botoNouAutorLlibre.setOnAction(e -> obrirFinestraNouAutor());
        botoNouGenereLLibre.setOnAction(e -> obrirFinestraNouGenere());

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

        HttpClient client = ClientFactory.getClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost/books"))
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
        HttpClient client = ClientFactory.getClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost/authors"))
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
        HttpClient client = ClientFactory.getClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost/genres"))
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
     * Obre una finestra modal per crear un nou autor.
     * Mostra un missatge d'error si hi ha problemes en carregar la vista.
     */
    private void obrirFinestraNouAutor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarAutorsView.fxml"));
            Parent root = loader.load();

            GestionarAutorsController controller = loader.getController();
            controller.setAutor(null); // nuevo autor

            Stage stage = new Stage();
            stage.setTitle("Nou autor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setOnShown(event -> Platform.runLater(() -> root.requestFocus()));
            stage.showAndWait();

            // Refrescar lista de autores
            carregarAutors();

            // Seleccionar el autor recién creado (por nombre, idealmente por ID si devuelto)
            Autor nou = controller.getAutorCreat();
            if (nou != null) {
                comboAutors.getItems().add(nou); // opcional
                comboAutors.setValue(nou);
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarMissatge("Error", "No s'ha pogut obrir la finestra de creació d'autor.");
        }
    }

    /**
     * Obre una finestra modal per crear un nou gènere.
     * Mostra un missatge d'error si hi ha problemes en carregar la vista.
     */
    private void obrirFinestraNouGenere() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarGeneresView.fxml"));
            Parent root = loader.load();

            GestionarGeneresController controller = loader.getController();
            controller.setGenere(null); // Creació de nou gènere

            Stage stage = new Stage();
            stage.setTitle("Nou gènere");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setOnShown(event -> Platform.runLater(() -> root.requestFocus()));
            stage.showAndWait();

            // Refresca la llista de gèneres
            carregarGeneres();

            Genere nou = controller.getGenereCreat();
            if (nou != null) {
                comboGeneres.getItems().add(nou);
                comboGeneres.setValue(nou);
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarMissatge("Error", "No s'ha pogut obrir la finestra de creació de gènere.");
        }
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