package com.codexteam.codexlib.models;

import java.util.List;

/**
 * Agrupa el resultat d’una cerca combinada,
 * mantenint separades les llistes de llibres i d’autors trobats.
 */
public class ResultatCerca {
    private List<Llibre> books;
    private List<Autor> authors;

    public List<Llibre> getBooks() {
        return books;
    }

    public void setBooks(List<Llibre> books) {
        this.books = books;
    }

    public List<Autor> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Autor> authors) {
        this.authors = authors;
    }
}
