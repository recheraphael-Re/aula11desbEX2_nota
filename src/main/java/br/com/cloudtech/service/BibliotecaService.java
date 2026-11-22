package br.com.cloudtech.service;

import br.com.cloudtech.models.Livro;
import br.com.cloudtech.storage.ArquivoJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BibliotecaService {
    private final ArquivoJson<ListaLivros> arquivo;
    private ListaLivros dados;

    public BibliotecaService(ObjectMapper json, @Value("${app.data-file}") String caminho) {
        arquivo = new ArquivoJson<>(json, caminho, ListaLivros.class);
        dados = arquivo.ler(new ListaLivros(0, List.of()));
    }

    public synchronized List<Livro> listar() {
        return dados.livros();
    }

    public synchronized Livro buscar(long id) {
        return dados.livros().get(indiceLivro(id));
    }

    public synchronized Livro cadastrar(Livro.Dados entrada) {
        Livro livro = entrada.comId(dados.ultimoId() + 1);
        var livros = new ArrayList<>(dados.livros());
        livros.add(livro);
        salvar(new ListaLivros(livro.id(), livros));
        return livro;
    }

    public synchronized Livro alterar(long id, Livro.Dados entrada) {
        var livros = new ArrayList<>(dados.livros());
        Livro atualizado = entrada.comId(id);
        livros.set(indiceLivro(id), atualizado);
        salvar(new ListaLivros(dados.ultimoId(), livros));
        return atualizado;
    }

    private int indiceLivro(long id) {
        List<Livro> livros = dados.livros();
        for (int i = 0; i < livros.size(); i++) {
            if (livros.get(i).id() == id) {
                return i;
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado");
    }

    private void salvar(ListaLivros atualizados) {
        arquivo.gravar(atualizados);
        dados = atualizados;
    }

    public record ListaLivros(long ultimoId, List<Livro> livros) {
        public ListaLivros {
            livros = List.copyOf(livros);
        }
    }
}
