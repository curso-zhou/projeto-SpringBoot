package br.pucpr.projeto.auth.dto;

import java.util.Set;

public record AuthTokenResponse(Long id, String nome, String email, Set<String> roles, String token) {}
