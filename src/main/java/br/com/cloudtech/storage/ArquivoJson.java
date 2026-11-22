package br.com.cloudtech.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ArquivoJson<T> {
    private final ObjectMapper json;
    private final Path arquivo;
    private final Class<T> tipo;

    public ArquivoJson(ObjectMapper json, String caminho, Class<T> tipo) {
        this.json = json;
        this.arquivo = Path.of(caminho).toAbsolutePath().normalize();
        this.tipo = tipo;
    }

    public T ler(T inicial) {
        if (Files.notExists(arquivo)) {
            return inicial;
        }
        try {
            T dados = json.readValue(arquivo.toFile(), tipo);
            if (dados == null) {
                throw new IOException("O arquivo JSON não pode conter apenas null.");
            }
            return dados;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler " + arquivo, e);
        }
    }

    public void gravar(T dados) {
        try {
            Path pasta = arquivo.getParent();
            Files.createDirectories(pasta);
            Path temporario = Files.createTempFile(pasta, "cloudtech-", ".tmp");
            try {
                json.writerWithDefaultPrettyPrinter().writeValue(temporario.toFile(), dados);
                try {
                    Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporario);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível gravar " + arquivo, e);
        }
    }
}
