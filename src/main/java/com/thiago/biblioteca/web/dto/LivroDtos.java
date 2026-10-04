package com.thiago.biblioteca.web.dto;

import com.thiago.biblioteca.domain.Livro;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class LivroDtos {

    private LivroDtos() {
    }

    public record NovoLivro(
            @NotBlank @Pattern(regexp = "\\d{10}|\\d{13}", message = "deve ter 10 ou 13 digitos, sem tracos") String isbn,
            @NotBlank @Size(max = 200) String titulo,
            @NotBlank @Size(max = 150) String autor,
            @Min(1450) @Max(2100) Integer anoPublicacao,
            @Min(1) int exemplares) {

        public Livro paraEntidade() {
            return new Livro(isbn, titulo.trim(), autor.trim(), anoPublicacao, exemplares);
        }
    }

    public record AtualizaLivro(
            @NotBlank @Size(max = 200) String titulo,
            @NotBlank @Size(max = 150) String autor,
            @Min(1450) @Max(2100) Integer anoPublicacao,
            @Min(0) int exemplares) {
    }

    public record LivroResposta(Long id, String isbn, String titulo, String autor,
                                Integer anoPublicacao, int exemplares) {

        public static LivroResposta de(Livro l) {
            return new LivroResposta(l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(),
                    l.getAnoPublicacao(), l.getExemplares());
        }
    }
}
