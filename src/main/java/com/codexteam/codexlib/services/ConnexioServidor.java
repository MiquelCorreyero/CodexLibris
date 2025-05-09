package com.codexteam.codexlib.services;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;


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
     * Realitza el procés de login amb el servidor mitjançant una connexió HTTPS.
     * <p>
     * Aquest mètode envia una petició POST amb les credencials de l'usuari en format JSON
     * a l'endpoint {@code https://localhost/auth/login}, i espera una resposta amb un
     * token JWT, el nom de l'usuari i el rol corresponent.
     * </p>
     * <p>
     * Per permetre la connexió amb un servidor local amb certificat autofirmat,
     * durant la fase de desenvolupament:
     * <ul>
     *     <li>Accepta tots els certificats SSL sense validació.</li>
     *     <li>Permet qualsevol nom de host (ex. localhost) mitjançant un {@code HostnameVerifier} personalitzat.</li>
     * </ul>
     * <strong>Aquesta configuració no és segura i només s'ha d'utilitzar en entorns de desenvolupament.</strong>
     * </p>
     *
     * @param username El nom d'usuari a autenticar.
     * @param password La contrasenya corresponent.
     * @return {@code true} si el login ha estat satisfactori (codi 200 i resposta vàlida), {@code false} en cas contrari.
     */
    public static boolean login(String username, String password) {
        try {
            // Acceptar tots els certificats SSL (només en fase desenvolupament)
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Acceptar qualsevol nom de host (per ex, localhost)
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            // URL endpoint del login
            URL url = new URL("https://localhost/auth/login");
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