package com.codexteam.codexlib.controllers.adminpanelcontrollers;

import com.codexteam.codexlib.controllers.objectdetailscontrollers.GestionarLlibresController;
import com.codexteam.codexlib.controllers.objectdetailscontrollers.GestionarReservesController;
import com.codexteam.codexlib.models.Llibre;
import com.codexteam.codexlib.models.ResultatCerca;
import com.codexteam.codexlib.models.Usuari;
import com.codexteam.codexlib.services.ClientFactory;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
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
 * Controlador del panell d’administració dedicat a la gestió del catàleg de llibres.
 *
 * <p>Aquesta classe permet visualitzar, afegir i editar llibres mitjançant
 * peticions a l'API REST i finestres modals. Els llibres es mostren en una
 * taula amb informació bàsica i es poden modificar amb un sistema intuïtiu d'interacció.</p>
 *
 * @author Miquel Correyero
 */
public class LlibresController {

    @FXML private TableView<Llibre> taulaLlibres;
    @FXML private TableColumn<Llibre, String> colTitol;
    @FXML private TableColumn<Llibre, String> colAutor;
    @FXML private TableColumn<Llibre, String> colIsbn;
    @FXML private TableColumn<Llibre, String> colDisponibilitat;

    @FXML private Button inserirNouLlibreButton;
    @FXML private TextField campCerca;
    @FXML private Button botoCerca;
    @FXML private Button netejaButton;
    @FXML private Button reservarButton;

    @FXML
    public void initialize() {

        // Obre la finestra de creació de llibres
        inserirNouLlibreButton.setOnAction(event -> obrirFinestraNouLlibre());

        // Obre la finestra per editar llibres en fer doble clic
        taulaLlibres.setRowFactory(tv -> {
            TableRow<Llibre> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !fila.isEmpty()) {
                    Llibre llibreSeleccionat = fila.getItem();
                    obrirFinestraEditarLlibre(llibreSeleccionat);
                }
            });
            return fila;
        });

        // Configuració de columnes
        colTitol.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colDisponibilitat.setCellValueFactory(cellData -> {
            boolean disponible = cellData.getValue().isAvailable();
            return new SimpleStringProperty(disponible ? "Sí" : "No");
        });

        carregarLlibres();

        // Cercar llibres
        botoCerca.setOnAction(event -> ferCerca());
        campCerca.setOnAction(event -> ferCerca()); // Per cercar presionant la tecla enter

        // Botó de reset per a la cerca
        netejaButton.setOnAction(event -> {
            campCerca.clear();      // Neteja el text field
            carregarLlibres();      // Torna a carregar tots els llibres
        });

        // Desactiva el botó si no s'ha fet cap cerca
        netejaButton.setDisable(true);
        campCerca.textProperty().addListener((obs, oldVal, newVal) -> {
            netejaButton.setDisable(newVal.trim().isEmpty());
        });

        // Obre la finestra de reservar llibre
        reservarButton.setOnAction(event -> obrirFinestraReservaLlibre());

        // Oculta el botó de nou llibre si l'usuari no és admin
        if (ConnexioServidor.getTipusUsuari() != 1) {
            inserirNouLlibreButton.setVisible(false);
        }

    }

    /**
     * Fa una petició GET a l’API REST per obtenir el llistat complet de llibres
     * i actualitza la taula amb els resultats.
     */
    private void carregarLlibres() {
        HttpClient client = ClientFactory.getClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost/books"))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List<Llibre> llibres = mapper.readValue(
                                response,
                                new TypeReference<List<Llibre>>() {}
                        );
                        Platform.runLater(() -> {
                            taulaLlibres.getItems().setAll(llibres);
                        });
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
     * Obre una finestra modal amb el formulari simplificat per afegir un nou llibre.
     * Un cop tancada, es refresca la taula per reflectir els possibles canvis.
     */
    private void obrirFinestraNouLlibre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarNouLlibreView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nou llibre");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/book_w.png")));

            // Desactivar el focus per defecte als inputs
            stage.setOnShown(e -> Platform.runLater(() -> root.requestFocus()));

            stage.showAndWait();

            carregarLlibres();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obre una finestra modal amb el formulari per reservar un llibre seleccionat de la taula.
     */
    private void obrirFinestraReservaLlibre() {
        Llibre llibreSeleccionat = taulaLlibres.getSelectionModel().getSelectedItem();

        if (llibreSeleccionat == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cap llibre seleccionat");
            alert.setHeaderText(null);
            alert.setContentText("Selecciona un llibre de la taula abans de fer una reserva.");
            alert.showAndWait();
            return;
        }

        if (!llibreSeleccionat.isAvailable()) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Llibre no disponible");
            alerta.setHeaderText(null);
            alerta.setContentText("Aquest llibre no està disponible per a reserves.");
            alerta.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarReservesView.fxml"));
            Parent root = loader.load();

            GestionarReservesController controller = loader.getController();
            controller.setLlibreSeleccionat(llibreSeleccionat);

            Stage stage = new Stage();
            stage.setTitle("Reservar llibre");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/book_w.png")));
            stage.setOnShown(e -> Platform.runLater(() -> root.requestFocus()));
            stage.showAndWait();

            carregarLlibres();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obre una finestra modal per editar un llibre existent.
     *
     * @param llibre Llibre seleccionat per editar.
     */
    private void obrirFinestraEditarLlibre(Llibre llibre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarLlibresView.fxml"));
            Parent root = loader.load();

            GestionarLlibresController controller = loader.getController();
            controller.seleccionarLlibre(llibre);

            Stage stage = new Stage();
            stage.setTitle("Editar llibre");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/book_w.png")));
            stage.showAndWait();

            carregarLlibres();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Realitza una cerca de llibres a la base de dades mitjançant una paraula clau.
     */
    private void ferCerca() {
        String paraulaClau = campCerca.getText().trim();
        if (paraulaClau.isEmpty()) {
            carregarLlibres(); // si no hi ha text, carrega tots els llibres
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
                        ResultatCerca resultat = mapper.readValue(response, ResultatCerca.class);
                        List<Llibre> llibres = resultat.getBooks();
                        Platform.runLater(() -> taulaLlibres.getItems().setAll(llibres));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }


}
