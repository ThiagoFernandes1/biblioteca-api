package com.thiago.biblioteca.service;

/** Tentativa de cadastrar algo que ja existe (ISBN ou e-mail repetido). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
