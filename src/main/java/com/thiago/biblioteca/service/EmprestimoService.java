package com.thiago.biblioteca.service;

import com.thiago.biblioteca.config.RegrasEmprestimo;
import com.thiago.biblioteca.domain.Emprestimo;
import com.thiago.biblioteca.domain.Livro;
import com.thiago.biblioteca.domain.Membro;
import com.thiago.biblioteca.repository.EmprestimoRepository;
import com.thiago.biblioteca.repository.LivroRepository;
import com.thiago.biblioteca.repository.MembroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimos;
    private final LivroRepository livros;
    private final MembroRepository membros;
    private final RegrasEmprestimo regras;
    private final Clock clock;

    public EmprestimoService(EmprestimoRepository emprestimos, LivroRepository livros,
                             MembroRepository membros, RegrasEmprestimo regras, Clock clock) {
        this.emprestimos = emprestimos;
        this.livros = livros;
        this.membros = membros;
        this.regras = regras;
        this.clock = clock;
    }

    /**
     * Empresta um exemplar do livro ao membro.
     *
     * Membro e livro sao lidos com bloqueio, sempre nessa ordem, entao duas
     * requisicoes simultaneas esperam uma pela outra em vez de furarem o
     * limite do membro ou emprestarem o mesmo ultimo exemplar duas vezes.
     */
    @Transactional
    public Emprestimo emprestar(Long livroId, Long membroId) {
        Membro membro = membros.findByIdParaEmprestimo(membroId)
                .orElseThrow(() -> new NaoEncontradoException("Membro", membroId));
        if (!membro.isAtivo()) {
            throw new RegraNegocioException("Membro inativo nao pode pegar livros emprestados.");
        }

        LocalDate hoje = LocalDate.now(clock);
        if (emprestimos.existsByMembroIdAndDataDevolucaoIsNullAndDataPrevistaBefore(membroId, hoje)) {
            throw new RegraNegocioException("Membro tem emprestimo em atraso; devolva antes de pegar outro.");
        }
        if (emprestimos.countByMembroIdAndDataDevolucaoIsNull(membroId) >= regras.limiteEmprestimos()) {
            throw new RegraNegocioException("Membro ja esta com " + regras.limiteEmprestimos()
                    + " livros, o limite de emprestimos simultaneos.");
        }

        Livro livro = livros.findByIdParaEmprestimo(livroId)
                .orElseThrow(() -> new NaoEncontradoException("Livro", livroId));
        if (emprestimos.countByLivroIdAndDataDevolucaoIsNull(livroId) >= livro.getExemplares()) {
            throw new RegraNegocioException("Nenhum exemplar de \"" + livro.getTitulo() + "\" disponivel no momento.");
        }

        return emprestimos.save(new Emprestimo(livro, membro, hoje, hoje.plusDays(regras.prazoDias())));
    }

    /** Registra a devolucao e calcula a multa pelos dias de atraso, se houver. */
    @Transactional
    public Emprestimo devolver(Long emprestimoId) {
        Emprestimo emprestimo = buscarPorId(emprestimoId);
        if (emprestimo.isDevolvido()) {
            throw new RegraNegocioException("Emprestimo ja devolvido em " + emprestimo.getDataDevolucao() + ".");
        }

        LocalDate hoje = LocalDate.now(clock);
        emprestimo.registrarDevolucao(hoje, calcularMulta(emprestimo.getDataPrevista(), hoje));
        return emprestimo;
    }

    BigDecimal calcularMulta(LocalDate prevista, LocalDate devolucao) {
        long diasAtraso = ChronoUnit.DAYS.between(prevista, devolucao);
        if (diasAtraso <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return regras.multaPorDia().multiply(BigDecimal.valueOf(diasAtraso)).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public Emprestimo buscarPorId(Long id) {
        return emprestimos.findWithLivroAndMembroById(id)
                .orElseThrow(() -> new NaoEncontradoException("Emprestimo", id));
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> atrasados() {
        return emprestimos.findByDataDevolucaoIsNullAndDataPrevistaBeforeOrderByDataPrevista(LocalDate.now(clock));
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> historicoDoMembro(Long membroId) {
        if (!membros.existsById(membroId)) {
            throw new NaoEncontradoException("Membro", membroId);
        }
        return emprestimos.findByMembroIdOrderByDataEmprestimoDesc(membroId);
    }
}
