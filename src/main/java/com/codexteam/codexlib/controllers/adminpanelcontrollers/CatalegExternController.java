package com.codexteam.codexlib.controllers.adminpanelcontrollers;

import com.codexteam.codexlib.controllers.objectdetailscontrollers.GestionarLlibresController;
import com.codexteam.codexlib.controllers.objectdetailscontrollers.IntroduirApiKeyAIController;
import com.codexteam.codexlib.controllers.objectdetailscontrollers.ResultatIAController;
import com.codexteam.codexlib.models.LlibreExtern;
import com.codexteam.codexlib.services.ClientFactory;
import com.codexteam.codexlib.services.ConnexioServidor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.codexteam.codexlib.models.RespostaExtern;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


/**
 * Controlador per gestionar la cerca de llibres externs mitjançant l'API d'Open Library.
 * Mostra els resultats obtinguts a través d’un endpoint propi que fa servir l’API externa.
 */
public class CatalegExternController {

    @FXML private TextField campCercaExtern;
    @FXML private Button botoCercaExtern;
    @FXML private Button netejaExternButton;
    @FXML private Button importarLlibreButton;
    @FXML private Button importarIAButton;
    @FXML private ImageView gifCargando;

    @FXML private TableView<LlibreExtern> taulaExtern;
    @FXML private TableColumn<LlibreExtern, String> colTitolExtern;
    @FXML private TableColumn<LlibreExtern, String> colAutorExtern;
    @FXML private TableColumn<LlibreExtern, String> colIsbnExtern;
    @FXML private TableColumn<LlibreExtern, String> colAnyExtern;

