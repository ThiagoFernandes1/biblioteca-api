package com.thiago.biblioteca;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thiago.biblioteca.domain.Livro;
import com.thiago.biblioteca.service.ConflitoException;
import com.thiago.biblioteca.service.LivroService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo contra um SQL Server de verdade: aplica as migrations, valida
 * as entidades contra o schema (ddl-auto=validate) e exercita as regras pela API.
 *
 * So roda quando DB_URL esta definida, como no job de CI que sobe o SQL Server.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class FluxoSqlServerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired LivroService livros;

    @Test
    void cadastrosSimultaneosDoMesmoIsbnViramUmCriadoEOsOutrosConflito() throws Exception {
        String isbn = "97898" + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
        int tentativas = 6;
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(tentativas);
        try {
            List<Future<String>> resultados = new ArrayList<>();
            for (int i = 0; i < tentativas; i++) {
                resultados.add(pool.submit(() -> {
                    largada.await();
                    try {
                        livros.cadastrar(new Livro(isbn, "Concorrente", "Autor", 2020, 1));
                        return "criado";
                    } catch (ConflitoException e) {
                        return "conflito";
                    }
                }));
            }
            largada.countDown();

            List<String> obtidos = new ArrayList<>();
            for (Future<String> r : resultados) {
                obtidos.add(r.get());   // qualquer outra excecao (o antigo 500) falha o teste aqui
            }
            assertThat(obtidos).containsOnlyOnce("criado");
            assertThat(obtidos).filteredOn("conflito"::equals).hasSize(tentativas - 1);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void emprestaDevolveERespeitaODisponivel() throws Exception {
        // ISBN e e-mails unicos para o teste poder rodar varias vezes no mesmo banco
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        String isbn = "97899" + String.format("%08d", Math.abs(sufixo.hashCode()) % 100_000_000);

        long livro = criar("/api/livros", """
                {"isbn":"%s","titulo":"Livro de teste %s","autor":"Autora Teste","anoPublicacao":2020,"exemplares":1}
                """.formatted(isbn, sufixo));
        long ana = criar("/api/membros", """
                {"nome":"Ana Teste","email":"ana.%s@exemplo.com"}""".formatted(sufixo));
        long bruno = criar("/api/membros", """
                {"nome":"Bruno Teste","email":"bruno.%s@exemplo.com"}""".formatted(sufixo));

        // ISBN repetido
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content("""
                        {"isbn":"%s","titulo":"Outro","autor":"X","exemplares":1}""".formatted(isbn)))
                .andExpect(status().isConflict());

        long emprestimo = criar("/api/emprestimos", emprestimo(livro, ana));

        // o unico exemplar ja esta com a Ana
        mvc.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON).content(emprestimo(livro, bruno)))
                .andExpect(status().isUnprocessableEntity());

        // a view do banco enxerga o emprestimo em aberto
        JsonNode disponibilidade = json.readTree(mvc.perform(get("/api/livros/disponibilidade"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        JsonNode linha = null;
        for (JsonNode n : disponibilidade) {
            if (n.get("id").asLong() == livro) linha = n;
        }
        assertThat(linha).isNotNull();
        assertThat(linha.get("emprestados").asInt()).isEqualTo(1);
        assertThat(linha.get("disponiveis").asInt()).isZero();

        mvc.perform(post("/api/emprestimos/{id}/devolucao", emprestimo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.multa").value(0.0));

        mvc.perform(post("/api/emprestimos/{id}/devolucao", emprestimo))
                .andExpect(status().isUnprocessableEntity());

        // devolvido, o exemplar fica livre para o Bruno
        criar("/api/emprestimos", emprestimo(livro, bruno));

        mvc.perform(get("/api/membros/{id}/emprestimos", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    private static String emprestimo(long livro, long membro) {
        return "{\"livroId\":%d,\"membroId\":%d}".formatted(livro, membro);
    }

    private long criar(String url, String corpo) throws Exception {
        String resposta = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("id").asLong();
    }
}
