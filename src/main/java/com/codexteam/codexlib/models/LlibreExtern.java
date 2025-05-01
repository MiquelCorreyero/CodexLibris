package com.codexteam.codexlib.models;

/**
 * Representa un llibre obtingut a través de l’API externa (Open Library).
 * Conté només la informació bàsica necessària per importar-lo al sistema.
 */
public class LlibreExtern {

    private String title;
    private String author;
    private int year;
    private String isbn;

    public LlibreExtern() {
    }

    public LlibreExtern(String title, String author, int year, String isbn) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.isbn = isbn;
    }

    // Getters i setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    // Format de visualització útil per a logs o debug
    @Override
    public String toString() {
        return title + " (" + year + ") - " + author;
    }
}

