package com.codexteam.codexlib.controllers;

import com.codexteam.codexlib.models.Autor;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.codexteam.codexlib.models.Genere;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Controlador de la finestra de detalls d'un llibre.
 * Aquesta finestra mostra informació detallada d'un llibre obtinguda des de la API de Open Library.
 */
public class NouLlibreApiController {

    @FXML private ImageView portadaImageView;
    @FXML private TextField titolTextField;
    @FXML private TextField autorTextField; // S'ha de canviar per un combobox que reculli els autors
    @FXML private TextField genereTextField; // S'ha de canviar per un combobox que reculli els gèneres
    @FXML private TextField dataPublicacioTextField;
    @FXML private TextField isbnTextField;
    @FXML private ComboBox<Autor> comboAutors;
    @FXML private ComboBox<Genere> comboGeneres;
    @FXML private ComboBox<String> disponibilitatComboBox;
    @FXML private Button guardarButton;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Mostra la informació detallada d’un llibre en els camps corresponents de la finestra.
     *
     * @param titol         Títol del llibre.
     * @param autorNom         Nom de l’autor.
     * @param dataPublicacio Data de publicació.
     * @param isbn          Codi ISBN.
     * @param portadaUrl    URL de la imatge de la portada (pot ser null o "Sense portada").
     */
    public void mostrarDetallsLlibre(String titol, String autorNom, String dataPublicacio, String isbn, String portadaUrl) {
        titolTextField.setText(titol);
        dataPublicacioTextField.setText(dataPublicacio);
        isbnTextField.setText(isbn);

        // Mostrar portada
        if (portadaUrl != null && !portadaUrl.equals("Sense portada")) {
            portadaImageView.setImage(new Image(portadaUrl));
        }

        // Comprovar si l'autor ja existeix
        Autor autorTrobat = comboAutors.getItems().stream()
                .filter(a -> a.getName().equalsIgnoreCase(autorNom))
                .findFirst()
                .orElse(null);

        if (autorTrobat != null) {
            comboAutors.setValue(autorTrobat);
        } else {
            // Crear l'autor al servidor
            Autor autorNou = crearAutor(autorNom);
            if (autorNou != null) {
                comboAutors.getItems().add(autorNou);
                comboAutors.setValue(autorNou);
            }
        }
    }

    /**
     * Inicialitza el controlador després de carregar l’FXML.
     * Omple els ComboBox amb les dades disponibles i configura els converters.
     */
    @FXML
    public void initialize() {
        disponibilitatComboBox.getItems().addAll("Sí", "No");
        disponibilitatComboBox.setPromptText("Disponibilitat:");

        List<Autor> autors = obtenirAutors();
        List<Genere> generes = obtenirGeneres();

        comboAutors.getItems().addAll(autors);
        comboGeneres.getItems().addAll(generes);

        guardarButton.setOnAction(event -> crearNouLlibre());

        // Mostrar només el nom
        comboAutors.setConverter(new StringConverter<Autor>() {
            @Override
            public String toString(Autor autor) {
                return autor != null ? autor.getName() : "";
            }

            @Override
            public Autor fromString(String s) {
                return null; // No s'utilitza
            }
        });

        comboGeneres.setConverter(new StringConverter<Genere>() {
            @Override
            public String toString(Genere genere) {
                return genere != null ? genere.getName() : "";
            }

            @Override
            public Genere fromString(String s) {
                return null;
            }
        });
    }

    /**
     * Obté la llista d’autors del servidor.
     * @return una llista d’objectes Autor.
     */
    private List<Autor> obtenirAutors() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/authors"))
                    .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), new TypeReference<List<Autor>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Obté la llista de gèneres del servidor.
     * @return una llista d’objectes Genere.
     */
    private List<Genere> obtenirGeneres() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/genres"))
                    .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), new TypeReference<List<Genere>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Crea un autor nou al servidor si no existeix.
     * @param nomAutor nom de l’autor.
     * @return l’autor creat o null si no s’ha pogut crear.
     */
    private Autor crearAutor(String nomAutor) {
        try {
            String json = String.format("""
            {
              "name": "%s",
              "birthDate": "0000-01-01",
              "nationality": "Desconeguda"
            }
        """, nomAutor);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/authors"))
                    .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body(), Autor.class);
            } else {
                System.err.println("No s'ha pogut crear l'autor. Codi: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Envia les dades del formulari per crear un nou llibre al servidor.
     */
    private void crearNouLlibre() {
        String titol = titolTextField.getText().trim();
        String isbn = isbnTextField.getText().trim();
        String dataPublicacio = dataPublicacioTextField.getText().trim();
        String dispo = disponibilitatComboBox.getValue();
        boolean disponible = "Sí".equalsIgnoreCase(dispo);

        Autor autorSeleccionat = comboAutors.getValue();
        Genere genereSeleccionat = comboGeneres.getValue();

        if (titol.isEmpty() || isbn.isEmpty() || dataPublicacio.isEmpty() || autorSeleccionat == null || genereSeleccionat == null) {
            mostrarAlerta("Error", "Tots els camps són obligatoris.");
            return;
        }

        // Crear JSON
        String json = String.format("""
        {
          "title": "%s",
          "authorId": %d,
          "isbn": "%s",
          "publishedDate": "%s",
          "genreId": %d,
          "available": %s
        }
        """, titol, autorSeleccionat.getId(), isbn, dataPublicacio, genereSeleccionat.getId(), disponible);

        try {
            System.out.println("Token actual: " + ConnexioServidor.getTokenSessio());
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/books"))
                    .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::statusCode)
                    .thenAccept(status -> {
                        if (status == 200 || status == 201) {
                            Platform.runLater(() -> mostrarAlerta("Èxit", "Llibre creat correctament!"));
                        } else {
                            Platform.runLater(() -> mostrarAlerta("Error", "No s'ha pogut crear el llibre. Codi: " + status));
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "S'ha produït un error inesperat.");
        }
    }

    /**
     * Mostra un missatge d'error si el login no és correcte
     *
     * @param titol Títol de l'alerta.
     * @param missatge Contingut del missatge.
     */
    private void mostrarAlerta(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }

}