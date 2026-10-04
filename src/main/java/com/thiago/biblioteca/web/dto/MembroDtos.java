package com.thiago.biblioteca.web.dto;

import com.thiago.biblioteca.domain.Membro;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class MembroDtos {

    private MembroDtos() {
    }

    public record NovoMembro(
            @NotBlank @Size(max = 150) String nome,
            @NotBlank @Email @Size(max = 200) String email) {
    }

    public record MembroResposta(Long id, String nome, String email, boolean ativo) {

        public static MembroResposta de(Membro m) {
            return new MembroResposta(m.getId(), m.getNome(), m.getEmail(), m.isAtivo());
        }
    }
}
