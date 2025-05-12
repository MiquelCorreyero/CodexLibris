
import com.codexteam.codexlib.services.ClientFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.util.UUID;

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

    private String token;
    private static final String BASE_URL = "https://localhost";
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Comprova que es pot fer login amb credencials vàlides
     * i que el servidor retorna un codi d'estat 200.
     */
    @Test
    void testLogin() throws IOException {
        URL url = new URL(BASE_URL + "/auth/login");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String credentials = "{\"username\": \"admin\", \"password\": \"admin\"}";
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = credentials.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();

        if (responseCode != 200) {
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(con.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                System.err.println("ERROR DEL SERVIDOR (login):");
                System.err.println(errorResponse);
            }
        }

        assertEquals(200, responseCode);
    }

    private String loginAndGetToken(String user, String pass) throws IOException, InterruptedException {
        HttpClient client = getTestClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\": \"" + user + "\", \"password\": \"" + pass + "\"}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Error login: " + response.body());
        return new JSONObject(response.body()).getString("token");
    }

    /**
     * Mètode executat abans de cada test per obtenir un nou token JWT vàlid.
     *
     * Aquest token s'utilitza per autenticar les peticions HTTP fetes durant les proves.
     * Garanteix que cada test disposi d'un token fresc i funcional, evitant errors per
     * expiració o invalidació de sessions anteriors.
     */
    @BeforeEach
    void obtenirTokenNou() throws IOException, InterruptedException {
        this.token = loginAndGetToken();
    }

    /**
     * Comprova que l'endpoint /books és accessible amb autenticació
     * i que la resposta conté dades de llibres (com ara el títol).
     */
    @Test
    void testGetBooks() throws IOException, InterruptedException {

        URL url = new URL(BASE_URL + "/books");
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
     * Prova per validar la creació i, tot seguit, l’eliminació d’un autor.
     */
    @Test
    void testCreateAutor() throws IOException, InterruptedException {
        String token = loginAndGetToken();

        // ---------- 1. Crear l’autor ----------
        String nouAutor = """
        {
          "name": "Autor CRUD Test",
          "birth_date": "1970-01-01",
          "nationality": "Catalana"
        }
        """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/authors"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(nouAutor))
                .build();

        HttpResponse<String> response =
                getTestClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(),
                "Error al crear l’autor: " + response.body());

        JSONObject json = new JSONObject(response.body());
        assertEquals("Autor CRUD Test", json.getString("name"));

        int autorId = json.getInt("id");   // ID retornat pel backend

        // ---------- 2. Eliminar l’autor creat ----------
        deleteAuthor(autorId, token);
    }

    /**
     * Prova per validar l'actualització d’un autor.
     */
    @Test
    void testUpdateAutor() throws IOException, InterruptedException {

        // Crear autor
        int autorId = crearAutor("Autor Original", "1960-05-05", "Espanyola", token);

        // JSON amb actualització
        String actualitzat = """
    {
      "id": %d,
      "name": "Autor Actualitzat",
      "birth_date": "1960-05-05",
      "nationality": "Espanyola"
    }
    """.formatted(autorId);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/authors/" + autorId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(actualitzat))
                .build();

        HttpResponse<String> updateResponse = getTestClient().send(updateRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, updateResponse.statusCode(), "Error al actualitzar l’autor: " + updateResponse.body());

        JSONObject updatedJson = new JSONObject(updateResponse.body());
        assertEquals("Autor Actualitzat", updatedJson.getString("name"));

        deleteAuthor(autorId, token);
    }

    /**
     * Prova per comprovar l'eliminació d’un autor mitjançant l'API REST.
     */
    @Test
    void testDeleteAutor() throws IOException, InterruptedException {
        int autorId = crearAutor("Autor Per Eliminar", "1950-01-01", "Francesa", token);
        deleteAuthor(autorId, token);
    }

    /**
     * Prova per comprovar la creació i eliminació d’un gènere
     */
    @Test
    void testCreateDeleteGenere() throws IOException, InterruptedException {

        // Crear un nou gènere
        String nouGenere = """
            {
              "name": "Història alternativa",
              "description": "Gènere de ficció especulativa"
            }
            """;

        HttpRequest genereRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/genres"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(nouGenere))
                .build();

        HttpResponse<String> genereResponse = getTestClient().send(genereRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, genereResponse.statusCode(), "Error al crear el gènere: " + genereResponse.body());

        JSONObject genereJson = new JSONObject(genereResponse.body());
        int genereId = genereJson.getInt("id");

        // Eliminar el gènere creat
        deleteGenre(genereId, token);
    }

    /**
     * Prova per validar l'actualització d'un gènere.
     */
    @Test
    void testUpdateGenere() throws IOException, InterruptedException {

        // Crear gènere original
        String genereOriginal = """
            {
              "name": "Aventura clàssica",
              "description": "Gènere creat per a proves d'actualització"
            }
            """;

        HttpRequest crearRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/genres"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(genereOriginal))
                .build();

        HttpResponse<String> crearResponse = getTestClient().send(crearRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, crearResponse.statusCode(), "Error al crear el gènere inicial: " + crearResponse.body());

        JSONObject genereCreat = new JSONObject(crearResponse.body());
        int genereId = genereCreat.getInt("id");

        // Actualitzar el gènere
        String genereActualitzat = """
    {
      "name": "Aventura modernitzada",
      "description": "Gènere modificat amb dades noves"
    }
    """;

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/genres/" + genereId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(genereActualitzat))
                .build();

        HttpResponse<String> updateResponse = getTestClient().send(updateRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, updateResponse.statusCode(), "Error al actualitzar el gènere: " + updateResponse.body());

        JSONObject genereModificat = new JSONObject(updateResponse.body());
        assertEquals("Aventura modernitzada", genereModificat.getString("name"));
        assertEquals("Gènere modificat amb dades noves", genereModificat.getString("description"));

        // Eliminar gènere de proves
        deleteGenre(genereId, token);
    }

    /**
     * Prova que comprova la creació i eliminació d'una reserva.
     */
    @Test
    void testCreateDeleteReserva() throws IOException, InterruptedException {

        // Reserva amb els camps en camelCase segons el contracte Swagger
        String novaReserva = """
            {
              "loan_date": "2025-05-10",
              "due_date": "2025-05-20",
              "return_date": "2025-05-19",
              "userId": 1,
              "bookId": 1,
              "statusId": 1
            }
            """;

        HttpRequest reservaRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/loans"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(novaReserva))
                .build();

        HttpResponse<String> resposta = getTestClient().send(reservaRequest, HttpResponse.BodyHandlers.ofString());

        assertTrue(resposta.statusCode() == 200 || resposta.statusCode() == 201,
                "Error al crear la reserva: " + resposta.body());

        JSONObject json = new JSONObject(resposta.body());
        assertTrue(json.has("id"), "La resposta hauria de contenir l’ID de la reserva.");
        int reservaId = json.getInt("id");

        // Eliminar la reserva creada
        deleteReserva(reservaId, token);
    }

    /**
     * Elimina una reserva mitjançant el seu ID.
     */
    private void deleteReserva(int reservaId, String token) throws IOException {
        URL url = new URL(BASE_URL + "/loans/" + reservaId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = con.getResponseCode();
        assertTrue(responseCode == 200 || responseCode == 204, "La reserva hauria d’haver estat eliminada.");
    }

    /**
     * Elimina un autor del sistema mitjançant el seu ID.
     */
    private void deleteAuthor(int autorId, String token) throws IOException {
        URL url = new URL(BASE_URL + "/authors/" + autorId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = con.getResponseCode();
        assertTrue(responseCode == 200 || responseCode == 204, "L'autor hauria d'haver estat eliminat.");
    }

    /**
     * Crea un nou autor a través de l'API REST, utilitzant el token d'autenticació proporcionat.
     */
    private int crearAutor(String nom, String naixement, String nacionalitat, String token) throws IOException, InterruptedException {
        String nouAutor = String.format("""
    {
      "name": "%s",
      "birth_date": "%s",
      "nationality": "%s"
    }
    """, nom, naixement, nacionalitat);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/authors"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(nouAutor))
                .build();

        HttpResponse<String> response = getTestClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Error al crear l’autor auxiliar: " + response.body());

        JSONObject json = new JSONObject(response.body());
        return json.getInt("id");
    }

    /**
     * Prova d'integració que valida la creació d’un nou gènere mitjançant l'API REST.
     */
    @Test
    void testCreateGenere() throws IOException, InterruptedException {
        String token = loginAndGetToken();

        String nouGenere = """
        {
          "name": "Ciberpunk_2077",
          "description": "Històries de tecnologia i distòpies."
        }
        """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/genres"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(nouGenere))
                .build();

        HttpResponse<String> response = getTestClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Error al crear el gènere: " + response.body());

        JSONObject genereJson = new JSONObject(response.body());
        assertEquals("Ciberpunk_2077", genereJson.getString("name"));

        // Eliminar gènere creat per netejar proves
        int genereId = genereJson.getInt("id");
        deleteGenre(genereId, token);
    }

    /**
     * Prova d'integració que crea un esdeveniment, l'actualitza
     * i finalment l'elimina.
     */
    @Test
    void testUpdateEvent() throws IOException, InterruptedException {
        String token = loginAndGetToken();

        /* ---------- 1. Crear esdeveniment ---------- */
        String eventOriginal = """
        {
          "title": "Esdeveniment original",
          "description": "Descripció inicial",
          "location": "Sala A",
          "event_date": "2025-07-01",
          "start_time": "10:00",
          "end_time": "11:00"
        }
        """;

        HttpRequest crearRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/events"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(eventOriginal))
                .build();

        HttpResponse<String> crearResponse =
                getTestClient().send(crearRequest, HttpResponse.BodyHandlers.ofString());

        assertTrue(crearResponse.statusCode() == 200 || crearResponse.statusCode() == 201,
                "Error al crear l'esdeveniment inicial: " + crearResponse.body());

        JSONObject createdJson = new JSONObject(crearResponse.body());
        int eventId = createdJson.getInt("id");

        /* ---------- 2. Actualitzar esdeveniment ---------- */
        String eventActualitzat = """
        {
          "title": "Esdeveniment actualitzat",
          "description": "Nou contingut de descripció",
          "location": "Sala B",
          "event_date": "2025-07-02",
          "start_time": "12:00",
          "end_time": "13:00"
        }
        """;

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/events/" + eventId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(eventActualitzat))
                .build();

        HttpResponse<String> updateResponse =
                getTestClient().send(updateRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, updateResponse.statusCode(),
                "Error al actualitzar l'esdeveniment: " + updateResponse.body());

        JSONObject updatedJson = new JSONObject(updateResponse.body());
        assertEquals("Esdeveniment actualitzat", updatedJson.getString("title"));
        assertEquals("Sala B", updatedJson.getString("location"));

        /* ---------- 3. Netejar ---------- */
        deleteEvent(eventId, token);
    }

    /**
     * Prova d'integració que valida l'eliminació d’un esdeveniment.
     */
    @Test
    void testDeleteEvent() throws IOException, InterruptedException {
        String token = loginAndGetToken();

        /* ---------- 1. Crear esdeveniment de prova ---------- */
        String nouEvent = """
        {
          "title": "Event a eliminar",
          "description": "Prova DELETE",
          "location": "Sala C",
          "event_date": "2025-08-01",
          "start_time": "09:00",
          "end_time": "10:00"
        }
        """;

        HttpRequest crearRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/events"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(nouEvent))
                .build();

        HttpResponse<String> crearResponse =
                getTestClient().send(crearRequest, HttpResponse.BodyHandlers.ofString());

        assertTrue(crearResponse.statusCode() == 200 || crearResponse.statusCode() == 201,
                "Error al crear l'esdeveniment: " + crearResponse.body());

        int eventId = new JSONObject(crearResponse.body()).getInt("id");

        /* ---------- 2. Eliminar esdeveniment ---------- */
        URL url = new URL(BASE_URL + "/events/" + eventId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", "Bearer " + token);

        int deleteCode = con.getResponseCode();
        assertTrue(deleteCode == 200 || deleteCode == 204,
                "Error al eliminar l'esdeveniment. Codi: " + deleteCode);

        /* ---------- 3. (Opcional) Verificar que ja no existeix ---------- */
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/events/" + eventId))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> getResponse =
                getTestClient().send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, getResponse.statusCode(),
                "S'esperava 404 després d'eliminar, però s'ha rebut " + getResponse.statusCode());
    }

    /**
     * Prova d'integració que valida la creació d’un llibre mitjançant l'API REST.
     *
     * Passos:
     * <ol>
     *   <li>Autentica l’usuari i obté un token JWT.</li>
     *   <li>Envia una petició <code>POST /books</code> amb les dades del nou llibre.</li>
     *   <li>Comprova que la resposta és 200 OK o 201 Created i que el títol coincideix.</li>
     *   <li>Elimina el llibre creat per mantenir la base de dades neta (DELETE /books/{id}).</li>
     * </ol>
     *
     * @throws IOException          Si hi ha un error de comunicació amb el backend.
     * @throws InterruptedException Si la petició HTTP és interrompuda.
     */
    @Test
    void testCreateBook() throws IOException, InterruptedException {

        String token = loginAndGetToken();

        // JSON de creació (ajusta els camps als que requereixi el teu backend)
        String nouLlibre = """
        {
          "title": "Proves JUnit amb CodexLibris",
          "authorId": 1,
          "genreId": 1,
          "isbn": "9781234567890",
          "publish_date": "2024-12-01",
          "pages": 256,
          "available": true
        }
        """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/books"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(nouLlibre))
                .build();

        HttpResponse<String> createResponse =
                getTestClient().send(createRequest, HttpResponse.BodyHandlers.ofString());

        assertTrue(createResponse.statusCode() == 200 || createResponse.statusCode() == 201,
                "Error al crear el llibre: " + createResponse.body());

        JSONObject json = new JSONObject(createResponse.body());
        assertEquals("Proves JUnit amb CodexLibris", json.getString("title"),
                "El títol retornat no coincideix");

        int bookId = json.getInt("id");

        // --- Neteja: eliminar el llibre creat ---
        URL url = new URL(BASE_URL + "/books/" + bookId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", "Bearer " + token);

        int deleteCode = con.getResponseCode();
        assertTrue(deleteCode == 200 || deleteCode == 204,
                "El llibre hauria d'haver estat eliminat.");
    }

    /**
     * Comprova que es poden obtenir autors mitjançant l'endpoint /authors
     * i que la resposta JSON conté el camp 'name'.
     */
    @Test
    void testGetAuthors() throws IOException, InterruptedException {

        URL url = new URL(BASE_URL + "/authors");
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
     * Verifica que l'endpoint /genres respon amb èxit
     * i que la resposta conté informació sobre els gèneres.
     */
    @Test
    void testGetGeneres() throws IOException, InterruptedException {

        URL url = new URL(BASE_URL + "/genres");
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
     * Valida que l'endpoint /loans respon correctament
     * i inclou informació rellevant sobre les reserves fetes.
     */
    @Test
    void testGetReserves() throws IOException, InterruptedException {

        URL url = new URL(BASE_URL + "/loans");
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
     * Comprova que l'endpoint /events permet obtenir esdeveniments
     * i que la resposta inclou camps com 'title' o 'event_date'.
     */
    @Test
    void testGetEvents() throws IOException, InterruptedException {

        URL url = new URL(BASE_URL + "/events");
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
     * Crea un nou esdeveniment via POST i comprova que es crea correctament.
     * També esborra l'esdeveniment creat per mantenir el sistema net.
     */
    @Test
    void testCreateEvent() throws IOException, InterruptedException {

        URL url = new URL(BASE_URL + "/events");
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

        int start = body.indexOf("\"id\":") + 5;
        int end = body.indexOf(",", start);
        int eventId = Integer.parseInt(body.substring(start, end).trim());

        deleteEvent(eventId, token);
    }

    /**
     * Comprova que l'endpoint de logout respon correctament
     * i retorna un missatge confirmant el tancament de sessió.
     */
    @Test
    void testLogoutEndpoint() throws IOException, InterruptedException {
        String tokenTemporal = loginAndGetToken();

        URL url = new URL(BASE_URL + "/auth/logout");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + tokenTemporal);
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

    @Test
    public void testTokenInvalidRebutjaAcces() throws Exception {
        // Desactiva validació SSL (NO fer això en producció!!)
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpClient client = HttpClient.newBuilder()
                .sslContext(sc)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/books"))
                .header("Authorization", "Bearer token_fals")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, response.statusCode(), "Amb un token invàlid, hauria de retornar 401.");
    }

    /**
     * Elimina un gènere del sistema mitjançant el seu ID.
     */
    private void deleteGenre(int genereId, String token) throws IOException {
        URL url = new URL(BASE_URL + "/genres/" + genereId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = con.getResponseCode();
        assertTrue(responseCode == 200 || responseCode == 204, "El gènere hauria d'haver estat eliminat.");
    }

    /**
     * Realitza una petició POST a l'endpoint de login per obtenir un token JWT.
     *
     * Aquest mètode envia les credencials d'usuari administrador al servidor i
     * comprova que la resposta sigui correcta (codi HTTP 200). Si la resposta és vàlida,
     * extreu i retorna el token JWT del cos de la resposta JSON.
     *
     * @return El token JWT com a cadena de text.
     * @throws IOException Si hi ha un problema de connexió amb el servidor.
     * @throws InterruptedException Si la petició HTTP és interrompuda.
     */
    private String loginAndGetToken() throws IOException, InterruptedException {
        HttpClient client = getTestClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\": \"admin\", \"password\": \"admin\"}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Error del servidor: " + response.body());

        JSONObject json = new JSONObject(response.body());
        return json.getString("token");
    }

    /**
     * Elimina un esdeveniment específic del servidor mitjançant una petició HTTP DELETE.
     */
    private void deleteEvent(int eventId, String token) throws IOException {
        URL url = new URL(BASE_URL + "/events/" + eventId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = con.getResponseCode();
        assertTrue(responseCode == 200 || responseCode == 204, "L'esdeveniment hauria d'haver estat eliminat.");
    }

    /**
     * Configura l'entorn de proves per desactivar la verificació SSL,
     * permetent connexions HTTPS fins i tot amb certificats autofirmats
     * o no verificats. Aquesta configuració només s'ha d'utilitzar en
     * entorns de desenvolupament o proves locals.
     */
    @BeforeAll
    static void desactivarVerificacioSSL() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    /**
     * Retorna una instància de {@link HttpClient} configurada per a proves.
     *
     * Aquesta instància pot acceptar certificats no verificats si l'aplicació
     * s'executa en mode desenvolupament, segons la configuració de {@link ClientFactory}.
     *
     * @return {@link HttpClient} configurat per a l'entorn de proves
     */
    private HttpClient getTestClient() {
        return ClientFactory.getClient();
    }

}
