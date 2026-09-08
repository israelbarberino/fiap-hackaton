package br.com.fiap.susagenda.application.auth;

import br.com.fiap.susagenda.application.user.UserRepository;
import br.com.fiap.susagenda.domain.user.User;
import br.com.fiap.susagenda.domain.user.UserRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    @Inject
    public AuthService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public User register(String name, String cpf, String email, String password, UserRole role) {
        return userRepository.save(User.create(name, cpf, email, passwordService.hash(password), role));
    }

    public String login(String login, String password) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new AuthenticationException("Credenciais inválidas."));
        if (!passwordService.matches(password, user.passwordHash())) {
            throw new AuthenticationException("Credenciais inválidas.");
        }
        return jwtService.issue(user);
    }

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
