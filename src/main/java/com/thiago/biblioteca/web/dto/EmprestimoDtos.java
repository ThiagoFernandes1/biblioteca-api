package com.thiago.biblioteca.web.dto;

import com.thiago.biblioteca.domain.Emprestimo;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class EmprestimoDtos {

    private EmprestimoDtos() {
    }

    public record NovoEmprestimo(@NotNull Long livroId, @NotNull Long membroId) {
    }

    public record EmprestimoResposta(Long id, Long livroId, String titulo, Long membroId, String membro,
                                     LocalDate dataEmprestimo, LocalDate dataPrevista,
                                     LocalDate dataDevolucao, BigDecimal multa) {

        public static EmprestimoResposta de(Emprestimo e) {
            return new EmprestimoResposta(e.getId(),
                    e.getLivro().getId(), e.getLivro().getTitulo(),
                    e.getMembro().getId(), e.getMembro().getNome(),
                    e.getDataEmprestimo(), e.getDataPrevista(), e.getDataDevolucao(), e.getMulta());
        }
    }
}
