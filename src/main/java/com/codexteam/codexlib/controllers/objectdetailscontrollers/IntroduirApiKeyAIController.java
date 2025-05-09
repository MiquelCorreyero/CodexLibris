package com.codexteam.codexlib.controllers.objectdetailscontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador per la finestra que demana la API Key d'OpenAI.
 */
public class IntroduirApiKeyAIController {

    @FXML private TextField openAiText;
    @FXML private Button botoAcceptarAI;
    @FXML private Button botoCancelarAI;

    private String apiKey = null;

    /**
     * Inicialitza els botons de la finestra.
     */
    @FXML
    public void initialize() {
        botoAcceptarAI.setOnAction(e -> {
            apiKey = openAiText.getText().trim();
            tancarFinestra();
        });

        botoCancelarAI.setOnAction(e -> {
            apiKey = null; // explícitament anul·lem la key
            tancarFinestra();
        });
    }

    /**
     * Retorna la API Key introduïda per l’usuari.
     * @return La clau API, o null si es va cancel·lar.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Tanca la finestra actual.
     */
    private void tancarFinestra() {
        Stage stage = (Stage) botoAcceptarAI.getScene().getWindow();
        stage.close();
    }
}
