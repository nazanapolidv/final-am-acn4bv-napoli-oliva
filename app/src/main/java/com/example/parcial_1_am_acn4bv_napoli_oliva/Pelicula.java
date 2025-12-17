package com.example.parcial_1_am_acn4bv_napoli_oliva;
import java.io.Serializable;

public class Pelicula implements Serializable {
    private int id;
    private String titulo;
    private int anio;
    private String genero;
    private String urlImagen;
    private String urlDescripcion;
    private String urlTrailer;
    private String userId;

    public Pelicula(int id, String titulo, int anio, String genero, String urlImagen, String urlDescripcion, String urlTrailer){
        this.id = id;
        this.titulo = titulo;
        this.anio = anio;
        this.genero = genero;
        this.urlImagen = urlImagen;
        this.urlDescripcion = urlDescripcion;
        this.urlTrailer = urlTrailer;
    }

    // Constructor vacío para Firebase
    public Pelicula (){ }

    // GETTERS
    public int getId (){ return id; }
    public String getTitulo(){ return titulo; }
    public int getAnio(){ return anio; }
    public String getGenero(){ return genero; }
    public String getUrlImagen(){ return urlImagen; }
    public String getUrlDescripcion() { return urlDescripcion; }
    public String getUserId() { return userId; }
    public String getUrlTrailer() { return urlTrailer; }

    // SETTERS
    public void setId(int id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setAnio(int anio) { this.anio = anio; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
    public void setUrlDescripcion(String urlDescripcion) { this.urlDescripcion = urlDescripcion; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUrlTrailer(String urlTrailer) { this.urlTrailer = urlTrailer; }
}