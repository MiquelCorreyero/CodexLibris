package com.codexteam.codexlib;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Controlador encarregat de cercar informació d'un llibre a partir del seu ISBN mitjançant l'API de Open Library.
 * Mostra els resultats obtinguts en una finestra amb els detalls del llibre.
 */
public class IsbnController {

    @FXML
    private TextField campISBN;

    @FXML
    private Button cercarISBNButton;

    @FXML
    private void initialize() {
        cercarISBNButton.setOnAction(event -> cercarPerIsbn());
    }

    /**
     * Realitza la consulta a Open Library amb l'ISBN introduït i, si troba un resultat,
     * obre una nova finestra amb els detalls del llibre.
     */
    @FXML
    private void cercarPerIsbn() {
        String isbn = campISBN.getText().trim();
        if (isbn.isEmpty()) {
            mostrarAlerta("Error", "Si us plau, introdueix un ISBN.");
            return;
        }

        // Open Library
        String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";

        // Google Books (no retorna gaires dades)
        // String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());

            if (!jsonResponse.has("ISBN:" + isbn)) {
                mostrarAlerta("Error", "No sha trobat cap llibre amb l'ISBN " + isbn);
                return;
            }

            JSONObject bookInfo = jsonResponse.getJSONObject("ISBN:" + isbn);

            String titol = bookInfo.optString("title", "Títol desconegut");
            String autor = bookInfo.has("authors") ? bookInfo.getJSONArray("authors").getJSONObject(0).getString("name") : "Autor desconegut";
            String publicacio = bookInfo.optString("publish_date", "Data no disponible");
            String portada = bookInfo.has("cover") ? bookInfo.getJSONObject("cover").optString("medium", "Sense portada") : "Sense portada";

            obrirFinestraDetalls(titol, autor, publicacio, isbn, portada);

        } catch (Exception e) {
            mostrarAlerta("Error", "No s'ha pogut connectar amb Open Library.");
            e.printStackTrace();
        }
    }

    /**
     * Obre una finestra modal mostrant els detalls del llibre obtingut de la cerca per ISBN.
     *
     * @param titol        Títol del llibre.
     * @param autor        Autor del llibre.
     * @param publicacio   Data de publicació.
     * @param isbn         Codi ISBN.
     * @param portada      URL de la portada del llibre.
     */
    private void obrirFinestraDetalls(String titol, String autor, String publicacio, String isbn, String portada) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codexteam/codexlib/fxml/detallsLlibreView.fxml"));
            Parent root = loader.load();

            DetallsLlibreController controller = loader.getController();
            controller.mostrarDetallsLlibre(titol, autor, publicacio, isbn, portada);

            Stage stage = new Stage();
            stage.setTitle("Detalls del Llibre");
            stage.setScene(new Scene(root));
            // Icona de la finestra
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codexteam/codexlib/images/details.png")));
            stage.show();

        } catch (IOException e) {
            mostrarAlerta("Error", "No s'ha pogut obrir la finestra amb els detalls del llibre.");
            e.printStackTrace();
        }
    }

    /**
     * Mostra una finestra d'alerta amb un missatge informatiu.
     *
     * @param titol    Títol de l'alerta.
     * @param missatge Missatge a mostrar.
     */
    private void mostrarAlerta(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }

    public void entradaLlibreManual(){

    }

}

