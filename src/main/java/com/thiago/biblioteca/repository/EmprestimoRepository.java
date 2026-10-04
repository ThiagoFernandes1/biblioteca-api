package com.thiago.biblioteca.repository;

import com.thiago.biblioteca.domain.Emprestimo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    long countByLivroIdAndDataDevolucaoIsNull(Long livroId);

    long countByMembroIdAndDataDevolucaoIsNull(Long membroId);

    boolean existsByMembroIdAndDataDevolucaoIsNullAndDataPrevistaBefore(Long membroId, LocalDate hoje);

    @EntityGraph(attributePaths = {"livro", "membro"})
    Optional<Emprestimo> findWithLivroAndMembroById(Long id);

    @EntityGraph(attributePaths = {"livro", "membro"})
    List<Emprestimo> findByDataDevolucaoIsNullAndDataPrevistaBeforeOrderByDataPrevista(LocalDate hoje);

    @EntityGraph(attributePaths = {"livro", "membro"})
    List<Emprestimo> findByMembroIdOrderByDataEmprestimoDesc(Long membroId);
}
