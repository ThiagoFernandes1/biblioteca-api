package com.thiago.biblioteca.web;

import com.thiago.biblioteca.service.EmprestimoService;
import com.thiago.biblioteca.web.dto.EmprestimoDtos.EmprestimoResposta;
import com.thiago.biblioteca.web.dto.EmprestimoDtos.NovoEmprestimo;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/emprestimos")
public class EmprestimoController {

    private final EmprestimoService service;

    public EmprestimoController(EmprestimoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmprestimoResposta emprestar(@Valid @RequestBody NovoEmprestimo dados) {
        return EmprestimoResposta.de(service.emprestar(dados.livroId(), dados.membroId()));
    }

    @PostMapping("/{id}/devolucao")
    public EmprestimoResposta devolver(@PathVariable Long id) {
        return EmprestimoResposta.de(service.devolver(id));
    }

    @GetMapping("/{id}")
    public EmprestimoResposta buscarPorId(@PathVariable Long id) {
        return EmprestimoResposta.de(service.buscarPorId(id));
    }

    @GetMapping("/atrasados")
    public List<EmprestimoResposta> atrasados() {
        return service.atrasados().stream().map(EmprestimoResposta::de).toList();
    }
}
