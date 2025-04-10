package com.codexteam.codexlib.controllers.adminpanelcontrollers;

import com.codexteam.codexlib.controllers.objectdetailscontrollers.GestionarLlibresController;
import com.codexteam.codexlib.models.Llibre;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
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
    }

    /**
     * Fa una petició GET a l’API REST per obtenir el llistat complet de llibres
     * i actualitza la taula amb els resultats.
     */
    private void carregarLlibres() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/books"))
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
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/book.png")));

            // Desactivar el focus per defecte als inputs
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
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/book.png")));
            stage.showAndWait();

            carregarLlibres();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