    @FXML
    public void initialize() {

        // Configura columnes
        colTitolExtern.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        colAutorExtern.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAuthor()));
        colIsbnExtern.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIsbn()));
        colAnyExtern.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getYear()))
        );
        // Cerca al prémer enter
        campCercaExtern.setOnAction(e -> ferCercaExtern());

        // Botó de cerca
        botoCercaExtern.setOnAction(e -> ferCercaExtern());

        // GIF amb animació de càrrega
        gifCargando.setImage(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/spinner.gif")));
        gifCargando.setVisible(false);

        // Botó de neteja
        netejaExternButton.setOnAction(e -> {
            campCercaExtern.clear();
            taulaExtern.getItems().clear();
        });

        // Desactiva neteja si no hi ha text
        campCercaExtern.textProperty().addListener((obs, oldVal, newVal) -> {
            netejaExternButton.setDisable(newVal.trim().isEmpty());
        });

        // Botó importar
        importarLlibreButton.setOnAction(e -> {
            LlibreExtern seleccionat = taulaExtern.getSelectionModel().getSelectedItem();
            if (seleccionat == null) {
                mostrarMissatge("Error", "Has de seleccionar un llibre per importar.");
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/gestionarLlibresView.fxml"));
                Parent root = loader.load();

                GestionarLlibresController controller = loader.getController();
                controller.importarLlibreExtern(seleccionat);

                Stage stage = new Stage();
                stage.setTitle("Importar llibre");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

            } catch (IOException ex) {
                ex.printStackTrace();
                mostrarMissatge("Error", "No s'ha pogut obrir la finestra d'importació.");
            }
        });

        // Botó importar amb IA
        importarIAButton.setOnAction(e -> {
            System.out.println("Botó 'Importar amb IA' clicat");
            mostrarFinestraApiKey();
        });

        // Oculta els botons si l'usuari no és admin
        if (ConnexioServidor.getTipusUsuari() != 1) {
            importarLlibreButton.setVisible(false);
        }

    }

    /**
     * Realitza una cerca de llibres externs a través de l'API pròpia que consulta Open Library.
     * Mostra els resultats en la taula si la resposta és correcta o un missatge d’error si falla.
     */
    private void ferCercaExtern() {
        String query = campCercaExtern.getText().trim();
        if (query.isEmpty()) {
            mostrarMissatge("Cerca buida", "Escriu una paraula clau per fer la cerca.");
            return;
        }

        Platform.runLater(() -> gifCargando.setVisible(true));

        String url = "https://localhost/external-books/search?q=" + query.replace(" ", "%20");

        HttpClient client = ClientFactory.getClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {

                    try {
                        int status = response.statusCode();

                        Platform.runLater(() -> gifCargando.setVisible(false));

                        if (status < 200 || status >= 300) {
                            System.err.println("Error " + status + ": El servidor ha retornat un error durant la cerca.");
                            System.err.println("Cos de la resposta:\n" + response.body());
                            return;
                        }

                        ObjectMapper mapper = new ObjectMapper();
                        RespostaExtern resultat = mapper.readValue(response.body(), RespostaExtern.class);

                        Platform.runLater(() -> {
                            taulaExtern.getItems().clear();
                            taulaExtern.getItems().addAll(resultat.getResults());
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> mostrarMissatge("Error", "No s'han pogut interpretar les dades."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarMissatge("Error", "Error en la connexió amb el servidor."));
                    return null;
                });
    }

    /**
     * Obre una finestra modal (FXML) perquè l’usuari introdueixi la seva API Key d’OpenAI
     * i recupera el valor introduït un cop la finestra es tanca.
     */
    private void mostrarFinestraApiKey() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/introduirApiKeyAIView.fxml"));
            Parent root = loader.load();

            IntroduirApiKeyAIController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Introduir API Key d'OpenAI");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            String apiKey = controller.getApiKey();

            if (apiKey != null && !apiKey.isBlank()) {
                LlibreExtern seleccionat = taulaExtern.getSelectionModel().getSelectedItem();
                if (seleccionat == null) {
                    mostrarMissatge("Error", "Has de seleccionar un llibre abans.");
                    return;
                }

                generarDadesIA(seleccionat, apiKey); // << Aquí llamamos a OpenAI
            } else {
                System.out.println("No s’ha introduït cap clau d’API.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            mostrarMissatge("Error", "No s'ha pogut obrir la finestra per introduir la clau API.");
        }
    }

    /**
     * Mostra una finestra modal amb els resultats generats per la IA sobre un llibre seleccionat.
     * <p>
     * El text rebut de la IA es divideix en tres seccions:
     * <ul>
     *     <li>Llibres similars recomanats.</li>
     *     <li>Altres llibres de l’autor.</li>
     *     <li>Un breu resum del perfil de l’autor.</li>
     * </ul>
     * Aquestes dades s’assignen als camps corresponents en el controlador de la vista {@code resultatIAView.fxml}.
     * </p>
     *
     * @param llibre      El llibre del qual s'han generat dades addicionals amb IA.
     * @param respostaIA  El text complet retornat per OpenAI amb les recomanacions i resum de l’autor.
     */
    private void mostrarFinestraResultatIA(LlibreExtern llibre, String respostaIA) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/gestio-items/resultatIAView.fxml"));
            Parent root = loader.load();

            ResultatIAController controller = loader.getController();

            // Procesamos el texto en secciones (asumiendo separación por doble salto de línea)
            String similars = "", altres = "", resum = "";
            String[] seccions = respostaIA.split("\n\n");
            if (seccions.length >= 3) {
                similars = seccions[0].trim();
                altres = seccions[1].trim();
                resum = seccions[2].trim();
            } else {
                similars = respostaIA;
            }

            controller.inicialitzarDades(
                    llibre.getTitle(),
                    llibre.getAuthor(),
                    similars,
                    altres,
                    resum
            );

            Stage stage = new Stage();
            stage.setTitle("Resultat IA per \"" + llibre.getTitle() + "\"");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarMissatge("Error", "No s'ha pogut mostrar el resultat IA.");
        }
    }

    /**
     * Genera una petició a l'API de OpenAI per obtenir informació addicional sobre un llibre seleccionat.
     * <p>
     * Envia un prompt amb el títol i l'autor del llibre, i sol·licita:
     * <ul>
     *     <li>Una llista de llibres similars recomanats.</li>
     *     <li>Altres llibres recomanats del mateix autor.</li>
     *     <li>Un breu resum del perfil de l’autor.</li>
     * </ul>
     * La resposta es mostra en una finestra modal amb els camps corresponents.
     * </p>
     *
     * @param llibre Llibre seleccionat des del catàleg extern.
     * @param apiKey Clau d'accés personal de l'usuari per a utilitzar l'API de OpenAI.
     */
    private void generarDadesIA(LlibreExtern llibre, String apiKey) {
        String prompt = String.format("""
        Dona'm la següent informació sobre aquest llibre:
        Títol: %s
        Autor: %s

        Vull que em donis:
        1. Una llista de llibres similars recomanats (5).
        2. Una llista d'altres llibres recomanats del mateix autor (3).
        3. Un breu resum del perfil del seu autor (max. 300 caràcters).
        El format ha de ser clar i fàcil de parsejar.
    """, llibre.getTitle(), llibre.getAuthor());

        try {
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode root = mapper.createObjectNode();
            root.put("model", "gpt-4");

            ArrayNode messages = mapper.createArrayNode();

            ObjectNode systemMsg = mapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", "Ets un expert en literatura.");
            messages.add(systemMsg);

            ObjectNode userMsg = mapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);

            root.set("messages", messages);

            String jsonRequest = mapper.writeValueAsString(root);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                JsonNode json = mapper.readTree(response.body());
                                String resposta = json.get("choices").get(0).get("message").get("content").asText();

                                Platform.runLater(() -> mostrarFinestraResultatIA(llibre, resposta));
                            } catch (Exception e) {
                                e.printStackTrace();
                                Platform.runLater(() -> mostrarMissatge("Error", "No s'han pogut analitzar les dades de la IA."));
                            }
                        } else {
                            Platform.runLater(() -> mostrarMissatge(
                                    "Error a OpenAI",
                                    "Codi: " + response.statusCode() + "\nResposta: " + response.body()
                            ));
                        }
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        Platform.runLater(() -> mostrarMissatge("Error", "Error de connexió amb OpenAI."));
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarMissatge("Error", "No s'ha pogut preparar la petició per a OpenAI.");
        }
    }

    /**
     * Mostra una alerta d’informació a l’usuari.
     */
    private void mostrarMissatge(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }

}

