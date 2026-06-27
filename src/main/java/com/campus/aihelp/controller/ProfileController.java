package com.campus.aihelp.controller;

import com.campus.aihelp.aop.AuditLog;
import com.campus.aihelp.mapper.UserMapper;
import com.campus.aihelp.service.CurrentUserService;
import com.campus.aihelp.service.PointService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {
    private final CurrentUserService currentUserService;
    private final PointService pointService;
    private final UserMapper userMapper;

    public ProfileController(CurrentUserService currentUserService, PointService pointService, UserMapper userMapper) {
        this.currentUserService = currentUserService;
        this.pointService = pointService;
        this.userMapper = userMapper;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        var user = currentUserService.currentUser();
        model.addAttribute("user", user);
        model.addAttribute("records", pointService.myRecords(user.getId()));
        model.addAttribute("allUsers", userMapper.findAll());
        return "profile";
    }

    @PostMapping("/profile")
    @AuditLog("维护个人资料")
    public String updateProfile(@RequestParam @NotBlank String realName,
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) String email,
                                RedirectAttributes ra) {
        var user = currentUserService.currentUser();
        user.setRealName(realName.trim());
        user.setStudentNo(studentNo);
        user.setEmail(email);
        userMapper.updateProfile(user);
        ra.addFlashAttribute("message", "个人资料已更新");
        return "redirect:/profile";
    }
}
