package br.pucpr.projeto.livros.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record LivroRequest(
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank @Size(max = 120) String autor,
        @NotNull Long categoriaId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal preco,
        @Size(max = 13) String isbn
) {}
