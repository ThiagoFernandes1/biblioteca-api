package com.thiago.biblioteca.service;

import com.thiago.biblioteca.domain.Livro;
import com.thiago.biblioteca.repository.EmprestimoRepository;
import com.thiago.biblioteca.repository.LivroRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LivroService {

    private final LivroRepository livros;
    private final EmprestimoRepository emprestimos;

    public LivroService(LivroRepository livros, EmprestimoRepository emprestimos) {
        this.livros = livros;
        this.emprestimos = emprestimos;
    }

    @Transactional(readOnly = true)
    public Page<Livro> buscar(String termo, Pageable pageable) {
        if (termo == null || termo.isBlank()) {
            return livros.findAll(pageable);
        }
        return livros.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(termo, termo, pageable);
    }

    @Transactional(readOnly = true)
    public Livro buscarPorId(Long id) {
        return livros.findById(id).orElseThrow(() -> new NaoEncontradoException("Livro", id));
    }

    @Transactional(readOnly = true)
    public List<LivroRepository.Disponibilidade> disponibilidade() {
        return livros.listarDisponibilidade();
    }

    @Transactional
    public Livro cadastrar(Livro livro) {
        String duplicado = "Ja existe um livro com o ISBN " + livro.getIsbn() + ".";
        if (livros.existsByIsbn(livro.getIsbn())) {
            throw new ConflitoException(duplicado);
        }
        try {
            return livros.saveAndFlush(livro);
        } catch (DataIntegrityViolationException e) {
            // dois cadastros simultaneos passam juntos pela checagem acima; a UNIQUE do banco barra o segundo
            throw new ConflitoException(duplicado);
        }
    }

    @Transactional
    public Livro atualizar(Long id, String titulo, String autor, Integer ano, int exemplares) {
        Livro livro = livros.findByIdParaEmprestimo(id)
                .orElseThrow(() -> new NaoEncontradoException("Livro", id));

        long emprestados = emprestimos.countByLivroIdAndDataDevolucaoIsNull(id);
        if (exemplares < emprestados) {
            throw new RegraNegocioException("O livro tem " + emprestados
                    + " exemplar(es) emprestado(s); nao da para reduzir o acervo para " + exemplares + ".");
        }

        livro.atualizar(titulo, autor, ano, exemplares);
        return livro;
    }
}
