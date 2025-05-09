import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves d'integració per validar que la comunicació amb el backend REST de CodexLibris
 * funciona correctament a través de peticions HTTP autenticades.
 *
 * Aquesta classe comprova que:
 * <ul>
 *     <li>El sistema permet autenticar-se mitjançant credencials vàlides, obtenint un token JWT.</li>
 *     <li>Els principals endpoints (com ara /books, /genres, /reserves i /events) responen correctament.</li>
 *     <li>Es poden fer peticions GET per consultar recursos.</li>
 *     <li>Es poden crear nous esdeveniments mitjançant POST.</li>
 *     <li>Es poden eliminar esdeveniments amb DELETE.</li>
 * </ul>
 *
 * @author Miquel Correyero
 */

public class ApiRestTest {

    /**
     * Realitza una petició POST al servidor per obtenir un token JWT
     * a partir de les credencials de l'usuari "admin".
     * @return El token JWT obtingut com a String
     * @throws IOException si falla la connexió
     */
    @Test
    void testLoginAndReceiveToken() throws IOException {
        URL url = new URL("https://localhost/auth/login");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String credentials = "{\"username\": \"admin\", \"password\": \"admin\"}";
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = credentials.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        assertEquals(200, con.getResponseCode());

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder resposta = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            resposta.append(line);
        }
        in.close();

        String body = resposta.toString();
        int start = body.indexOf("token\":\"") + 8;
        int end = body.indexOf("\"", start);
        String token = body.substring(start, end);

        assertNotNull(token);
        assertTrue(token.length() > 20);
    }

    /**
     * Comprova que es pot fer logout correctament i que el servidor respon amb un missatge adequat.
     */
    @Test
    void testLogoutEndpoint() throws IOException {
        String token = loginAndGetToken();

        URL url = new URL("https://localhost/auth/logout");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Content-Type", "application/json");

        int responseCode = con.getResponseCode();
        assertTrue(responseCode == 200 || responseCode == 204, "El servidor hauria de respondre correctament al logout.");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder resposta = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            resposta.append(line);
        }
        in.close();

        String body = resposta.toString();
        assertTrue(body.contains("Has tancat la sessió correctament."), "El missatge de logout no és el que s’esperava.");
    }

    /**
     * Comprova que es pot accedir al recurs /books amb una petició GET
     * i que la resposta conté dades sobre llibres.
     * @throws IOException si hi ha problemes de connexió
     */
    @Test
    void testAccessBooksEndpoint() throws IOException {
        String token = loginAndGetToken();

        URL url = new URL("https://localhost/books");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Content-Type", "application/json");

        int code = con.getResponseCode();
        assertEquals(200, code);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        assertTrue(response.toString().contains("title"));
    }

    /**
     * Comprova que es pot accedir al recurs /authors amb una petició GET
     * i que la resposta conté informació sobre autors.
     * @throws IOException si hi ha problemes de connexió
     */
    @Test
    void testGetAuthors() throws IOException {
        String token = loginAndGetToken();

        URL url = new URL("https://localhost/authors");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Content-Type", "application/json");

        int status = con.getResponseCode();
        assertEquals(200, status);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            content.append(line);
        }
        in.close();

        assertTrue(content.toString().contains("name"));
    }

    /**
     * Comprova que es pot accedir a l'endpoint GET /genres i obtenir una resposta vàlida.
     */
    @Test
    void testAccessGenresEndpoint() throws IOException {
        String token = loginAndGetToken();

        URL url = new URL("https://localhost/genres");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Content-Type", "application/json");

        int code = con.getResponseCode();
        assertEquals(200, code);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        String responseBody = response.toString();
        assertTrue(responseBody.contains("name"), "La resposta hauria de contenir el camp 'name' del gènere.");
    }

    /**
     * Comprova que es pot accedir a l'endpoint GET /reserves i obtenir una resposta vàlida.
     */
    @Test
    void testAccessReservesEndpoint() throws IOException {
        String token = loginAndGetToken();

        URL url = new URL("https://localhost/loans");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Content-Type", "application/json");

        int code = con.getResponseCode();
        assertEquals(200, code);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        String responseBody = response.toString();
        assertTrue(responseBody.contains("book_title") || responseBody.contains("user_name"),
                "La resposta hauria de contenir informació d'una reserva.");
    }

    /**
     * Comprova que es pot accedir a l'endpoint GET /events i obtenir una resposta vàlida.
     */
    @Test
    void testAccessEventsEndpoint() throws IOException {
        String token = loginAndGetToken();

        URL url = new URL("https://localhost/events");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Content-Type", "application/json");

        int code = con.getResponseCode();
        assertEquals(200, code);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        String responseBody = response.toString();
        assertTrue(responseBody.contains("title") || responseBody.contains("event_date"),
                "La resposta hauria de contenir informació d'un esdeveniment.");
    }

    /**
     * Comprova que es pot crear un esdeveniment nou via POST /events.
     */
    @Test
    void testCreateEvent() throws IOException {
        String token = loginAndGetToken();

        URL url = new URL("https://localhost/events");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String nouEvent = """
        {
          "title": "Prova JUnit",
          "description": "Esdeveniment de prova automatitzada",
          "location": "Biblioteca Central",
          "event_date": "2025-06-01",
          "start_time": "18:00",
          "end_time": "19:00"
        }
        """;

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = nouEvent.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = con.getResponseCode();
        assertTrue(code == 200 || code == 201);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        String body = response.toString();
        assertTrue(body.contains("Prova JUnit"));

        // Guarda l'ID per a l'eliminació posterior (opcional si vols enllaçar-ho)
        int start = body.indexOf("\"id\":") + 5;
        int end = body.indexOf(",", start);
        int eventId = Integer.parseInt(body.substring(start, end).trim());

        // Elimina l'event
        deleteEvent(eventId, token);
    }

    // Mètode auxiliar per login
    private String loginAndGetToken() throws IOException {
        URL url = new URL("https://localhost/auth/login");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String credentials = "{\"username\": \"admin\", \"password\": \"admin\"}";
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = credentials.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder resposta = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            resposta.append(line);
        }
        in.close();

        String body = resposta.toString();
        int start = body.indexOf("token\":\"") + 8;
        int end = body.indexOf("\"", start);
        return body.substring(start, end);
    }

    // Mètode auxiliar per esborrar esdeveniment
    private void deleteEvent(int eventId, String token) throws IOException {
        URL url = new URL("https://localhost/events/" + eventId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = con.getResponseCode();
        assertTrue(responseCode == 200 || responseCode == 204, "L'esdeveniment hauria d'haver estat eliminat.");
    }

}
