package br.pucpr.projeto.auth.service;

import br.pucpr.projeto.auth.dto.RegisterRequest;
import br.pucpr.projeto.auth.dto.RegisterResponse;
import br.pucpr.projeto.auth.dto.LoginRequest;
import br.pucpr.projeto.auth.dto.LoginResponse;
import br.pucpr.projeto.auth.dto.UpdateProfileRequest;
import br.pucpr.projeto.auth.dto.UpdateProfileResponse;
import br.pucpr.projeto.auth.dto.AuthTokenResponse;
import br.pucpr.projeto.auth.model.User;
import br.pucpr.projeto.auth.repository.UserRepository;
import br.pucpr.projeto.auth.exception.InvalidCredentialsException;
import br.pucpr.projeto.core.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new IllegalArgumentException("Email já registrado");
        }
        String hash = passwordEncoder.encode(request.senha());
        User user = new User(request.nome(), request.email(), hash);
        // Se solicitado, torna o usuário também vendedor
        try {
            if (request.vendedor() != null && request.vendedor()) {
                user.getRoles().add("ROLE_VENDEDOR");
            }
        } catch (NoSuchMethodError | Exception e) {
            // caso o DTO não possua o campo (compatibilidade), ignora
        }
        userRepository.save(user);
        return new RegisterResponse(user.getId(), user.getNome(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.senha(), user.getSenhaHash())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtTokenProvider.generate(user.getId(), user.getEmail(), user.getRoles());
        return new AuthTokenResponse(user.getId(), user.getNome(), user.getEmail(), user.getRoles(), token);
    }

    // Método legacy se ainda for usado em testes
    @Transactional(readOnly = true)
    public LoginResponse legacyLogin(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.senha(), user.getSenhaHash())) {
            throw new InvalidCredentialsException();
        }
        return new LoginResponse(user.getId(), user.getNome(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public UpdateProfileResponse getProfileByEmail(String email) {
        var user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return new UpdateProfileResponse(user.getId(), user.getNome(), user.getEmail());
    }

    @Transactional
    public UpdateProfileResponse updateProfile(String currentEmail, UpdateProfileRequest req) {
        var user = userRepository.findByEmail(currentEmail.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Atualiza nome
        if (req.nome != null && !req.nome.isBlank()) {
            user.setNome(req.nome);
        }

        // Atualiza email, verificar conflito
        if (req.email != null && !req.email.isBlank()) {
            String newEmail = req.email.toLowerCase();
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email já em uso");
            }
            user.setEmail(newEmail);
        }

        // Atualiza senha se solicitado
        if (req.novaSenha != null && !req.novaSenha.isBlank()) {
            if (req.senhaAtual == null || !passwordEncoder.matches(req.senhaAtual, user.getSenhaHash())) {
                throw new IllegalArgumentException("Senha atual inválida");
            }
            user.setSenhaHash(passwordEncoder.encode(req.novaSenha));
        }

        userRepository.save(user);
        return new UpdateProfileResponse(user.getId(), user.getNome(), user.getEmail());
    }
}
