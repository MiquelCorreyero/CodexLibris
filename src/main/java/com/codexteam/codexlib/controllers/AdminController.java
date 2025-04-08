package com.codexteam.codexlib.controllers;

import com.codexteam.codexlib.models.Autor;
import com.codexteam.codexlib.models.Llibre;
import com.codexteam.codexlib.models.Reserva;
import com.codexteam.codexlib.services.ConnexioServidor;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static com.codexteam.codexlib.services.ConnexioServidor.getNomUsuariActual;

/**
 * Controlador del panell d'administració, només visible per a usuaris de tipus admin.
 * Gestiona la navegació entre diferents seccions com llibres, usuaris, reserves i esdeveniments.
 * També s'encarrega de carregar les dades dels diferents apartats des del servidor i mostrar-les a les taules corresponents.
 */
public class AdminController {

    //=====================================================
    //            ELEMENTS DE LA INTERFÍCIE
    //=====================================================

    // PANELLS
    @FXML private AnchorPane paneInici;
    @FXML private AnchorPane paneLlibres;
    @FXML private AnchorPane paneAutors;
    @FXML private AnchorPane paneGeneres;
    @FXML private AnchorPane paneUsuaris;
    @FXML private AnchorPane paneReserves;
    @FXML private AnchorPane paneEsdeveniments;

    // LABEL USUARI ACTIU
    @FXML private Label textBenvinguda;

    // IMATGES CLICABLES
    @FXML private ImageView configButton;
    @FXML private ImageView bellButton;

    // COLUMNES DE LA TAULA DE LLIBRES
    @FXML private TableView<Llibre> taulaLlibres;
    @FXML private TableColumn<Llibre, String> colTitol;
    @FXML private TableColumn<Llibre, String> colAutor;
    @FXML private TableColumn<Llibre, String> colIsbn;
    @FXML private TableColumn<Llibre, String> colDisponibilitat;

    // COLUMNES DE LA TAULA D'AUTORS
    @FXML private TableView<Autor> taulaAutors;
    @FXML private TableColumn<Autor, String> colIdAutor;
    @FXML private TableColumn<Autor, String> colNomAutor;
    @FXML private TableColumn<Autor, String> colNacionalitatAutor;
    @FXML private TableColumn<Autor, String> colDataAutor;

    // COLUMNES DE LA TAULA DE RESERVES
    @FXML private TableView<Reserva> taulaReserves;
    @FXML private TableColumn<Reserva, String> colIdReserva;
    @FXML private TableColumn<Reserva, String> colNomReserva;
    @FXML private TableColumn<Reserva, String> colCognomsReserva;
    @FXML private TableColumn<Reserva, String> colEmailReserva;
    @FXML private TableColumn<Reserva, String> colLlibreReserva;
    @FXML private TableColumn<Reserva, String> colDataReserva;
    @FXML private TableColumn<Reserva, String> colDataRetorn;

    // BOTONS
    @FXML private Button inserirNouLlibreButton; // Cercar llibre per ISBN
    @FXML private Button logoutButton; // Logout
    @FXML private Button inserirNouAutorButton; // Inserir o editar autor


    //=====================================================
    //                VISIBILITAT PANELLS
    //=====================================================
    private void hideAllPanes() {
        paneInici.setVisible(false);
        paneLlibres.setVisible(false);
        paneUsuaris.setVisible(false);
        paneAutors.setVisible(false);
        paneGeneres.setVisible(false);
        paneReserves.setVisible(false);
        paneEsdeveniments.setVisible(false);
    }

    /**
     * Amaga tots els panells i mostra el panell d'inici.
     */
    @FXML
    private void showInici() {
        hideAllPanes();
        paneInici.setVisible(true);
    }

