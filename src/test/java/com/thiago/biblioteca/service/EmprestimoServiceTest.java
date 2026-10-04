package com.thiago.biblioteca.service;

import com.thiago.biblioteca.config.RegrasEmprestimo;
import com.thiago.biblioteca.domain.Emprestimo;
import com.thiago.biblioteca.domain.Livro;
import com.thiago.biblioteca.domain.Membro;
import com.thiago.biblioteca.repository.EmprestimoRepository;
import com.thiago.biblioteca.repository.LivroRepository;
import com.thiago.biblioteca.repository.MembroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 4);

    @Mock EmprestimoRepository emprestimos;
    @Mock LivroRepository livros;
    @Mock MembroRepository membros;

    EmprestimoService service;
    Livro livro;
    Membro membro;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(HOJE.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        RegrasEmprestimo regras = new RegrasEmprestimo(14, 3, new BigDecimal("2.00"));
        service = new EmprestimoService(emprestimos, livros, membros, regras, clock);

        livro = new Livro("9780000000001", "Dom Casmurro", "Machado de Assis", 1899, 2);
        membro = new Membro("Ana", "ana@exemplo.com");
    }

    @Nested
    class Emprestar {

        @BeforeEach
        void membroELivroExistem() {
            when(membros.findByIdParaEmprestimo(1L)).thenReturn(Optional.of(membro));
        }

        private void livroDisponivel() {
            when(livros.findByIdParaEmprestimo(10L)).thenReturn(Optional.of(livro));
            when(emprestimos.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        void criaEmprestimoComPrazoDeQuatorzeDias() {
            livroDisponivel();

            Emprestimo e = service.emprestar(10L, 1L);

            assertThat(e.getDataEmprestimo()).isEqualTo(HOJE);
            assertThat(e.getDataPrevista()).isEqualTo(HOJE.plusDays(14));
            assertThat(e.getLivro()).isSameAs(livro);
            assertThat(e.getMembro()).isSameAs(membro);
        }

        @Test
        void recusaMembroInativo() {
            membro.desativar();

            assertThatThrownBy(() -> service.emprestar(10L, 1L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("inativo");
            verify(emprestimos, never()).save(any());
        }

        @Test
        void recusaMembroComEmprestimoAtrasado() {
            when(emprestimos.existsByMembroIdAndDataDevolucaoIsNullAndDataPrevistaBefore(1L, HOJE)).thenReturn(true);

            assertThatThrownBy(() -> service.emprestar(10L, 1L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("atraso");
        }

        @Test
        void recusaQuandoMembroAtingiuOLimite() {
            when(emprestimos.countByMembroIdAndDataDevolucaoIsNull(1L)).thenReturn(3L);

            assertThatThrownBy(() -> service.emprestar(10L, 1L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("limite");
        }

        @Test
        void recusaQuandoTodosOsExemplaresEstaoEmprestados() {
            when(livros.findByIdParaEmprestimo(10L)).thenReturn(Optional.of(livro));
            when(emprestimos.countByLivroIdAndDataDevolucaoIsNull(10L)).thenReturn(2L);

            assertThatThrownBy(() -> service.emprestar(10L, 1L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("Nenhum exemplar");
        }

        @Test
        void livroInexistente() {
            when(livros.findByIdParaEmprestimo(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.emprestar(10L, 1L))
                    .isInstanceOf(NaoEncontradoException.class);
        }
    }

    @Test
    void membroInexistente() {
        when(membros.findByIdParaEmprestimo(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.emprestar(10L, 99L))
                .isInstanceOf(NaoEncontradoException.class);
    }

    @Nested
    class Devolver {

        private Emprestimo emprestimoComPrevisao(LocalDate prevista) {
            Emprestimo e = new Emprestimo(livro, membro, prevista.minusDays(14), prevista);
            when(emprestimos.findWithLivroAndMembroById(5L)).thenReturn(Optional.of(e));
            return e;
        }

        @Test
        void noPrazoNaoCobraMulta() {
            emprestimoComPrevisao(HOJE);

            Emprestimo e = service.devolver(5L);

            assertThat(e.getDataDevolucao()).isEqualTo(HOJE);
            assertThat(e.getMulta()).isEqualByComparingTo("0");
        }

        @Test
        void cobraMultaPorDiaDeAtraso() {
            emprestimoComPrevisao(HOJE.minusDays(5));

            Emprestimo e = service.devolver(5L);

            assertThat(e.getMulta()).isEqualByComparingTo("10.00");
        }

        @Test
        void naoDeixaDevolverDuasVezes() {
            emprestimoComPrevisao(HOJE).registrarDevolucao(HOJE, BigDecimal.ZERO);

            assertThatThrownBy(() -> service.devolver(5L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("ja devolvido");
        }
    }
}
