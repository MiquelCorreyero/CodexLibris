package com.codexteam.codexlib.controllers.objectdetailscontrollers;

import com.codexteam.codexlib.models.Llibre;
import com.codexteam.codexlib.models.Reserva;
import com.codexteam.codexlib.models.Usuari;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Controlador encarregat de gestionar la finestra modal per a inserir, editar i eliminar reserves.
 *
 * També actualitza l’estat de disponibilitat del llibre associat en funció de la reserva creada o eliminada.
 * Es fa ús de ComboBoxes per a la selecció dinàmica d’usuaris i llibres disponibles.
 *
 * Aquesta classe s’associa al fitxer FXML <strong>gestionarReservesView.fxml</strong>.
 *
 * @author Miquel Correyero
 */
public class GestionarReservesController {

    @FXML private Label titolLabel;
    @FXML private TextField nomUsuariField;
    @FXML private TextField cognomsUsuariField;
    @FXML private TextField emailUsuariField;
    @FXML private TextField llibreField;
    @FXML private DatePicker dataReservaPicker;
    @FXML private DatePicker dataRetornPicker;
    @FXML private Button guardarReservaButton;
    @FXML private Button eliminarReservaButton;

    @FXML private ComboBox<Usuari> comboUsuaris;
    @FXML private ComboBox<Llibre> comboLlibres;

    private Reserva reservaActual;

    @FXML
    public void initialize() {
        guardarReservaButton.setOnAction(e -> guardarReserva());
        eliminarReservaButton.setOnAction(e -> eliminarReserva());

        comboUsuaris.setConverter(new StringConverter<Usuari>() {
            @Override public String toString(Usuari u) { return u != null ? u.getFirstName() + " " + u.getLastName() : ""; }
            @Override public Usuari fromString(String string) { return null; }
        });

        comboLlibres.setConverter(new StringConverter<Llibre>() {
            @Override public String toString(Llibre l) { return l != null ? l.getTitle() : ""; }
            @Override public Llibre fromString(String string) { return null; }
        });

        carregarLlibres();
        carregarUsuaris();
    }

    /**
     * Assigna la reserva a editar o prepara la vista per crear-ne una de nova.
     */
    public void setReserva(Reserva reserva) {
        this.reservaActual = reserva;

        if (reserva != null) {
            titolLabel.setText("Editar o eliminar reserva");

            dataReservaPicker.setValue(reserva.getLoan_date());
            dataRetornPicker.setValue(reserva.getReturn_date());

            // Seleccionar usuari
            for (Usuari usuari : comboUsuaris.getItems()) {
                if (usuari.getId() == reserva.getUser_id()) {
                    comboUsuaris.setValue(usuari);
                    break;
                }
            }

            // Seleccionar llibre
            for (Llibre llibre : comboLlibres.getItems()) {
                if (llibre.getId() == reserva.getBook_id()) {
                    comboLlibres.setValue(llibre);
                    break;
                }
            }

            eliminarReservaButton.setDisable(false);
        } else {
            titolLabel.setText("Nova reserva");

            dataReservaPicker.setValue(null);
            dataRetornPicker.setValue(null);
            comboUsuaris.setValue(null);
            comboLlibres.setValue(null);

            eliminarReservaButton.setDisable(true);
        }
    }


