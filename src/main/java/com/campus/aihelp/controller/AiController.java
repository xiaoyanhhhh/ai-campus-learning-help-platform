package com.campus.aihelp.controller;

import com.campus.aihelp.service.AiService;
import com.campus.aihelp.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AiController {
    private final AiService aiService;
    private final CurrentUserService currentUserService;

    public AiController(AiService aiService, CurrentUserService currentUserService) {
        this.aiService = aiService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/ai")
    public String page() {
        return "ai";
    }

    @PostMapping("/ai/recommend")
    public String recommend(@RequestParam String tags, Model model) {
        model.addAttribute("recommendResult", aiService.recommend(currentUserService.currentUser().getId(), tags));
        return "ai";
    }

    @PostMapping("/ai/summary")
    public String summary(@RequestParam String title, @RequestParam String content, Model model) {
        model.addAttribute("summaryResult", aiService.summarize(currentUserService.currentUser().getId(), title, content));
        return "ai";
    }

    @PostMapping("/ai/audit")
    public String audit(@RequestParam String content, Model model) {
        model.addAttribute("auditResult", aiService.auditContent(currentUserService.currentUser().getId(), content));
        return "ai";
    }
}
