package com.jemini.stayhost.user.infrastructure.component;

import com.jemini.stayhost.user.domain.component.UserManager;
import com.jemini.stayhost.user.domain.model.User;
import com.jemini.stayhost.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserManagerImpl implements UserManager {

    private final UserRepository userRepository;

    @Override
    public User save(final User user) {
        return userRepository.save(user);
    }
}
