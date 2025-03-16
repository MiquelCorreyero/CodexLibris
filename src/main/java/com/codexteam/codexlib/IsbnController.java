package com.codexteam.codexlib;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IsbnController {

    @FXML
    private TextField campISBN;  // Campo de texto donde el usuario introduce el ISBN

    @FXML
    private Button cercarISBNButton;  // Botón para iniciar la búsqueda

    @FXML
    private void initialize() {
        cercarISBNButton.setOnAction(event -> cercarPerIsbn());
    }

    @FXML
    private void cercarPerIsbn() {
        String isbn = campISBN.getText().trim();
        if (isbn.isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor, introduce un ISBN.");
            return;
        }

        // Open Library
        String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";

        // Google Books
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
                mostrarAlerta("ISBN no encontrado", "No se ha encontrado un libro con el ISBN " + isbn);
                return;
            }

            JSONObject bookInfo = jsonResponse.getJSONObject("ISBN:" + isbn);

            String titulo = bookInfo.optString("title", "Título desconocido");
            String autor = bookInfo.has("authors") ? bookInfo.getJSONArray("authors").getJSONObject(0).getString("name") : "Autor desconocido";
            String publicacion = bookInfo.optString("publish_date", "Fecha no disponible");
            String portada = bookInfo.has("cover") ? bookInfo.getJSONObject("cover").optString("medium", "Sin portada") : "Sin portada";

            // Mostrar los datos obtenidos
            mostrarAlerta("Libro Encontrado", "Título: " + titulo + "\nAutor: " + autor + "\nPublicado en: " + publicacion + "\nPortada: " + portada);

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo conectar con Open Library.");
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

