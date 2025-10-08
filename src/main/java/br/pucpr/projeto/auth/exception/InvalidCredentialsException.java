package br.pucpr.projeto.auth.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("Credenciais inválidas"); }
}
