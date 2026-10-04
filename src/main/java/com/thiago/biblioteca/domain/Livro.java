package com.thiago.biblioteca.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "livro")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(name = "ano_publicacao")
    private Integer anoPublicacao;

    @Column(nullable = false)
    private int exemplares;

    protected Livro() {
    }

    public Livro(String isbn, String titulo, String autor, Integer anoPublicacao, int exemplares) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.exemplares = exemplares;
    }

    public void atualizar(String titulo, String autor, Integer anoPublicacao, int exemplares) {
        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.exemplares = exemplares;
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public Integer getAnoPublicacao() { return anoPublicacao; }
    public int getExemplares() { return exemplares; }
}
