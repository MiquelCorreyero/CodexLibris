package com.codexteam.codexlib.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Model que representa un esdeveniment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Esdeveniment {

    @JsonProperty("title")
    private String titol;

    @JsonProperty("description")
    private String contingut;

    @JsonProperty("location")
    private String adreca;

    @JsonProperty("event_date")
    private String data;

    @JsonProperty("start_time")
    private String horaInici;

    @JsonProperty("end_time")
    private String horaFi;

    private int id;

    public Esdeveniment() {
    }

    public Esdeveniment(int id, String titol, String contingut, String adreca, String data, String horaInici) {
        this.id = id;
        this.titol = titol;
        this.contingut = contingut;
        this.adreca = adreca;
        this.data = data;
        this.horaInici = horaInici;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitol() {
        return titol;
    }

    public void setTitol(String titol) {
        this.titol = titol;
    }

    public String getContingut() {
        return contingut;
    }

    public void setContingut(String contingut) {
        this.contingut = contingut;
    }

    public String getAdreca() {
        return adreca;
    }

    public void setAdreca(String adreca) {
        this.adreca = adreca;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHoraInici() {
        return horaInici;
    }

    public void setHoraInici(String horaInici) {
        this.horaInici = horaInici;
    }

    public String getHoraFi() {
        return horaFi;
    }

    public void setHoraFi(String horaFi) {
        this.horaFi = horaFi;
    }

    @Override
    public String toString() {
        return titol + " (" + data + " " + horaInici + ")";
    }
}
