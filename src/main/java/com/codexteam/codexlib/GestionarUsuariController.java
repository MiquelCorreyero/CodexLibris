package com.codexteam.codexlib;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GestionarUsuariController {

    @FXML private Label titolLabel;
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> rolComboBox;
    @FXML private Button eliminarUsuariButton;
    @FXML private Button guardarUsuariButton;

    private Usuari usuariActual;

    @FXML
    public void initialize() {
        rolComboBox.getItems().addAll("admin", "user"); // Ajusta según los roles válidos
        eliminarUsuariButton.setOnAction(e -> eliminarUsuari());
        guardarUsuariButton.setOnAction(e -> guardarUsuari());
    }

    public void setUsuari(Usuari usuari) {
        this.usuariActual = usuari;

        if (usuari != null) {
            titolLabel.setText("Editar o eliminar l'usuari");
            usernameField.setText(usuari.getUsername());
            firstNameField.setText(usuari.getFirstName());
            lastNameField.setText(usuari.getLastName());
            emailField.setText(usuari.getEmail());
            passwordField.setText(usuari.getPassword());
            rolComboBox.setValue(usuari.getRole().getName());
            eliminarUsuariButton.setDisable(false);
        } else {
            titolLabel.setText("Nou usuari");
            firstNameField.clear();
            lastNameField.clear();
            usernameField.clear();
            emailField.clear();
            passwordField.clear();
            rolComboBox.setValue(null);
            eliminarUsuariButton.setDisable(true);
        }
    }

    private void guardarUsuari() {
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String rol = rolComboBox.getValue(); // admin o user

        // Validación
        if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty() || password.isEmpty() || rol == null || rol.isEmpty()) {
            mostrarAlerta("Error", "Tots els camps són obligatoris.");
            return;
        }

        // ID de rol (ajusta según lo que espere el backend)
        int roleId = rol.equalsIgnoreCase("admin") ? 1 : 2;

        try {
            ObjectMapper mapper = new ObjectMapper();
            var dades = new java.util.HashMap<String, Object>();
            dades.put("userName", username); // <- importante usar "userName" con N mayúscula
            dades.put("firstName", firstName);
            dades.put("lastName", lastName);
            dades.put("email", email);
            dades.put("password", password);
            dades.put("roleId", roleId);

            String json = mapper.writeValueAsString(dades);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request;

            if (usuariActual == null) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/users"))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            } else {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/users/" + usuariActual.getId()))
                        .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            }

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            Platform.runLater(() -> {
                                mostrarAlerta("Èxit", "Usuari desat correctament.");
                                tancarFinestra();
                            });
                        } else {
                            Platform.runLater(() -> mostrarAlerta("Error", "Error al desar usuari. Codi: " + response.statusCode() + "\nResposta: " + response.body()));
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "S'ha produït un error inesperat.");
        }
    }

    private void eliminarUsuari() {
        if (usuariActual == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar usuari");
        confirm.setHeaderText("Estàs segur que vols eliminar aquest usuari?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/users/" + usuariActual.getId()))
                    .header("Authorization", "Bearer " + ConnexioServidor.getTokenSessio())
                    .DELETE()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 204) {
                            Platform.runLater(() -> {
                                mostrarAlerta("Èxit", "Usuari eliminat correctament.");
                                tancarFinestra();
                            });
                        } else {
                            Platform.runLater(() -> mostrarAlerta("Error", "No s'ha pogut eliminar. Codi: " + response.statusCode()));
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error inesperat en eliminar l'usuari.");
        }
    }

    private void tancarFinestra() {
        Stage stage = (Stage) guardarUsuariButton.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titol, String missatge) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }
}

