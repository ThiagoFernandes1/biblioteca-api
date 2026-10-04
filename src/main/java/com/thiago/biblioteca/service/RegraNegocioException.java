package com.thiago.biblioteca.service;

/** Pedido valido no formato, mas que as regras da biblioteca nao permitem. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
