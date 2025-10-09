package br.pucpr.projeto.auth.dto;

public class UpdateProfileResponse {
    public Long id;
    public String nome;
    public String email;

    public UpdateProfileResponse() {}

    public UpdateProfileResponse(Long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }
}
