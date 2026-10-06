package com.thiago.biblioteca.web;

import com.thiago.biblioteca.domain.Emprestimo;
import com.thiago.biblioteca.domain.Livro;
import com.thiago.biblioteca.domain.Membro;
import com.thiago.biblioteca.service.EmprestimoService;
import com.thiago.biblioteca.service.NaoEncontradoException;
import com.thiago.biblioteca.service.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmprestimoController.class)
class EmprestimoControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean EmprestimoService service;

    @Test
    void emprestimoCriadoDevolve201() throws Exception {
        Livro livro = new Livro("9780000000001", "Dom Casmurro", "Machado de Assis", 1899, 1);
        Membro membro = new Membro("Ana", "ana@exemplo.com");
        LocalDate hoje = LocalDate.of(2026, 10, 4);
        when(service.emprestar(1L, 2L)).thenReturn(new Emprestimo(livro, membro, hoje, hoje.plusDays(14)));

        mvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"livroId\":1,\"membroId\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.dataPrevista").value("2026-10-18"));
    }

    @Test
    void regraDeNegocioViraStatus422() throws Exception {
        when(service.emprestar(1L, 2L)).thenThrow(new RegraNegocioException("Nenhum exemplar disponivel."));

        mvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"livroId\":1,\"membroId\":2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Nenhum exemplar disponivel."));
    }

    @Test
    void emprestimoInexistenteViraStatus404() throws Exception {
        when(service.devolver(7L)).thenThrow(new NaoEncontradoException("Emprestimo", 7L));

        mvc.perform(post("/api/emprestimos/7/devolucao"))
                .andExpect(status().isNotFound());
    }

    @Test
    void corpoSemCamposObrigatoriosViraStatus400() throws Exception {
        mvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.livroId").exists())
                .andExpect(jsonPath("$.campos.membroId").exists());
    }

    @Test
    void idQueNaoENumeroDizOQueEstaErrado() throws Exception {
        mvc.perform(get("/api/emprestimos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("'id' recebeu \"abc\", mas espera um numero inteiro."));
    }

    @Test
    void jsonQuebradoSaiEmProblemJson() throws Exception {
        mvc.perform(post("/api/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"livroId\":1,"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void metodoNaoSuportadoSaiEmProblemJson() throws Exception {
        mvc.perform(delete("/api/emprestimos/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
