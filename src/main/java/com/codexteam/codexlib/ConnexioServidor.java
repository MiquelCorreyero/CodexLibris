package com.codexteam.codexlib;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ConnexioServidor {

    // Token JWT del login
    private static String tokenSessio = null;
    // Nom de l'usuari que ha fet el login
    private static String nomUsuariActual = null;
    // ID del rol (1 = admin, qualsevol altre valor = usuari normal)
    private static int tipusUsuari = -1;

    public static boolean login(String username, String password) {
        try {
            // URL endpoint del login
            URL url = new URL("http://localhost:8080/auth/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Crear JSON amb les dades de connexió
            String jsonInput = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";

            // Enviem la petició al server
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Obtenim resposta del server
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (Scanner scanner = new Scanner(conn.getInputStream(), "utf-8")) {
                    String jsonResponse = scanner.useDelimiter("\\A").next();

                    // Convertim el JSON en un objecte
                    JSONObject jsonObject = new JSONObject(jsonResponse);

                    // Obtenim el token d'inici de sessió, el nom d'usuari i el rol
                    tokenSessio = jsonObject.getString("token");
                    nomUsuariActual = jsonObject.getString("username");
                    tipusUsuari = jsonObject.getInt("roleId");

                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Obtenir el token JWT
    public static String getTokenSessio() {
        return tokenSessio;
    }

    // Obtenir el nom de l'usuari
    public static String getNomUsuariActual() {
        return nomUsuariActual;
    }

    // Obtenir el tipus d'usuari
    public static int getTipusUsuari() {
        return tipusUsuari;
    }

    // Tancar la sessió
    public static void logout() {
        tokenSessio = null;
        nomUsuariActual = null;
        tipusUsuari = -1;
    }
}