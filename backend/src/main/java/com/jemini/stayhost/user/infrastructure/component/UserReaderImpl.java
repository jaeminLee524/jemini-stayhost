package com.jemini.stayhost.user.infrastructure.component;

import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.exception.NotFoundException;
import com.jemini.stayhost.user.domain.component.UserReader;
import com.jemini.stayhost.user.domain.model.User;
import com.jemini.stayhost.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReaderImpl implements UserReader {

    private final UserRepository userRepository;

    @Override
    public User getById(final Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User getByEmail(final String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public boolean existsByEmail(final String email) {
        return userRepository.existsByEmail(email);
    }
}
