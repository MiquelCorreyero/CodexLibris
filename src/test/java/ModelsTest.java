import com.codexteam.codexlib.models.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitàries per verificar els models de dades utilitzats a CodexLibris.
 * Es comprova el correcte funcionament dels getters i setters de les classes:
 * Autor, Usuari, Genere, Llibre, Reserva i Esdeveniment.
 */
public class ModelsTest {

    /**
     * Prova la creació i accés als valors d'un objecte Autor.
     */
    @Test
    void testAutor() {
        Autor autor = new Autor();
        autor.setId(1);
        autor.setName("Mercè Rodoreda");
        autor.setNationality("Cristinenca");

        assertEquals(1, autor.getId());
        assertEquals("Mercè Rodoreda", autor.getName());
        assertEquals("Cristinenca", autor.getNationality());
    }

    /**
     * Prova la creació d'un usuari amb rol i la recuperació de totes les seves propietats.
     */
    @Test
    void testUsuari() {
        Usuari usuari = new Usuari();

        Usuari.Rol rol = new Usuari.Rol();
        rol.setId(1);
        rol.setName("Administrador");

        usuari.setId(10);
        usuari.setUsername("mcorreyero");
        usuari.setFirstName("Miquel");
        usuari.setLastName("Correyero");
        usuari.setEmail("miquel@test.com");
        usuari.setPassword("password123");
        usuari.setCreatedAt("2024-04-01T10:00:00");
        usuari.setLastLogin("2024-04-10T18:00:00");
        usuari.setActive(true);
        usuari.setEnabled(true);
        usuari.setRole(rol);

        assertEquals(10, usuari.getId());
        assertEquals("mcorreyero", usuari.getUsername());
        assertEquals("Miquel", usuari.getFirstName());
        assertEquals("Correyero", usuari.getLastName());
        assertEquals("miquel@test.com", usuari.getEmail());
        assertEquals("password123", usuari.getPassword());
        assertEquals("2024-04-01T10:00:00", usuari.getCreatedAt());
        assertEquals("2024-04-10T18:00:00", usuari.getLastLogin());
        assertTrue(usuari.isActive());
        assertTrue(usuari.isEnabled());
        assertNotNull(usuari.getRole());
        assertEquals(1, usuari.getRole().getId());
        assertEquals("Administrador", usuari.getRole().getName());
    }

    /**
     * Verifica la correcta assignació dels camps d'un objecte Genere.
     */
    @Test
    void testGenere() {
        Genere genere = new Genere();
        genere.setId(1);
        genere.setName("Fantasia");
        genere.setDescription("Històries amb elements màgics o sobrenaturals");

        assertEquals(1, genere.getId());
        assertEquals("Fantasia", genere.getName());
        assertEquals("Històries amb elements màgics o sobrenaturals", genere.getDescription());
    }

    /**
     * Comprova el funcionament dels getters i setters d’un objecte Llibre,
     * incloent relacions amb Autor i Genere.
     */
    @Test
    void testLlibre() {
        Autor autor = new Autor();
        autor.setId(1);
        autor.setName("Autor Prova");

        Genere genere = new Genere();
        genere.setId(2);
        genere.setName("Assaig");

        Llibre llibre = new Llibre();
        llibre.setId(10);
        llibre.setTitle("Títol de Prova");
        llibre.setAuthor(autor);
        llibre.setIsbn("1234567890123");
        llibre.setPublished_date("2023-01-01");
        llibre.setGenre(genere);
        llibre.setAvailable(true);

        assertEquals(10, llibre.getId());
        assertEquals("Títol de Prova", llibre.getTitle());
        assertEquals("1234567890123", llibre.getIsbn());
        assertEquals("2023-01-01", llibre.getPublished_date());
        assertTrue(llibre.isAvailable());
        assertEquals("Autor Prova", llibre.getAuthor().getName());
        assertEquals("Assaig", llibre.getGenre().getName());
    }

    /**
     * Comprova que una Reserva pot emmagatzemar tota la informació rellevant
     * sobre el préstec d’un llibre per part d’un usuari.
     */
    @Test
    void testReserva() {
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setUser_id(100);
        reserva.setUser_name("Anna");
        reserva.setBook_id(200);
        reserva.setBook_title("El llibre");
        reserva.setLoan_date(LocalDate.of(2024, 4, 10));
        reserva.setDue_date(LocalDate.of(2024, 4, 17));
        reserva.setLoan_status_id(1);
        reserva.setLoan_status_name("En préstec");

        assertEquals(1, reserva.getId());
        assertEquals(100, reserva.getUser_id());
        assertEquals("Anna", reserva.getUser_name());
        assertEquals(200, reserva.getBook_id());
        assertEquals("El llibre", reserva.getBook_title());
        assertEquals(LocalDate.of(2024, 4, 10), reserva.getLoan_date());
        assertEquals(LocalDate.of(2024, 4, 17), reserva.getDue_date());
        assertEquals(1, reserva.getLoan_status_id());
        assertEquals("En préstec", reserva.getLoan_status_name());
    }

    /**
     * Verifica tots els camps de l’objecte Esdeveniment, incloent el mètode toString.
     */
    @Test
    void testEsdeveniment() {
        Esdeveniment e = new Esdeveniment();
        e.setId(1);
        e.setTitol("Taller de lectura");
        e.setContingut("Comentem una obra clàssica");
        e.setAdreca("Biblioteca Central");
        e.setData("2024-05-01");
        e.setHoraInici("17:00");
        e.setHoraFi("19:00");

        assertEquals(1, e.getId());
        assertEquals("Taller de lectura", e.getTitol());
        assertEquals("Comentem una obra clàssica", e.getContingut());
        assertEquals("Biblioteca Central", e.getAdreca());
        assertEquals("2024-05-01", e.getData());
        assertEquals("17:00", e.getHoraInici());
        assertEquals("19:00", e.getHoraFi());

        // Verificació del mètode toString
        assertEquals("Taller de lectura (2024-05-01 17:00)", e.toString());
    }

}

