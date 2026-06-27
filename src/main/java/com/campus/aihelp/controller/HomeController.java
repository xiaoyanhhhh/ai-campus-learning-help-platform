package com.campus.aihelp.controller;

import com.campus.aihelp.aop.AuditLog;
import com.campus.aihelp.mapper.HelpRequestMapper;
import com.campus.aihelp.mapper.LogMapper;
import com.campus.aihelp.mapper.PointMapper;
import com.campus.aihelp.mapper.UserMapper;
import com.campus.aihelp.service.CurrentUserService;
import com.campus.aihelp.service.ResourceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    private final CurrentUserService currentUserService;
    private final ResourceService resourceService;
    private final HelpRequestMapper helpRequestMapper;
    private final PointMapper pointMapper;
    private final LogMapper logMapper;
    private final UserMapper userMapper;

    public HomeController(CurrentUserService currentUserService, ResourceService resourceService,
                          HelpRequestMapper helpRequestMapper, PointMapper pointMapper, LogMapper logMapper,
                          UserMapper userMapper) {
        this.currentUserService = currentUserService;
        this.resourceService = resourceService;
        this.helpRequestMapper = helpRequestMapper;
        this.pointMapper = pointMapper;
        this.logMapper = logMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/")
    public String index(Model model) {
        var user = currentUserService.currentUser();
        var resources = resourceService.hot(5);
        var allHelps = helpRequestMapper.findAll();
        List<Map<String, Object>> rank = pointMapper.rank(5).stream()
                .map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", value(row, "real_name", "REAL_NAME"));
                    item.put("points", value(row, "points", "POINTS"));
                    return item;
                })
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("hotResources", resources);
        model.addAttribute("helps", allHelps.stream().limit(5).toList());
        model.addAttribute("rank", rank);
        model.addAttribute("resourceCount", resources.size());
        model.addAttribute("openHelpCount", allHelps.stream().filter(h -> "OPEN".equals(h.getStatus())).count());
        model.addAttribute("aiCallCount", logMapper.recentAi().size());
        return "index";
    }

    private Object value(Map<String, Object> row, String lower, String upper) {
        return row.containsKey(lower) ? row.get(lower) : row.get(upper);
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        var users = userMapper.findAll();
        users.forEach(user -> user.setRoles(userMapper.findRoleCodes(user.getId())));
        model.addAttribute("users", users);
        model.addAttribute("aiLogs", logMapper.recentAi());
        model.addAttribute("operationLogs", logMapper.recentOperations());
        model.addAttribute("points", pointMapper.recent(20));
        return "admin";
    }

    @PostMapping("/admin/users/{id}/status")
    @AuditLog("管理员更新用户状态")
    public String updateUserStatus(@PathVariable Long id,
                                   @RequestParam String status,
                                   RedirectAttributes ra) {
        var current = currentUserService.currentUser();
        if (current != null && current.getId().equals(id) && !"ACTIVE".equals(status)) {
            throw new IllegalArgumentException("不能禁用或待审当前登录管理员账号");
        }
        if (!List.of("ACTIVE", "PENDING", "DISABLED").contains(status)) {
            throw new IllegalArgumentException("不支持的用户状态");
        }
        userMapper.updateStatus(id, status);
        ra.addFlashAttribute("message", "用户状态已更新：" + status);
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/role")
    @AuditLog("管理员分配用户角色")
    public String updateUserRole(@PathVariable Long id,
                                 @RequestParam String role,
                                 RedirectAttributes ra) {
        if (!List.of("ADMIN", "TEACHER", "STUDENT").contains(role)) {
            throw new IllegalArgumentException("不支持的用户角色");
        }
        userMapper.deleteRoles(id);
        userMapper.addRole(id, role);
        ra.addFlashAttribute("message", "用户角色已分配：" + role);
        return "redirect:/admin";
    }
}
