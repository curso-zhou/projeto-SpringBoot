package br.pucpr.projeto.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    // Campos opcionais: enviar null para não alterar
    @Size(max = 120)
    public String nome;

    @Email
    public String email;

    // Para alterar senha: enviar senhaAtual e novaSenha
    public String senhaAtual;

    @Size(min = 6)
    public String novaSenha;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String nome, String email, String senhaAtual, String novaSenha) {
        this.nome = nome;
        this.email = email;
        this.senhaAtual = senhaAtual;
        this.novaSenha = novaSenha;
    }
}