    /**
     * Mostra el panell d'usuaris i amaga la resta.
     */
    @FXML
    private void showUsuaris() {
        hideAllPanes();
        paneUsuaris.setVisible(true);

        if (paneUsuaris.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/panellUsuarisView.fxml"));
                Parent usuarisContent = loader.load();
                paneUsuaris.getChildren().setAll(usuarisContent);
                AnchorPane.setTopAnchor(usuarisContent, 0.0);
                AnchorPane.setBottomAnchor(usuarisContent, 0.0);
                AnchorPane.setLeftAnchor(usuarisContent, 0.0);
                AnchorPane.setRightAnchor(usuarisContent, 0.0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mostra el panell de llibres (catàleg) i amaga la resta.
     */
    @FXML
    private void showLlibres() {
        hideAllPanes();
        paneLlibres.setVisible(true);
    }

    /**
     * Mostra el panell d'autors i amaga la resta.
     */
    @FXML
    private void showAutors() {
        hideAllPanes();
        paneAutors.setVisible(true);
    }

    /**
     * Mostra el panell de gèneres i amaga la resta.
     */
    @FXML
    private void showGeneres() {
        hideAllPanes();
        paneGeneres.setVisible(true);
    }

    /**
     * Mostra el panell de reserves i amaga la resta.
     */
    @FXML
    private void showReserves() {
        hideAllPanes();
        paneReserves.setVisible(true);
    }

    /**
     * Mostra el panell d'esdeveniments i amaga la resta.
     */
    @FXML
    private void showEsdeveniments() {
        hideAllPanes();
        paneEsdeveniments.setVisible(true);
    }

    //=====================================================
    //             INICIALITZAR ELS COMPONENTS
    //=====================================================
    /**
     * Inicialitza el controlador després de carregar l'FXML.
     * Configura el comportament dels botons i carrega la llista de llibres.
     */
    @FXML
    public void initialize() {

        // Mostrar el nom de l'usuari que inicia sessió
        textBenvinguda.setText("Benvingut" + formatNomUsuari(getNomUsuariActual()));

        // Mostrar missatge al clicar sobre les notificacions
        bellButton.setOnMouseClicked(event -> mostrarMissatge("Alerta", "Ep! Sóc una notificació!"));
        bellButton.setCursor(javafx.scene.Cursor.HAND);

        // Mostrar finestra per a cercar llibre per ISBN
        inserirNouLlibreButton.setOnAction(event ->
                obrirNovaFinestra("/com/codexteam/codexlib/fxml/isbnView.fxml", "Cercar llibre per ISBN", "/com/codexteam/codexlib/images/isbn.png")
        );

        // Mostrar finestra de configuració
        configButton.setOnMouseClicked(event ->
                obrirNovaFinestra("/com/codexteam/codexlib/fxml/configView.fxml", "Configuració", "/com/codexteam/codexlib/images/config_.png")
        );
        configButton.setCursor(javafx.scene.Cursor.HAND);

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

        // CARREGAR LLISTAT DE RESERVES
        colIdReserva.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomReserva.setCellValueFactory(new PropertyValueFactory<>("user_first_name"));
        colCognomsReserva.setCellValueFactory(new PropertyValueFactory<>("user_name"));
        colEmailReserva.setCellValueFactory(new PropertyValueFactory<>("user_email"));
        colLlibreReserva.setCellValueFactory(new PropertyValueFactory<>("book_title"));
        colDataReserva.setCellValueFactory(new PropertyValueFactory<>("loan_date"));
        colDataRetorn.setCellValueFactory(new PropertyValueFactory<>("return_date"));

        carregarReserves();

    }

    // Mostrar el nom de l'usuari que inicia sessió
    private String formatNomUsuari(String nomUsuari) {
        return (nomUsuari != null && !nomUsuari.trim().isEmpty()) ? ", " + nomUsuari : ".";
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

    //=====================================================
    //                       LOGOUT
    //=====================================================
    /**
     * Tanca la sessió de l'usuari actual i retorna al panell de login.
     */
    @FXML
    private void tancarSessio() {
        // Mostrar alerta per confirmar si vol tancar la sessió
        if (!confirmarTancarSessio()) {
            return;
        }
        // Esborrar el token de sessió
        ConnexioServidor.logout();
        // Mostrar un missatge de confirmació (crec que fa més nosa que servei)
        // mostrarMissatge("Sessió tancada", "La sessió s'ha tancat correctament.");
        // Tancar la finestra actual
        tancarFinestraActual();
        // Tornar a la pantalla de login
        obrirNovaFinestra("/com/codexteam/codexlib/fxml/loginView.fxml", "Inici de sessió", "/com/codexteam/codexlib/images/enter.png");
    }

    /**
     * Tanca la finestra actual de l'aplicació.
     */
    private void tancarFinestraActual() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Mostra una finestra de confirmació per tancar la sessió.
     *
     * @return true si l’usuari accepta tancar la sessió, false si ho cancel·la.
     */
    private boolean confirmarTancarSessio() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Tancar la sessió");
        confirmAlert.setHeaderText("Segur que vols tancar la sessió?");

        // Obtenim la resposta de l'usuari
        Optional<ButtonType> result = confirmAlert.showAndWait();

        // Retorna true si l'usuari clica "OK", false si clica "Cancel"
        return result.isPresent() && result.get() == ButtonType.OK;
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

    //=====================================================
    //            OBTENIR LLISTAT D'AUTORS
    //=====================================================
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
                        // Si hay fechas tipo LocalDate, registrar módulo:
                        mapper.registerModule(new JavaTimeModule());

                        List<Autor> autors = mapper.readValue(
                                response,
                                new TypeReference<List<Autor>>() {}
                        );

                        // Añadir los autores a la tabla desde el hilo principal
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

    //=====================================================
    //           OBRIR FINESTRA DETALLS AUTORS
    //=====================================================
    private void obrirGestionarAutors(Autor autor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestionarAutorsView.fxml"));
            Parent root = loader.load();

            GestionarAutorsController controller = loader.getController();
            controller.setAutor(autor); // null si és nou

            Stage stage = new Stage();
            stage.setTitle(autor == null ? "Nou autor" : "Editar autor");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/author.png")));
            stage.showAndWait();

            carregarAutors(); // Refresca la taula
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //=====================================================
    //            OBTENIR LLISTAT DE RESERVES
    //=====================================================
    /**
     * Obté el llistat de les reserves del servidor mitjançant una petició HTTP
     * i les mostra a la taula de reserves.
     */
    private void carregarReserves() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/loans"))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());
                        mapper.findAndRegisterModules(); // Perque reconegui dates de tipus LocalDate
                        List<Reserva> reserves = mapper.readValue(
                                response,
                                new TypeReference<List<Reserva>>() {}
                        );
                        Platform.runLater(() -> taulaReserves.getItems().setAll(reserves));
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
    //            MOSTRAR MISSATGES INFORMATIUS
    //=====================================================
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

}