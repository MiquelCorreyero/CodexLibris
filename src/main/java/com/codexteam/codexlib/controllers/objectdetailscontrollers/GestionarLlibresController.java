package com.codexteam.codexlib.controllers.objectdetailscontrollers;

import com.codexteam.codexlib.models.Autor;
import com.codexteam.codexlib.models.LlibreExtern;
import com.codexteam.codexlib.services.ClientFactory;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.codexteam.codexlib.models.Genere;
import com.codexteam.codexlib.models.Llibre;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Controlador de la finestra modal encarregada de gestionar la inserció i edició de llibres des del catàleg extern.
 * Proporciona la funcionalitat per omplir un formulari amb dades del llibre, seleccionar autor i gènere,
 * i enviar les dades mitjançant peticions HTTP al backend (POST per crear, PUT per editar, DELETE per eliminar).
 *
 * Aquest controlador també carrega automàticament la llista d’autors i gèneres per mostrar-los als ComboBox.
 *
 *  Aquesta classe s’associa al fitxer FXML <strong>gestionarLlibresView.fxml</strong>.
 *
 * @author Miquel Correyero
 */
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
    @FXML private Button botoNouAutor;
    @FXML private Button botoNouGenere;

    private Llibre llibreActual;

    /**
     * Inicialitza els components del formulari.
     * Assigna opcions i accions als botons i comboboxos.
     */
    @FXML
    public void initialize() {
        disponibilitatComboBox.getItems().addAll("Sí", "No");
        disponibilitatComboBox.setPromptText("Disponibilitat");

        eliminarLlibreButton.setOnAction(e -> eliminarLlibre());
        guardarLlibreButton.setOnAction(e -> guardarLlibre());
        botoNouAutor.setOnAction(e -> obrirFinestraNouAutor());
        botoNouGenere.setOnAction(e -> obrirFinestraNouGenere());

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

    /**
     * Omple els camps del formulari amb la informació d’un llibre existent.
     * Si es rep null, s’inicialitza per crear un llibre nou.
     *
     * @param llibre El llibre a editar, o null si és nou.
     */
    public void seleccionarLlibre(Llibre llibre) {
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

    /**
     * Recull les dades del formulari i envia una petició POST o PUT
     * segons si el llibre és nou o existent.
     */
    private void guardarLlibre() {
        String titol = titolField.getText().trim();
        String isbn = isbnField.getText().trim();
        String dataInput = dataPublicacioField.getText().trim();
        String dispo = disponibilitatComboBox.getValue();
        boolean disponible = "Sí".equalsIgnoreCase(dispo);

        Autor autor = comboAutors.getValue();
        Genere genere = comboGeneres.getValue();

        if (titol.isEmpty() || isbn.isEmpty() || dataInput.isEmpty() || autor == null || genere == null) {
            mostrarAlerta("Error", "Tots els camps són obligatoris.");
            return;
        }

        // Comprovem que el format de l'ISBN sigui correcte o mostrem un error en pantalla
        if (!isbnValid(isbn)) {
            mostrarAlerta("Error", "L'ISBN introduït no té el format correcte.\nEl format hauria de ser com aquest: 978-84-123456-01");
            return;
        }

        // Convertir manualment la data amb hora i zona
        String publicacio = dataInput + "T00:00:00.000Z";

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

        System.out.println("JSON generat:\n" + json);

        try {
            HttpClient client = ClientFactory.getClient();
            HttpRequest request;

            if (llibreActual == null) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://localhost/books"))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            } else {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://localhost/books/" + llibreActual.getId()))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            }

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            mostrarAlerta("Èxit", "Llibre desat correctament.");
                            tancarFinestra();
                        } else {
                            mostrarAlerta("Error", "Error al desar el llibre. Codi: " + response.statusCode() +
                                    "\nResposta: " + response.body());
                        }
                    }));

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "S'ha produït un error inesperat.");
        }
    }

    /**
     * Envia una petició DELETE per eliminar el llibre actual,
     * si l’usuari ho confirma.
     */
    private void eliminarLlibre() {
        if (llibreActual == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar llibre");
        confirm.setHeaderText("Estàs segur que vols eliminar aquest llibre?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            HttpClient client = ClientFactory.getClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://localhost/books/" + llibreActual.getId()))
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

    /**
     * Obre una finestra modal per crear un nou autor.
     * Mostra un missatge d'error si hi ha problemes en carregar la vista.
     */
    private void obrirFinestraNouAutor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarAutorsView.fxml"));
            Parent root = loader.load();

            GestionarAutorsController controller = loader.getController();
            controller.setAutor(null);

            Stage stage = new Stage();
            stage.setTitle("Nou autor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setOnShown(event -> Platform.runLater(() -> root.requestFocus()));
            stage.showAndWait();

            carregarAutors();

            Autor nou = controller.getAutorCreat();
            if (nou != null) {
                comboAutors.getItems().add(nou);
                comboAutors.setValue(nou);
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No s'ha pogut obrir la finestra de creació d'autor.");
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
            controller.setGenere(null);

            Stage stage = new Stage();
            stage.setTitle("Nou gènere");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setOnShown(event -> Platform.runLater(() -> root.requestFocus()));
            stage.showAndWait();

            carregarGeneres();

            Genere nou = controller.getGenereCreat();
            if (nou != null) {
                comboGeneres.getItems().add(nou);
                comboGeneres.setValue(nou);
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No s'ha pogut obrir la finestra de creació de gènere.");
        }
    }

    /**
     * Elimina accents i normalitza un text a minúscules per fer comparacions més tolerants.
     */
    private String normalitzarText(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(normalized)
                .replaceAll("")
                .toLowerCase();
    }

    /**
     * Tanca la finestra actual.
     */
    private void tancarFinestra() {
        Stage stage = (Stage) guardarLlibreButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Mostra una alerta informativa.
     *
     * @param titol Títol de l’alerta.
     * @param missatge Missatge a mostrar.
     */
    private void mostrarAlerta(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }

    /**
     * Fa una petició GET a l’API per obtenir els autors disponibles i omplir el ComboBox.
     */
    private void carregarAutors(Runnable callback) {
        try {
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
                            List<Autor> autors = mapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<List<Autor>>() {});
                            Platform.runLater(() -> {
                                comboAutors.getItems().setAll(autors);
                                if (callback != null) callback.run(); // << ejecutar después
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarAutors() {
        carregarAutors(null);
    }

    /**
     * Fa una petició GET a l’API per obtenir els gèneres disponibles i omplir el ComboBox.
     */
    private void carregarGeneres() {
        try {
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

    /**
     * Prepara el formulari per importar un llibre extern,
     * omplint els camps amb les dades rebudes i intentant seleccionar l'autor corresponent.
     *
     * @param extern Llibre extern a importar, amb títol, ISBN i autor.
     */
    public void importarLlibreExtern(LlibreExtern extern) {
        this.llibreActual = null;

        titolLabel.setText("Importar llibre extern");
        titolField.setText(extern.getTitle());
        isbnField.setText(extern.getIsbn());
        dataPublicacioField.setText(extern.getYear() + "-01-01");
        disponibilitatComboBox.setValue("Sí");

        comboAutors.setValue(null);
        comboGeneres.setValue(null);
        eliminarLlibreButton.setDisable(true);

        String nomExternNormalitzat = normalitzarText(extern.getAuthor());

        carregarAutors(() -> {
            for (Autor autor : comboAutors.getItems()) {
                if (normalitzarText(autor.getName()).equals(nomExternNormalitzat)) {
                    comboAutors.setValue(autor);
                    break;
                }
            }
        });

        carregarGeneres();
    }

    /**
     * Comprova si un ISBN compleix el format {@code XXX-XX-XXXXXX-XX}, només amb dígits.
     *
     * @param isbn ISBN a validar.
     * @return {@code true} si l'ISBN és vàlid, {@code false} altrament.
     */
    private boolean isbnValid(String isbn) {
        return isbn.matches("\\d{3}-\\d{2}-\\d{6}-\\d{2}");
    }

}