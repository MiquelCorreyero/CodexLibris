package com.codexteam.codexlib.controllers.adminpanelcontrollers;

import com.codexteam.codexlib.controllers.objectdetailscontrollers.GestionarAutorsController;
import com.codexteam.codexlib.models.Autor;
import com.codexteam.codexlib.models.ResultatCerca;
import com.codexteam.codexlib.services.ClientFactory;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Controlador del panell d’administració encarregat de gestionar els autors de la biblioteca.
 * Aquesta classe permet visualitzar la llista d’autors registrats al sistema, afegir-ne de nous,
 * editar-ne les dades o accedir als seus detalls.
 *
 * <p>Els autors es carreguen mitjançant una petició HTTP GET a l’API REST i es mostren
 * dins una taula. Es poden obrir finestres emergents per gestionar un autor en concret
 * amb un doble clic o mitjançant el botó de Nou autor.</p>
 *
 * <p>El controlador utilitza l’FXML <code>gestionarAutorsView.fxml</code> per mostrar la interfície
 * de detall d’un autor.</p>
 *
 * @author Miquel Correyero
 */
public class AutorsController {

    // COLUMNES DE LA TAULA D'AUTORS
    @FXML
    private TableView<Autor> taulaAutors;
    @FXML private TableColumn<Autor, String> colIdAutor;
    @FXML private TableColumn<Autor, String> colNomAutor;
    @FXML private TableColumn<Autor, String> colNacionalitatAutor;
    @FXML private TableColumn<Autor, String> colDataAutor;

    // BOTONS
    @FXML private Button inserirNouAutorButton; // Inserir o editar autor
    @FXML private Button botoCercaAutor;
    @FXML private Button netejaAutorButton;

    @FXML private TextField campCercaAutor;

    /**
     * Inicialitza el panell d'autors:
     * - Assigna els valors a les columnes de la taula.
     * - Carrega la llista d'autors des del servidor.
     * - Defineix el comportament en fer doble clic sobre un autor.
     * - Configura el botó per afegir un nou autor.
     */
    @FXML
    public void initialize() {

        // CARREGAR LLISTAT D'AUTORS
        colIdAutor.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomAutor.setCellValueFactory(new PropertyValueFactory<>("name"));
        colNacionalitatAutor.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        colDataAutor.setCellValueFactory(new PropertyValueFactory<>("birth_date"));

        carregarAutors();

        // EDITA L'AUTOR EN FER DOBLE CLIC SOBRE UNA FILA
        taulaAutors.setRowFactory(tv -> {
            TableRow<Autor> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !fila.isEmpty()) {
                    Autor autorSeleccionat = fila.getItem();
                    obrirGestionarAutors(autorSeleccionat);
                }
            });
            return fila;
        });

        // OBRE LA FINESTRA PER CREAR UN NOU AUTOR
        inserirNouAutorButton.setOnAction(e -> obrirGestionarAutors(null));

        // Botó de cerca
        botoCercaAutor.setOnAction(e -> ferCercaAutors());

        // Botó de neteja
        netejaAutorButton.setOnAction(e -> {
            campCercaAutor.clear();
            carregarAutors();
        });

        // Enter per cercar
        campCercaAutor.setOnAction(e -> ferCercaAutors());

        // Desactiva botó de neteja si el camp és buit
        campCercaAutor.textProperty().addListener((obs, oldVal, newVal) -> {
            netejaAutorButton.setDisable(newVal.trim().isEmpty());
        });

    }

    /**
     * Envia una petició HTTP GET al servidor per obtenir tots els autors.
     * Si la resposta és correcta, actualitza la taula amb els resultats.
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
                        mapper.registerModule(new JavaTimeModule());

                        List<Autor> autors = mapper.readValue(
                                response,
                                new TypeReference<List<Autor>>() {}
                        );

                        Platform.runLater(() -> {
                            taulaAutors.getItems().setAll(autors);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> mostrarMissatge("Error", "No s'han pogut carregar els autors."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarMissatge("Error", "Error en la connexió amb el servidor."));
                    return null;
                });
    }

    /**
     * Obre una nova finestra modal per editar o crear un autor.
     *
     * @param autor L'autor a editar, o {@code null} si es vol crear un de nou.
     */
    private void obrirGestionarAutors(Autor autor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarAutorsView.fxml"));
            Parent root = loader.load();

            GestionarAutorsController controller = loader.getController();
            controller.setAutor(autor); // null si és nou

            Stage stage = new Stage();
            stage.setTitle(autor == null ? "Nou autor" : "Editar autor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/id_user_icon.png")));

            // Desactivar el focus per defecte als inputs
            stage.setOnShown(e -> Platform.runLater(() -> root.requestFocus()));

            stage.showAndWait();

            carregarAutors(); // Refresca la taula
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra un missatge d’informació amb el títol i contingut especificats.
     *
     * @param title Títol de la finestra d’alerta.
     * @param message Missatge que es mostrarà a l’usuari.
     */
    private void mostrarMissatge(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Realitza una cerca d'autors a la base de dades mitjançant una paraula clau.
     *
     * <p>Aquest mètode envia una petició GET a l’endpoint de cerca de l’API REST
     * amb la paraula clau introduïda per l’usuari. Si el camp de text és buit,
     * es torna a carregar la llista completa d’autors.</p>
     *
     * <p>Els resultats obtinguts substitueixen el contingut actual de la taula
     * mostrant únicament els autors que coincideixen amb el criteri de cerca.</p>
     *
     * <p>En cas d’error en la resposta o la connexió, es mostra una alerta a l’usuari.</p>
     */
    private void ferCercaAutors() {
        String paraulaClau = campCercaAutor.getText().trim();
        if (paraulaClau.isEmpty()) {
            carregarAutors();
            return;
        }

        HttpClient client = ClientFactory.getClient();
        String url = "https://localhost/search/?query=" + paraulaClau.replace(" ", "%20");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());

                        // Clase wrapper per a la resposta
                        ResultatCerca resultat = mapper.readValue(response, ResultatCerca.class);
                        List<Autor> autors = resultat.getAuthors();

                        Platform.runLater(() -> taulaAutors.getItems().setAll(autors));

                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> mostrarMissatge("Error", "No s'han pogut carregar els autors trobats."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarMissatge("Error", "Error en la connexió amb el servidor."));
                    return null;
                });
    }


}