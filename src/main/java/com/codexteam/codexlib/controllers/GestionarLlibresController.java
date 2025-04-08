package com.codexteam.codexlib.controllers;

import com.codexteam.codexlib.models.Autor;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.codexteam.codexlib.models.Genere;
import com.codexteam.codexlib.models.Llibre;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GestionarLlibresController {

    @FXML private Label titolLabel;
    @FXML private TextField titolField;
    @FXML private ComboBox<Autor> comboAutors;
    @FXML private ComboBox<Genere> comboGeneres;
    @FXML private TextField isbnField;
    @FXML private TextField dataPublicacioField;
    @FXML private ComboBox<String> disponibilitatComboBox;
    @FXML private Button guardarLlibreButton;
    @FXML private Button eliminarLlibreButton;

    private Llibre llibreActual;

    @FXML
    public void initialize() {
        disponibilitatComboBox.getItems().addAll("Sí", "No");
        disponibilitatComboBox.setPromptText("Disponibilitat");

        eliminarLlibreButton.setOnAction(e -> eliminarLlibre());
        guardarLlibreButton.setOnAction(e -> guardarLlibre());

        carregarAutors();
        carregarGeneres();

        comboAutors.setConverter(new javafx.util.StringConverter<Autor>() {
            @Override
            public String toString(Autor autor) {
                return autor != null ? autor.getName() : "";
            }

            @Override
            public Autor fromString(String string) {
                return null;
            }
        });

        comboGeneres.setConverter(new javafx.util.StringConverter<Genere>() {
            @Override
            public String toString(Genere genere) {
                return genere != null ? genere.getName() : "";
            }

            @Override
            public Genere fromString(String string) {
                return null;
            }
        });
    }

    public void setLlibre(Llibre llibre) {
        this.llibreActual = llibre;

        if (llibre != null) {
            titolLabel.setText("Editar o eliminar llibre");
            titolField.setText(llibre.getTitle());
            isbnField.setText(llibre.getIsbn());
            dataPublicacioField.setText(llibre.getPublished_date());
            disponibilitatComboBox.setValue(llibre.isAvailable() ? "Sí" : "No");

            // Seleccionar autor si coincideix pel id
            if (llibre.getAuthor() != null) {
                for (Autor autor : comboAutors.getItems()) {
                    if (autor.getId() == llibre.getAuthor().getId()) {
                        comboAutors.setValue(autor);
                        break;
                    }
                }
            }

            // Seleccionar gènere si coincideix pel id
            if (llibre.getGenre() != null) {
                for (Genere genere : comboGeneres.getItems()) {
                    if (genere.getId() == llibre.getGenre().getId()) {
                        comboGeneres.setValue(genere);
                        break;
                    }
                }
            }

            eliminarLlibreButton.setDisable(false);
        } else {
            titolLabel.setText("Nou llibre");
            titolField.clear();
            isbnField.clear();
            dataPublicacioField.clear();
            disponibilitatComboBox.setValue("Sí");
            comboAutors.setValue(null);
            comboGeneres.setValue(null);
            eliminarLlibreButton.setDisable(true);
        }
    }


    private void guardarLlibre() {
        String titol = titolField.getText().trim();
        String isbn = isbnField.getText().trim();
        String publicacio = dataPublicacioField.getText().trim();
        String dispo = disponibilitatComboBox.getValue();
        boolean disponible = "Sí".equalsIgnoreCase(dispo);

        Autor autor = comboAutors.getValue();
        Genere genere = comboGeneres.getValue();

        if (titol.isEmpty() || isbn.isEmpty() || publicacio.isEmpty() || autor == null || genere == null) {
            mostrarAlerta("Error", "Tots els camps són obligatoris.");
            return;
        }

        String json = String.format("""
        {
            "title": "%s",
            "isbn": "%s",
            "publishedDate": "%s",
            "available": %s,
            "authorId": %d,
            "genreId": %d
        }
        """, titol, isbn, publicacio, disponible, autor.getId(), genere.getId());

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request;

            if (llibreActual == null) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/books"))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            } else {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/books/" + llibreActual.getId()))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            }

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            Platform.runLater(() -> {
                                mostrarAlerta("Èxit", "Llibre desat correctament.");
                                tancarFinestra();
                            });
                        } else {
                            Platform.runLater(() -> mostrarAlerta("Error", "Error al desar el llibre. Codi: " + response.statusCode()));
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "S'ha produït un error inesperat.");
        }
    }

    private void eliminarLlibre() {
        if (llibreActual == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar llibre");
        confirm.setHeaderText("Estàs segur que vols eliminar aquest llibre?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/books/" + llibreActual.getId()))
                    .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                    .DELETE()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 204) {
                            Platform.runLater(() -> {
                                mostrarAlerta("Èxit", "Llibre eliminat correctament.");
                                tancarFinestra();
                            });
                        } else {
                            Platform.runLater(() -> mostrarAlerta("Error", "No s'ha pogut eliminar el llibre. Codi: " + response.statusCode()));
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "S'ha produït un error inesperat.");
        }
    }


    private void tancarFinestra() {
        Stage stage = (Stage) guardarLlibreButton.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }

    private void carregarAutors() {
        try {
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
                            List<Autor> autors = mapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<List<Autor>>() {});
                            Platform.runLater(() -> comboAutors.getItems().setAll(autors));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarGeneres() {
        try {
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
                            List<Genere> generes = mapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<List<Genere>>() {});
                            Platform.runLater(() -> comboGeneres.getItems().setAll(generes));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}