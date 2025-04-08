package com.codexteam.codexlib.controllers.admincontrollers;

import com.codexteam.codexlib.controllers.GestionarLlibresController;
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

public class LlibresController {

    // COLUMNES DE LA TAULA DE LLIBRES
    @FXML
    private TableView<Llibre> taulaLlibres;
    @FXML private TableColumn<Llibre, String> colTitol;
    @FXML private TableColumn<Llibre, String> colAutor;
    @FXML private TableColumn<Llibre, String> colIsbn;
    @FXML private TableColumn<Llibre, String> colDisponibilitat;

    // BOTONS
    @FXML private Button inserirNouLlibreButton; // Cercar llibre per ISBN

    @FXML
    public void initialize() {

        // Mostrar finestra per a cercar llibre per ISBN
        inserirNouLlibreButton.setOnAction(event ->
                obrirNovaFinestra("/com/codexteam/codexlib/fxml/isbnView.fxml", "Cercar llibre per ISBN", "/com/codexteam/codexlib/images/isbn.png")
        );

        // EDITA EL LLIBRE EN FER DOBLE CLIC SOBRE UNA FILA
        taulaLlibres.setRowFactory(tv -> {
            TableRow<Llibre> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !fila.isEmpty()) {
                    Llibre llibreSeleccionat = fila.getItem();
                    obrirGestionarLlibre(llibreSeleccionat);
                }
            });
            return fila;
        });

        // CARREGAR LLISTAT DE LLIBRES
        colTitol.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colDisponibilitat.setCellValueFactory(cellData -> {
            boolean disponible = cellData.getValue().isAvailable();
            return new SimpleStringProperty(disponible ? "Sí" : "No");
        });

        carregarLlibres();

    }

    //=====================================================
    //        OBTENIR LLISTAT DE LLIBRES DEL CATÀLEG
    //=====================================================
    /**
     * Obté el llistat de llibres del servidor mitjançant una petició HTTP
     * i els mostra a la taula de llibres.
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

    private void obrirGestionarLlibre(Llibre llibre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestionarLlibresView.fxml"));
            Parent root = loader.load();

            GestionarLlibresController controller = loader.getController();
            controller.setLlibre(llibre); // null si és nou

            Stage stage = new Stage();
            stage.setTitle(llibre == null ? "Nou llibre" : "Editar llibre");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/book.png")));
            stage.showAndWait();

            carregarLlibres(); // Refrescar taula
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //=====================================================
    //              OBRIR UNA NOVA FINESTRA
    //=====================================================
    /**
     * Obre una nova finestra modal amb el FXML, títol i icona especificats.
     *
     * @param fxml Ruta del fitxer FXML.
     * @param nomFinestra Títol de la finestra.
     * @param icona Ruta de la icona de la finestra.
     */
    private void obrirNovaFinestra(String fxml, String nomFinestra, String icona) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(nomFinestra);
            stage.setScene(new Scene(root));

            // Icona de la finestra
            stage.getIcons().add(new Image(getClass().getResourceAsStream(icona)));

            // BloqueJa la finestra principal fins a tancar aquesta
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
