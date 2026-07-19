package com.apple.inc.user.repository.jpa;

import com.apple.inc.user.entities.r2dbc.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
