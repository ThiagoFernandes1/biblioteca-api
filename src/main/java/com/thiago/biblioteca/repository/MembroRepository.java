package com.thiago.biblioteca.repository;

import com.thiago.biblioteca.domain.Membro;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MembroRepository extends JpaRepository<Membro, Long> {

    boolean existsByEmailIgnoreCase(String email);

    /** Bloqueia o membro para que dois emprestimos simultaneos nao furem o limite dele. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Membro m where m.id = :id")
    Optional<Membro> findByIdParaEmprestimo(Long id);
}
