package com.codexteam.codexlib;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class DetallsLlibreController {

    @FXML
    private ImageView portadaImageView;
    @FXML
    private TextField titolTextField;
    @FXML
    private TextField autorTextField;
    @FXML
    private TextField dataPublicacioTextField;
    @FXML
    private TextField isbnTextField;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Mostrem els camps amb la informació obtinguda de la API
    public void mostrarDetallsLlibre(String titol, String autor, String dataPublicacio, String isbn, String portadaUrl) {
        titolTextField.setText(titol);
        autorTextField.setText(autor);
        dataPublicacioTextField.setText(dataPublicacio);
        isbnTextField.setText(isbn);

        // Mostrem la portada si existeix ('Sense portada' li passem des de IsbnController)
        if (portadaUrl != null && !portadaUrl.equals("Sense portada")) {
            portadaImageView.setImage(new Image(portadaUrl));
        }
    }
}
