package com.codexteam.codexlib.controllers.objectdetailscontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador per a la finestra que mostra els resultats generats per la IA sobre un llibre seleccionat.
 * <p>
 * Aquesta finestra inclou camps per mostrar:
 * <ul>
 *     <li>El títol del llibre.</li>
 *     <li>El nom de l’autor.</li>
 *     <li>Una llista de llibres similars recomanats.</li>
 *     <li>Altres llibres recomanats del mateix autor.</li>
 *     <li>Un breu resum del perfil de l’autor.</li>
 * </ul>
 * La vista associada és {@code resultatIAView.fxml}.
 * </p>
 */
public class ResultatIAController {

    @FXML private TextField titolField;
    @FXML private TextField autorField;
    @FXML private TextArea similarsArea;
    @FXML private TextArea altresLlibresArea;
    @FXML private TextArea resumAutorArea;

    public void inicialitzarDades(String titol, String autor, String similars, String altres, String resum) {
        titolField.setText(titol);
        autorField.setText(autor);
        similarsArea.setText(similars);
        altresLlibresArea.setText(altres);
        resumAutorArea.setText(resum);
    }

    @FXML
    private void tancarFinestra() {
        Stage stage = (Stage) titolField.getScene().getWindow();
        stage.close();
    }
}
