package com.jemini.stayhost.user.domain.component;

import com.jemini.stayhost.user.domain.model.User;

public interface UserReader {

    User getById(Long id);

    User getByEmail(String email);

    boolean existsByEmail(String email);
}
