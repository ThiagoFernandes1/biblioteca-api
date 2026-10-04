package com.thiago.biblioteca.web;

import com.thiago.biblioteca.repository.LivroRepository;
import com.thiago.biblioteca.service.LivroService;
import com.thiago.biblioteca.web.dto.LivroDtos.AtualizaLivro;
import com.thiago.biblioteca.web.dto.LivroDtos.LivroResposta;
import com.thiago.biblioteca.web.dto.LivroDtos.NovoLivro;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService service;

    public LivroController(LivroService service) {
        this.service = service;
    }

    @GetMapping
    public Page<LivroResposta> buscar(@RequestParam(required = false) String busca,
                                      @PageableDefault(size = 20, sort = "titulo") Pageable pageable) {
        return service.buscar(busca, pageable).map(LivroResposta::de);
    }

    @GetMapping("/{id}")
    public LivroResposta buscarPorId(@PathVariable Long id) {
        return LivroResposta.de(service.buscarPorId(id));
    }

    @GetMapping("/disponibilidade")
    public List<LivroRepository.Disponibilidade> disponibilidade() {
        return service.disponibilidade();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivroResposta cadastrar(@Valid @RequestBody NovoLivro dados) {
        return LivroResposta.de(service.cadastrar(dados.paraEntidade()));
    }

    @PutMapping("/{id}")
    public LivroResposta atualizar(@PathVariable Long id, @Valid @RequestBody AtualizaLivro dados) {
        return LivroResposta.de(service.atualizar(id, dados.titulo().trim(), dados.autor().trim(),
                dados.anoPublicacao(), dados.exemplares()));
    }
}
