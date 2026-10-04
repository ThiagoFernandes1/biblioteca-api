package com.thiago.biblioteca.web;

import com.thiago.biblioteca.service.EmprestimoService;
import com.thiago.biblioteca.service.MembroService;
import com.thiago.biblioteca.web.dto.EmprestimoDtos.EmprestimoResposta;
import com.thiago.biblioteca.web.dto.MembroDtos.MembroResposta;
import com.thiago.biblioteca.web.dto.MembroDtos.NovoMembro;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/membros")
public class MembroController {

    private final MembroService membros;
    private final EmprestimoService emprestimos;

    public MembroController(MembroService membros, EmprestimoService emprestimos) {
        this.membros = membros;
        this.emprestimos = emprestimos;
    }

    @GetMapping
    public Page<MembroResposta> listar(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return membros.listar(pageable).map(MembroResposta::de);
    }

    @GetMapping("/{id}")
    public MembroResposta buscarPorId(@PathVariable Long id) {
        return MembroResposta.de(membros.buscarPorId(id));
    }

    @GetMapping("/{id}/emprestimos")
    public List<EmprestimoResposta> historico(@PathVariable Long id) {
        return emprestimos.historicoDoMembro(id).stream().map(EmprestimoResposta::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembroResposta cadastrar(@Valid @RequestBody NovoMembro dados) {
        return MembroResposta.de(membros.cadastrar(dados.nome().trim(), dados.email().trim().toLowerCase()));
    }

    @PatchMapping("/{id}/desativar")
    public MembroResposta desativar(@PathVariable Long id) {
        return MembroResposta.de(membros.desativar(id));
    }
}
