package com.campus.aihelp.service;

import com.campus.aihelp.domain.User;
import com.campus.aihelp.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserMapper userMapper;

    public CurrentUserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        User user = userMapper.findByUsername(auth.getName());
        if (user != null) {
            user.setRoles(userMapper.findRoleCodes(user.getId()));
        }
        return user;
    }
}
