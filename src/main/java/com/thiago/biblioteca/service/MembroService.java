package com.thiago.biblioteca.service;

import com.thiago.biblioteca.domain.Membro;
import com.thiago.biblioteca.repository.MembroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembroService {

    private final MembroRepository membros;

    public MembroService(MembroRepository membros) {
        this.membros = membros;
    }

    @Transactional(readOnly = true)
    public Page<Membro> listar(Pageable pageable) {
        return membros.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Membro buscarPorId(Long id) {
        return membros.findById(id).orElseThrow(() -> new NaoEncontradoException("Membro", id));
    }

    @Transactional
    public Membro cadastrar(String nome, String email) {
        if (membros.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Ja existe um membro com o e-mail " + email + ".");
        }
        return membros.save(new Membro(nome, email));
    }

    /** Desativar em vez de excluir preserva o historico de emprestimos do membro. */
    @Transactional
    public Membro desativar(Long id) {
        Membro membro = buscarPorId(id);
        membro.desativar();
        return membro;
    }
}
