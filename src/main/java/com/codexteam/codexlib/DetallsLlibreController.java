package com.codexteam.codexlib;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Controlador de la finestra de detalls d'un llibre.
 * Aquesta finestra mostra informació detallada d'un llibre obtinguda des de la API de Open Library.
 */
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

    /**
     * Mostra la informació detallada d’un llibre en els camps corresponents de la finestra.
     *
     * @param titol         Títol del llibre.
     * @param autor         Nom de l’autor.
     * @param dataPublicacio Data de publicació.
     * @param isbn          Codi ISBN.
     * @param portadaUrl    URL de la imatge de la portada (pot ser null o "Sense portada").
     */
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
