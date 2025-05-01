package com.codexteam.codexlib.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representa la resposta rebuda del servidor amb llibres externs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespostaExtern {
    private int total;
    private List<LlibreExtern> results;

    public RespostaExtern() {}

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<LlibreExtern> getResults() {
        return results;
    }

    public void setResults(List<LlibreExtern> results) {
        this.results = results;
    }
}
