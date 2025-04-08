package com.codexteam.codexlib.models;

/**
 * Representa un llibre dins del sistema. Conté informació bàsica com títol, autor, ISBN, gènere,
 * data de publicació, disponibilitat i dades de creació/modificació.
 */
public class Llibre {
    private int id;
    private String title;
    private Autor author;
    private String isbn;
    private String published_date;
    private Genere genre;
    private boolean available;
    private String created_at;
    private String updated_at;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Autor getAuthor() { return author; }
    public void setAuthor(Autor author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getPublished_date() { return published_date; }
    public void setPublished_date(String published_date) { this.published_date = published_date; }

    public Genere getGenre() { return genre; }
    public void setGenre(Genere genre) { this.genre = genre; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }

    // Métodos para mostrar en la tabla
    public String getAuthorName() {
        return author != null ? author.getName() : "Desconegut";
    }

    public String getGenreName() {
        return genre != null ? genre.getName() : "Sense gènere";
    }
}