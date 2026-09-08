package br.com.fiap.susagenda.application.user;

import br.com.fiap.susagenda.domain.user.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(String id);

    Optional<User> findByLogin(String login);
}
