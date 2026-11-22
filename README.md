# Exercício 2 — Biblioteca

API Java 21 e Spring Boot para listar, cadastrar, consultar e atualizar os livros do acervo da CloudTech Solutions.

## 1. Executar

Na raiz deste repositório, use JDK 21 e Maven 3.9:

```powershell
mvn -B verify
mvn spring-boot:run
```

A API responde em `http://localhost:8081`. Para executar o JAR empacotado, use `java -jar target/biblioteca-1.0.0.jar`.

## 2. Endpoints

| Método | Caminho | Resultado esperado |
| --- | --- | --- |
| GET | `/` | 200, mensagem de boas-vindas |
| GET | `/livros` | 200, lista do acervo, incluindo livros emprestados |
| POST | `/livros/cadastrar` | 201, livro cadastrado com o ID gerado |
| PUT | `/livros/alterar/{id}` | 200, livro atualizado com o mesmo ID |
| GET | `/livros/{id}` | 200, dados do livro, ou 404 para ID inexistente |
| GET | `/documentacao` | 200, lista dos endpoints |

O POST e o PUT recebem `titulo`, `autor`, `editora`, `ano`, `categoria` e `status` em JSON. Informe `Disponível` ou `Emprestado` no campo `status`. Dados inválidos retornam 400 e o PUT retorna 404 quando o livro não existe.

Modelos ficam em `models`, rotas em `controller` e o gerenciamento do acervo em `service`. A aplicação lê `data/biblioteca.json` na inicialização e grava o arquivo após cadastros e alterações. Os dados e os IDs são preservados ao reiniciar com o mesmo arquivo. Configure `APP_DATA_FILE` para escolher outro caminho.

## 3. Testes

`mvn -B verify` executa o JUnit 5 com MockMvc e gera o JAR. O relatório da execução fica em `target/surefire-reports/`. Os testes usam um arquivo temporário próprio.

Siga [tests/requisicoes.http](tests/requisicoes.http) para reproduzir as chamadas no Thunder Client, incluindo a consulta que retorna 404. Use **Body → JSON** para o POST e o PUT e substitua o ID de exemplo pelo ID do cadastro. Capture método, URL, código HTTP e resposta. Repita as chamadas com o domínio da API publicada.

## 4. GitHub Actions e Railway

O [workflow](.github/workflows/pipeline.yml) roda no GitHub em push e pull request. Ele prepara o Java 21, executa `mvn -B verify` e disponibiliza o JAR e os XMLs do Surefire como artefatos.

Para testar o contêiner localmente:

```powershell
docker build -t cloudtech-biblioteca .
docker run --rm -p 8081:8081 cloudtech-biblioteca
```

Na Railway, conecte este repositório à branch `main` e ative **Wait for CI** para o deploy automático aguardar os testes. A API usa a variável `PORT` e assume `8081` em execução local. Gere um domínio público e valide `/`, `/documentacao` e as rotas de livros. Se precisar preservar dados entre redeploys, use uma única instância com um volume em `/app/data`.

Use o [relatório](RELATORIO.md) para registrar as evidências da entrega.
