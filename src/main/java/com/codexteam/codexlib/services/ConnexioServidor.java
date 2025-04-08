package com.codexteam.codexlib.services;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Classe encarregada de gestionar la connexió amb el servidor,
 * incloent l'autenticació mitjançant login i l'emmagatzematge del token JWT.
 */
public class ConnexioServidor {

    /** Token JWT obtingut després de fer login. */
    private static String tokenSessio = null;

    /** Nom de l'usuari que ha iniciat sessió. */
    private static String nomUsuariActual = null;

    /** Tipus d'usuari (1 = administrador, qualsevol altre = usuari normal). */
    private static int tipusUsuari = -1;

    /**
     * Realitza el procés de login amb el servidor.
     *
     * @param username Nom d'usuari.
     * @param password Contrasenya.
     * @return true si el login ha estat satisfactori, false en cas contrari.
     */
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

    /**
     * Retorna el token JWT obtingut després de l'autenticació.
     *
     * @return Token JWT de sessió.
     */
    public static String getTokenSessio() {
        return tokenSessio;
    }

    /**
     * Retorna el nom de l'usuari que ha iniciat sessió.
     *
     * @return Nom de l'usuari actual.
     */
    public static String getNomUsuariActual() {
        return nomUsuariActual;
    }

    /**
     * Retorna el tipus d'usuari (1 per a administradors).
     *
     * @return Codi del rol d'usuari.
     */
    public static int getTipusUsuari() {
        return tipusUsuari;
    }

    /**
     * Elimina les dades de sessió actual (token, usuari i rol).
     */
    public static void logout() {
        tokenSessio = null;
        nomUsuariActual = null;
        tipusUsuari = -1;
    }
}