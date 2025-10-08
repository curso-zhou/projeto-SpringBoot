package br.pucpr.projeto.livros.dto;

import java.math.BigDecimal;

public record LivroResponse(Long id, String titulo, String autor, Long categoriaId, String categoriaNome, BigDecimal preco, String isbn) {}
