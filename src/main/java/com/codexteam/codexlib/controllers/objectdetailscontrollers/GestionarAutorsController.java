package com.codexteam.codexlib.controllers.objectdetailscontrollers;

import com.codexteam.codexlib.models.Autor;
import com.codexteam.codexlib.services.ClientFactory;
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
import java.util.Optional;

/**
 * Controlador per a la finestra modal de gestió d'autors.
 *
 * <p>Aquesta classe permet afegir, modificar i eliminar autors mitjançant peticions a l’API REST.</p>
 *
 * <p>El controlador detecta si s’està treballant amb un autor existent o si es tracta de la creació
 * d’un nou autor, i canvia el comportament de la finestra en conseqüència (canvia el títol i habilita/deshabilita
 * el botó d'eliminació).</p>
 *
 * <p>Les accions de creació i actualització s'envien al backend mitjançant peticions HTTP de tipus
 * POST o PUT, respectivament. La petició DELETE només s'envia si l’usuari confirma l’eliminació.</p>
 *
 * Aquesta classe s’associa al fitxer FXML <strong>gestionarAutorsView.fxml</strong>.
 *
 * @author Miquel Correyero
 */
public class GestionarAutorsController {

    @FXML private TextField nomTextField;
    @FXML private TextField dataNaixementTextField;
    @FXML private TextField nacionalitatTextField;
    @FXML private Button guardarAutorButton;
    @FXML private Button eliminarAutorButton;
    @FXML private Label titolLabel;

    private Autor autorActual; // null si és nou
    private Autor autorCreat = null;

    /**
     * Inicialitza el controlador, assignant els esdeveniments als botons.
     */
    @FXML
    public void initialize() {
        guardarAutorButton.setOnAction(e -> guardarAutor());
        eliminarAutorButton.setOnAction(e -> eliminarAutor());
    }

    public Autor getAutorCreat() {
        return autorCreat;
    }

    /**
     * Assigna l'autor a editar, o null si és un autor nou.
     * @param autor L'autor a editar (pot ser null).
     */
    public void setAutor(Autor autor) {
        this.autorActual = autor;

        if (autor != null) {
            titolLabel.setText("Editar o eliminar l'autor");
            nomTextField.setText(autor.getName());
            dataNaixementTextField.setText(autor.getBirth_date());
            nacionalitatTextField.setText(autor.getNationality());
            eliminarAutorButton.setDisable(false); // activar si estem editant l'auotr
        } else {
            titolLabel.setText("Nou autor");
            eliminarAutorButton.setDisable(true); // desactivar si estem creant l'autor
        }
    }

    /**
     * Envia una petició al servidor per guardar o actualitzar l'autor.
     * Utilitza POST si és nou, o PUT si ja existeix.
     */
    private void guardarAutor() {
        String nom = nomTextField.getText().trim();
        String data = dataNaixementTextField.getText().trim();
        String nacionalitat = nacionalitatTextField.getText().trim();

        if (nom.isEmpty() || data.isEmpty() || nacionalitat.isEmpty()) {
            mostrarAlerta("Error", "Tots els camps són obligatoris.");
            return;
        }

        try {
            HttpClient client = ClientFactory.getClient();
            ObjectMapper mapper = new ObjectMapper();

            String json = String.format("""
                {
                  "name": "%s",
                  "birthDate": "%s",
                  "nationality": "%s"
                }
            """, nom, data, nacionalitat);

            HttpRequest request;

            if (autorActual == null) {
                // Crear autor nou (POST)
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://localhost/authors"))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            } else {
                // Actualitzar autor existent (PUT)
                request = HttpRequest.newBuilder()
                        .uri(URI.create("https://localhost/authors/" + autorActual.getId()))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            }

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            Platform.runLater(() -> {
                                try {
                                    Autor autorInsertat = mapper.readValue(response.body(), Autor.class);
                                    autorCreat = autorInsertat;

                                    mostrarAlerta("Èxit", autorActual == null ? "Autor creat correctament!" : "Autor actualitzat correctament!");
                                    ((Stage) guardarAutorButton.getScene().getWindow()).close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mostrarAlerta("Error", "No s'ha pogut interpretar la resposta del servidor.");
                                }
                            });
                        } else {
                            Platform.runLater(() -> mostrarAlerta("Error", "Error al desar l'autor. Codi: " + response.statusCode()));
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "S'ha produït un error inesperat.");
        }
    }

    /**
     * Elimina l'autor actual després de confirmar-ho amb l'usuari.
     * Envia una petició DELETE al servidor.
     */
    private void eliminarAutor() {
        if (autorActual == null) {
            mostrarAlerta("Error", "No es pot eliminar un autor nou.");
            return;
        }

        // Confirmació
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar eliminació");
        confirmAlert.setHeaderText("Segur que vols eliminar aquest autor?");
        confirmAlert.setContentText("Aquesta acció és irreversible.");

        Optional<ButtonType> resultat = confirmAlert.showAndWait();
        if (resultat.isEmpty() || resultat.get() != ButtonType.OK) {
            return;
        }

        try {
            HttpClient client = ClientFactory.getClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://localhost/authors/" + autorActual.getId()))
                    .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                    .DELETE()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 204) {
                            Platform.runLater(() -> {
                                mostrarAlerta("Èxit", "Autor eliminat correctament.");
                                ((Stage) eliminarAutorButton.getScene().getWindow()).close();
                            });
                        } else {
                            Platform.runLater(() -> mostrarAlerta("Error", "No s'ha pogut eliminar l'autor. Codi: " + response.statusCode()));
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "S'ha produït un error inesperat.");
        }
    }

    /**
     * Mostra un diàleg d'alerta amb un missatge personalitzat.
     *
     * @param titol    Títol de la finestra emergent.
     * @param missatge Contingut del missatge a mostrar.
     */
    private void mostrarAlerta(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }
}