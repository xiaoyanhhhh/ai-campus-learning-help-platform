package com.campus.aihelp.controller;

import com.campus.aihelp.aop.AuditLog;
import com.campus.aihelp.domain.User;
import com.campus.aihelp.mapper.UserMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    @AuditLog("用户注册")
    public String register(@RequestParam @NotBlank String username,
                           @RequestParam @NotBlank String realName,
                           @RequestParam(required = false) String studentNo,
                           @RequestParam(required = false) String email,
                           @RequestParam @NotBlank String password,
                           RedirectAttributes ra) {
        if (userMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setRealName(realName.trim());
        user.setStudentNo(studentNo);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus("ACTIVE");
        user.setPoints(100);
        userMapper.insert(user);
        userMapper.addRole(user.getId(), "STUDENT");
        ra.addFlashAttribute("message", "注册成功，请登录");
        return "redirect:/login";
    }
}
