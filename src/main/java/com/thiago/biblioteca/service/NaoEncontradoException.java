package com.thiago.biblioteca.service;

public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String recurso, Long id) {
        super(recurso + " " + id + " nao encontrado.");
    }
}
