package com.example.proyectopropio.ui;
public class Club {
    Double latitud;
    Double longitud;
    String direccio;
    String problema;
    String url;
    String Carles;

    public Club(Double latitud, Double longitud, String direccio,
                String problema, String url, String Carles) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccio = direccio;
        this.problema = problema;
        this.url = url;
        this.Carles= Carles;
    }

    public Club() {
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getDireccio() {
        return direccio;
    }

    public void setDireccio(String direccio) {
        this.direccio = direccio;
    }

    public String getProblema() {
        return problema;
    }

    public void setProblema(String problema) {
        this.problema = problema;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCarles() {
        return Carles;
    }

    public void setCarles(String carles) {
        this.Carles =carles;
    }
}