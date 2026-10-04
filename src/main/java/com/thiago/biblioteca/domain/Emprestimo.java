package com.thiago.biblioteca.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "emprestimo")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id")
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membro_id")
    private Membro membro;

    @Column(name = "data_emprestimo", nullable = false)
    private LocalDate dataEmprestimo;

    @Column(name = "data_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(name = "data_devolucao")
    private LocalDate dataDevolucao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal multa = BigDecimal.ZERO;

    protected Emprestimo() {
    }

    public Emprestimo(Livro livro, Membro membro, LocalDate dataEmprestimo, LocalDate dataPrevista) {
        this.livro = livro;
        this.membro = membro;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevista = dataPrevista;
    }

    public void registrarDevolucao(LocalDate data, BigDecimal multa) {
        this.dataDevolucao = data;
        this.multa = multa;
    }

    public boolean isDevolvido() {
        return dataDevolucao != null;
    }

    public Long getId() { return id; }
    public Livro getLivro() { return livro; }
    public Membro getMembro() { return membro; }
    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public LocalDate getDataPrevista() { return dataPrevista; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public BigDecimal getMulta() { return multa; }
}