    /**
     * Guarda la reserva mitjançant una petició POST o PUT.
     */
    private void guardarReserva() {
        // Validar campos obligatorios
        if (comboUsuaris.getValue() == null || comboLlibres.getValue() == null ||
                dataReservaPicker.getValue() == null || dataRetornPicker.getValue() == null) {
            mostrarMissatge("Error", "Tots els camps són obligatoris.");
            return;
        }

        int userId = comboUsuaris.getValue().getId();
        int bookId = comboLlibres.getValue().getId();
        int statusId = 1;

        String loanDate = dataReservaPicker.getValue().toString();
        String dueDate = dataRetornPicker.getValue().toString();
        String returnDate = dataRetornPicker.getValue().toString();

        String json = String.format("""
        {
          "loan_date": "%s",
          "due_date": "%s",
          "return_date": "%s",
          "userId": %d,
          "bookId": %d,
          "statusId": %d
        }
    """, loanDate, dueDate, returnDate, userId, bookId, statusId);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request;

            if (reservaActual == null) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/loans"))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            } else {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/loans/" + reservaActual.getId()))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            }

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                mostrarMissatge("Èxit", "Reserva desada correctament.");

                                marcarLlibreComNoDisponible(comboLlibres.getValue());

                                tancarFinestra();
                            } else {
                                mostrarMissatge("Error", "No s'ha pogut desar. Codi: " + response.statusCode() + "\nResposta: " + response.body());
                            }
                        });
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarMissatge("Error", "S'ha produït un error inesperat.");
        }
    }

    /**
     * Elimina la reserva actual.
     */
    private void eliminarReserva() {
        if (reservaActual == null) {
            mostrarMissatge("Error", "No hi ha cap reserva seleccionada.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar reserva");
        confirm.setHeaderText("Estàs segur que vols eliminar aquesta reserva?");
        confirm.setContentText("Aquesta acció no es pot desfer.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/loans/" + reservaActual.getId()))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .DELETE()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 204) {
                            mostrarMissatge("Èxit", "Reserva eliminada correctament.");

                            // Recuperar el llibre seleccionat
                            marcarLlibreComDisponible(comboLlibres.getValue());

                            tancarFinestra();
                        } else {
                            mostrarMissatge("Error", "No s'ha pogut eliminar la reserva. Codi: " + response.statusCode());
                        }
                    });
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarMissatge("Error", "Error inesperat en eliminar la reserva."));
                    return null;
                });
    }

    /**
     * Carrega la llista de llibres disponibles des del servidor
     * i omple el ComboBox amb aquests llibres. Si s'està editant
     * una reserva, selecciona el llibre corresponent.
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
                        List<Llibre> llibres = mapper.readValue(response, new TypeReference<List<Llibre>>() {});
                        Platform.runLater(() -> {
                            comboLlibres.getItems().setAll(llibres);
                            if (reservaActual != null) {
                                for (Llibre llibre : llibres) {
                                    if (llibre.getId() == reservaActual.getBook_id()) {
                                        comboLlibres.setValue(llibre);
                                        break;
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Carrega la llista d'usuaris disponibles des del servidor
     * i omple el ComboBox amb aquests usuaris. Si s'està editant
     * una reserva, selecciona l'usuari corresponent.
     */
    private void carregarUsuaris() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List<Usuari> usuaris = mapper.readValue(response, new TypeReference<List<Usuari>>() {});
                        Platform.runLater(() -> {
                            comboUsuaris.getItems().setAll(usuaris);
                            if (reservaActual != null) {
                                for (Usuari usuari : usuaris) {
                                    if (usuari.getId() == reservaActual.getUser_id()) {
                                        comboUsuaris.setValue(usuari);
                                        break;
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Marca el llibre com a no disponible a la base de dades
     * mitjançant una petició PUT. Es crida després de crear una reserva.
     *
     * @param llibre Llibre que s'ha de marcar com no disponible.
     */
    private void marcarLlibreComNoDisponible(Llibre llibre) {
        try {
            if (llibre.getAuthor() == null || llibre.getGenre() == null) {
                System.err.println("ERROR: El llibre no té autor o gènere assignat. No es pot marcar com no disponible.");
                return;
            }

            String dataPublicacio = llibre.getPublished_date().split("T")[0];

            String json = String.format("""
        {
          "title": "%s",
          "authorId": %d,
          "isbn": "%s",
          "publishedDate": "%s",
          "genreId": %d,
          "available": false
        }
        """,
                    llibre.getTitle(),
                    llibre.getAuthor().getId(),
                    llibre.getIsbn(),
                    dataPublicacio,
                    llibre.getGenre().getId()
            );

            System.out.println("JSON per PUT del llibre:\n" + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/books/" + llibre.getId()))
                    .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            System.err.println("No s'ha pogut marcar el llibre com no disponible. Codi: " + response.statusCode());
                            System.err.println("Resposta: " + response.body());
                        } else {
                            System.out.println("Llibre marcat com no disponible.");
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Marca el llibre com a disponible a la base de dades
     * mitjançant una petició PUT. Es crida després d’eliminar una reserva.
     *
     * @param llibre Llibre que s'ha de marcar com disponible.
     */
    private void marcarLlibreComDisponible(Llibre llibre) {
        try {
            if (llibre.getAuthor() == null || llibre.getGenre() == null) {
                System.err.println("ERROR: El llibre no té autor o gènere assignat.");
                return;
            }

            String dataPublicacio = llibre.getPublished_date().split("T")[0];

            String json = String.format("""
        {
          "title": "%s",
          "authorId": %d,
          "isbn": "%s",
          "publishedDate": "%s",
          "genreId": %d,
          "available": true
        }
        """,
                    llibre.getTitle(),
                    llibre.getAuthor().getId(),
                    llibre.getIsbn(),
                    dataPublicacio,
                    llibre.getGenre().getId()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/books/" + llibre.getId()))
                    .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            System.err.println("No s'ha pogut marcar el llibre com disponible. Codi: " + response.statusCode());
                            System.err.println("Resposta: " + response.body());
                        } else {
                            System.out.println("Llibre marcat com disponible.");
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra un diàleg informatiu a l’usuari.
     */
    private void mostrarMissatge(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }

    /**
     * Tanca la finestra actual.
     */
    private void tancarFinestra() {
        Stage stage = (Stage) guardarReservaButton.getScene().getWindow();
        stage.close();
    }
}