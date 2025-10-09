package br.pucpr.projeto.auth.controller;

import br.pucpr.projeto.auth.dto.RegisterRequest;
import br.pucpr.projeto.auth.dto.RegisterResponse;
import br.pucpr.projeto.auth.dto.LoginRequest;
import br.pucpr.projeto.auth.dto.AuthTokenResponse;
import br.pucpr.projeto.auth.dto.UpdateProfileRequest;
import br.pucpr.projeto.auth.dto.UpdateProfileResponse;
import br.pucpr.projeto.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(java.util.Map.of(
                "email", user.getUsername(),
                "roles", user.getAuthorities().stream().map(a -> a.getAuthority()).toList()
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<UpdateProfileResponse> getProfile(@AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.status(401).build();
        var profile = userService.getProfileByEmail(user.getUsername());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UpdateProfileResponse> updateProfile(@AuthenticationPrincipal UserDetails user,
                                                               @Valid @RequestBody UpdateProfileRequest req) {
        if (user == null) return ResponseEntity.status(401).build();
        var profile = userService.updateProfile(user.getUsername(), req);
        return ResponseEntity.ok(profile);
    }
}
