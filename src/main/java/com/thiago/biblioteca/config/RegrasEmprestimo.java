package com.thiago.biblioteca.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Regras de emprestimo configuraveis em application.yml (prefixo "biblioteca").
 *
 * @param prazoDias         dias corridos ate a data prevista de devolucao
 * @param limiteEmprestimos emprestimos em aberto permitidos por membro
 * @param multaPorDia       valor cobrado por dia de atraso na devolucao
 */
@Validated
@ConfigurationProperties("biblioteca")
public record RegrasEmprestimo(
        @Min(1) int prazoDias,
        @Min(1) int limiteEmprestimos,
        @NotNull @DecimalMin("0.00") BigDecimal multaPorDia) {
}
