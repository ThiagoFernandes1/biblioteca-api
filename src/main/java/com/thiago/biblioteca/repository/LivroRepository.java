package com.thiago.biblioteca.repository;

import com.thiago.biblioteca.domain.Livro;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    boolean existsByIsbn(String isbn);

    Page<Livro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(
            String titulo, String autor, Pageable pageable);

    /**
     * Le o livro com bloqueio de escrita (UPDLOCK no SQL Server) ate o fim da
     * transacao, para que dois emprestimos simultaneos do ultimo exemplar nao
     * passem ambos pela checagem de disponibilidade.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Livro l where l.id = :id")
    Optional<Livro> findByIdParaEmprestimo(Long id);

    /** Le a view dbo.vw_disponibilidade_livro, que faz a conta no proprio banco. */
    @Query(value = """
            SELECT id, isbn, titulo, exemplares, emprestados, disponiveis
            FROM   dbo.vw_disponibilidade_livro
            ORDER BY titulo
            """, nativeQuery = true)
    List<Disponibilidade> listarDisponibilidade();

    interface Disponibilidade {
        Long getId();
        String getIsbn();
        String getTitulo();
        Integer getExemplares();
        Integer getEmprestados();
        Integer getDisponiveis();
    }
}
