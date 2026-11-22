package br.com.cloudtech.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record Livro(long id, String titulo, String autor, String editora,
                    int ano, String categoria, String status) {
    public record Dados(@NotBlank String titulo, @NotBlank String autor,
                        @NotBlank String editora, @NotNull @Min(1) @Max(9999) Integer ano,
                        @NotBlank String categoria, @NotBlank @Pattern(regexp = "Disponível|Emprestado",
                        message = "deve ser Disponível ou Emprestado") String status) {
        public Livro comId(long id) {
            return new Livro(id, titulo, autor, editora, ano, categoria, status);
        }
    }
}
