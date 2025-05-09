package com.codexteam.codexlib.controllers.objectdetailscontrollers;

import com.codexteam.codexlib.models.LlibreExtern;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controlador de la finestra que mostra les dades
 * del llibre seleccionat i els camps generats per IA.
 */
public class ImportarIAController {

    @FXML private TextField titolField;
    @FXML private TextField autorField;
    @FXML private TextField anyField;
    @FXML private TextArea resumLlibreArea;
    @FXML private TextArea bioAutorArea;
    @FXML private TextArea obresSimilarsArea;
    @FXML private TextArea altresLlibresAutorArea;
    @FXML private Button guardarButton;
    @FXML private Button cancelarButton;

    private LlibreExtern llibreExtern;

    @FXML
    public void initialize() {
        cancelarButton.setOnAction(e -> tancarFinestra());
        guardarButton.setOnAction(e -> guardarDades());
    }

    public void setDadesLlibre(LlibreExtern llibre) {
        this.llibreExtern = llibre;
        titolField.setText(llibre.getTitle());
        autorField.setText(llibre.getAuthor());
        anyField.setText(String.valueOf(llibre.getYear()));
    }

    private void tancarFinestra() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
    }

    private void guardarDades() {
        // En aquesta versió de prova només tanquem la finestra. Aquí s'hauria de guardar a la BD
        System.out.println("Guardar dades:");
        System.out.println("Resum: " + resumLlibreArea.getText());
        System.out.println("Biografia: " + bioAutorArea.getText());
        System.out.println("Similars: " + obresSimilarsArea.getText());
        System.out.println("Altres obres: " + altresLlibresAutorArea.getText());
        tancarFinestra();
    }
}
