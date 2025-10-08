package br.pucpr.projeto.auth.terminal;

import br.pucpr.projeto.auth.dto.LoginRequest;
import br.pucpr.projeto.auth.dto.LoginResponse;
import br.pucpr.projeto.auth.dto.RegisterRequest;
import br.pucpr.projeto.auth.dto.RegisterResponse;
import br.pucpr.projeto.auth.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Terminal simples: execute a aplicação com o argumento --terminal para habilitar.
 * Comandos:
 *   register "Nome" email senha
 *   login email senha
 *   exit
 */
@Component
public class AuthTerminalRunner implements CommandLineRunner {

    private final UserService userService;

    public AuthTerminalRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        boolean enabled = Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("--terminal") || a.equalsIgnoreCase("terminal") || a.equalsIgnoreCase("--terminal=true"))
                || "true".equalsIgnoreCase(System.getProperty("terminal.enabled"))
                || "true".equalsIgnoreCase(System.getenv("TERMINAL"));

        if (!enabled) {
            System.out.println("[AuthTerminal] (inativo) Passe --terminal ou -Dterminal.enabled=true ou defina env TERMINAL=true para ativar o modo interativo.");
            return; // não ativa terminal
        }
        System.out.println("[AuthTerminal] MODO INTERATIVO ATIVO. Comandos: register | login | exit");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Encerrando terminal...");
                    break;
                }
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                try {
                    switch (parts[0].toLowerCase()) {
                        case "register" -> handleRegister(parts);
                        case "login" -> handleLogin(parts);
                        default -> System.out.println("Comando desconhecido: " + parts[0]);
                    }
                } catch (Exception e) {
                    System.out.println("Erro: " + e.getMessage());
                }
            }
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 4) {
            System.out.println("Uso: register Nome_Completo email senha  (Se nome tiver espaços, apenas escreva normalmente: register João da Silva email senha)");
            return;
        }
        // Estratégia: últimos dois tokens = email e senha; o restante (1 .. len-3) é o nome
        String email = parts[parts.length - 2];
        String senha = parts[parts.length - 1];
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < parts.length - 2; i++) {
            if (i > 1) sb.append(' ');
            sb.append(parts[i].replace('"',' ').trim());
        }
        String nome = sb.toString().trim();
        if (nome.isEmpty()) {
            System.out.println("Nome inválido");
            return;
        }
        RegisterResponse resp = userService.register(new RegisterRequest(nome, email, senha));
        System.out.printf("[OK] Usuário registrado id=%d email=%s%n", resp.id(), resp.email());
    }

    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Uso: login email senha");
            return;
        }
        String email = parts[1];
        String senha = parts[2];
        LoginResponse resp = userService.login(new LoginRequest(email, senha));
        System.out.printf("[OK] Login bem-sucedido id=%d nome=%s email=%s%n", resp.id(), resp.nome(), resp.email());
    }
}
