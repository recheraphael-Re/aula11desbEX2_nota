package br.com.cloudtech.controller;

import br.com.cloudtech.models.Livro;
import br.com.cloudtech.service.BibliotecaService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LivroController {
    private final BibliotecaService biblioteca;

    public LivroController(BibliotecaService biblioteca) {
        this.biblioteca = biblioteca;
    }

    @GetMapping("/")
    public Map<String, String> inicio() {
        return Map.of("mensagem", "Bem-vindo ao sistema de gerenciamento da Biblioteca CloudTech Solutions");
    }

    @GetMapping("/livros")
    public List<Livro> listar() {
        return biblioteca.listar();
    }

    @GetMapping("/livros/{id}")
    public Livro buscar(@PathVariable long id) {
        return biblioteca.buscar(id);
    }

    @PostMapping("/livros/cadastrar")
    @ResponseStatus(HttpStatus.CREATED)
    public Livro cadastrar(@Valid @RequestBody Livro.Dados dados) {
        return biblioteca.cadastrar(dados);
    }

    @PutMapping("/livros/alterar/{id}")
    public Livro alterar(@PathVariable long id, @Valid @RequestBody Livro.Dados dados) {
        return biblioteca.alterar(id, dados);
    }

    @GetMapping("/documentacao")
    public Map<String, List<String>> documentacao() {
        return Map.of("endpoints", List.of("/", "/livros", "/livros/cadastrar",
                "/livros/alterar/{id}", "/livros/{id}", "/documentacao"));
    }
}
