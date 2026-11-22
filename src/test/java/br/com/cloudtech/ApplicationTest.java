package br.com.cloudtech;

import br.com.cloudtech.service.BibliotecaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTest {
    private static final String LIVRO = """
            {"titulo":"Banco de Dados para Iniciantes","autor":"Maria Souza","editora":"EduTech",
             "ano":2023,"categoria":"Informática","status":"Disponível"}
            """;
    private static final String ATUALIZADO = """
            {"titulo":"Banco de Dados Essencial","autor":"Maria Souza","editora":"EduTech",
             "ano":2024,"categoria":"Banco de Dados","status":"Emprestado"}
            """;
    private static final long ID_INEXISTENTE = 999999;

    @TempDir
    static Path pastaDeDados;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @DynamicPropertySource
    static void configurarArquivo(DynamicPropertyRegistry propriedades) {
        propriedades.add("app.data-file", () -> arquivoDeDados().toString());
    }

    @Test
    void exibePaginaInicial() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value(
                        "Bem-vindo ao sistema de gerenciamento da Biblioteca CloudTech Solutions"));
    }

    @Test
    void listaLivros() throws Exception {
        long id = cadastrarLivro();
        mvc.perform(get("/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].titulo").value(
                        contains("Banco de Dados para Iniciantes")));
    }

    @Test
    void cadastraLivro() throws Exception {
        cadastrar(LIVRO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Banco de Dados para Iniciantes"))
                .andExpect(jsonPath("$.status").value("Disponível"));
    }

    @Test
    void consultaLivroPorId() throws Exception {
        long id = cadastrarLivro();
        mvc.perform(get("/livros/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.ano").value(2023))
                .andExpect(jsonPath("$.autor").value("Maria Souza"));
    }

    @Test
    void alteraLivro() throws Exception {
        long id = cadastrarLivro();
        alterar(id, ATUALIZADO)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("Emprestado"));
        mvc.perform(get("/livros/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Banco de Dados Essencial"))
                .andExpect(jsonPath("$.ano").value(2024));
    }

    @Test
    void retorna404ParaLivroInexistente() throws Exception {
        mvc.perform(get("/livros/{id}", ID_INEXISTENTE)).andExpect(status().isNotFound());
        alterar(ID_INEXISTENTE, ATUALIZADO).andExpect(status().isNotFound());
    }

    @Test
    void documentaEndpoints() throws Exception {
        mvc.perform(get("/documentacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endpoints").value(containsInAnyOrder(
                        "/", "/livros", "/livros/cadastrar", "/livros/alterar/{id}", "/livros/{id}", "/documentacao")));
    }

    @Test
    void rejeitaCamposAusentesEStatusInvalido() throws Exception {
        cadastrar("{}").andExpect(status().isBadRequest());
        cadastrar(LIVRO.replace("Disponível", "Vendido")).andExpect(status().isBadRequest());
        alterar(cadastrarLivro(), ATUALIZADO.replace("Emprestado", "Vendido"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recuperaLivroAlteradoDoArquivo() throws Exception {
        long id = cadastrarLivro();
        alterar(id, ATUALIZADO).andExpect(status().isOk());

        var reaberta = new BibliotecaService(json, arquivoDeDados().toString());

        assertEquals("Emprestado", reaberta.buscar(id).status());
        assertEquals(2024, reaberta.buscar(id).ano());
    }

    private long cadastrarLivro() throws Exception {
        byte[] resposta = cadastrar(LIVRO).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsByteArray();
        return json.readTree(resposta).get("id").asLong();
    }

    private ResultActions cadastrar(String corpo) throws Exception {
        return mvc.perform(post("/livros/cadastrar").contentType(MediaType.APPLICATION_JSON)
                .content(corpo.getBytes(StandardCharsets.UTF_8)));
    }

    private ResultActions alterar(long id, String corpo) throws Exception {
        return mvc.perform(put("/livros/alterar/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo.getBytes(StandardCharsets.UTF_8)));
    }

    private static Path arquivoDeDados() {
        return pastaDeDados.resolve("biblioteca.json");
    }
}
