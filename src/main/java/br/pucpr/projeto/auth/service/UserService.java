package br.pucpr.projeto.auth.service;

import br.pucpr.projeto.auth.dto.RegisterRequest;
import br.pucpr.projeto.auth.dto.RegisterResponse;
import br.pucpr.projeto.auth.dto.LoginRequest;
import br.pucpr.projeto.auth.dto.LoginResponse;
import br.pucpr.projeto.auth.model.User;
import br.pucpr.projeto.auth.repository.UserRepository;
import br.pucpr.projeto.auth.exception.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new IllegalArgumentException("Email já registrado");
        }
        String hash = passwordEncoder.encode(request.senha());
        User user = new User(request.nome(), request.email(), hash);
        userRepository.save(user);
        return new RegisterResponse(user.getId(), user.getNome(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.senha(), user.getSenhaHash())) {
            throw new InvalidCredentialsException();
        }
        return new LoginResponse(user.getId(), user.getNome(), user.getEmail());
    }
}
