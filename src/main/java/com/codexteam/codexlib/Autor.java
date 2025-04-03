package com.codexteam.codexlib;

public class Autor {
    private int id;
    private String name;
    private String birth_date;
    private String nationality;
    private String created_at;
    private String updated_at;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBirth_date() { return birth_date; }
    public void setBirth_date(String birth_date) { this.birth_date = birth_date; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
}

