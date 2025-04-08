package com.codexteam.codexlib.models;

import java.time.LocalDate;

public class Reserva {
    private int id;
    private LocalDate loan_date;
    private LocalDate due_date;
    private LocalDate return_date;
    private int user_id;
    private String user_name;
    private String user_first_name;
    private String user_email;
    private int book_id;
    private String book_title;
    private String book_isbn;
    private Genere genre;
    private int loan_status_id;
    private String loan_status_name;

    // Getters i setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getLoan_date() {
        return loan_date;
    }

    public void setLoan_date(LocalDate loan_date) {
        this.loan_date = loan_date;
    }

    public LocalDate getDue_date() {
        return due_date;
    }

    public void setDue_date(LocalDate due_date) {
        this.due_date = due_date;
    }

    public LocalDate getReturn_date() {
        return return_date;
    }

    public void setReturn_date(LocalDate return_date) {
        this.return_date = return_date;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_first_name() {
        return user_first_name;
    }

    public void setUser_first_name(String user_first_name) {
        this.user_first_name = user_first_name;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public int getBook_id() {
        return book_id;
    }

    public void setBook_id(int book_id) {
        this.book_id = book_id;
    }

    public String getBook_title() {
        return book_title;
    }

    public void setBook_title(String book_title) {
        this.book_title = book_title;
    }

    public String getBook_isbn() {
        return book_isbn;
    }

    public void setBook_isbn(String book_isbn) {
        this.book_isbn = book_isbn;
    }

    public Genere getGenre() {
        return genre;
    }

    public void setGenre(Genere genre) {
        this.genre = genre;
    }

    public int getLoan_status_id() {
        return loan_status_id;
    }

    public void setLoan_status_id(int loan_status_id) {
        this.loan_status_id = loan_status_id;
    }

    public String getLoan_status_name() {
        return loan_status_name;
    }

    public void setLoan_status_name(String loan_status_name) {
        this.loan_status_name = loan_status_name;
    }
}

