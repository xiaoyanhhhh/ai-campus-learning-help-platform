package com.campus.aihelp.controller;

import com.campus.aihelp.aop.AuditLog;
import com.campus.aihelp.domain.HelpComment;
import com.campus.aihelp.domain.HelpRequest;
import com.campus.aihelp.mapper.HelpCommentMapper;
import com.campus.aihelp.mapper.CourseMapper;
import com.campus.aihelp.service.AiService;
import com.campus.aihelp.service.CurrentUserService;
import com.campus.aihelp.service.HelpService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class HelpController {
    private final HelpService helpService;
    private final CourseMapper courseMapper;
    private final CurrentUserService currentUserService;
    private final HelpCommentMapper helpCommentMapper;
    private final AiService aiService;

    public HelpController(HelpService helpService, CourseMapper courseMapper, CurrentUserService currentUserService,
                          HelpCommentMapper helpCommentMapper, AiService aiService) {
        this.helpService = helpService;
        this.courseMapper = courseMapper;
        this.currentUserService = currentUserService;
        this.helpCommentMapper = helpCommentMapper;
        this.aiService = aiService;
    }

    @GetMapping("/helps")
    public String list(Model model) {
        model.addAttribute("helps", helpService.all());
        model.addAttribute("courses", courseMapper.findAll());
        model.addAttribute("user", currentUserService.currentUser());
        return "helps";
    }

    @GetMapping("/helps/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("help", helpService.get(id));
        model.addAttribute("comments", helpCommentMapper.findByHelpId(id));
        model.addAttribute("user", currentUserService.currentUser());
        return "help-detail";
    }

    @PostMapping("/helps")
    public String create(@RequestParam @NotBlank String title,
                         @RequestParam @NotBlank String description,
                         @RequestParam Long courseId,
                         @RequestParam(required = false) String tags,
                         @RequestParam(defaultValue = "0") Integer bountyPoints,
                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime deadline) {
        HelpRequest request = new HelpRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCourseId(courseId);
        request.setTags(tags);
        request.setBountyPoints(bountyPoints);
        request.setDeadline(deadline);
        request.setPublisherId(currentUserService.currentUser().getId());
        helpService.create(request);
        return "redirect:/helps";
    }

    @PostMapping("/helps/{id}/claim")
    public String claim(@PathVariable Long id, RedirectAttributes ra) {
        helpService.claim(id, currentUserService.currentUser().getId());
        ra.addFlashAttribute("message", "认领成功");
        return "redirect:/helps/" + id;
    }

    @PostMapping("/helps/{id}/solution")
    public String submit(@PathVariable Long id, @RequestParam String solution, RedirectAttributes ra) {
        helpService.submit(id, currentUserService.currentUser().getId(), solution);
        ra.addFlashAttribute("message", "解答已提交，等待发布者确认");
        return "redirect:/helps/" + id;
    }

    @PostMapping("/helps/{id}/complete")
    public String complete(@PathVariable Long id, @RequestParam(required = false) String evaluation, RedirectAttributes ra) {
        helpService.complete(id, currentUserService.currentUser().getId(), evaluation);
        ra.addFlashAttribute("message", "互助已完成，积分已结算");
        return "redirect:/helps/" + id;
    }

    @PostMapping("/helps/{id}/comments")
    @AuditLog("提交求助补充说明")
    public String comment(@PathVariable Long id, @RequestParam @NotBlank String content, RedirectAttributes ra) {
        HelpComment comment = new HelpComment();
        comment.setHelpId(id);
        comment.setUserId(currentUserService.currentUser().getId());
        comment.setContent(content.trim());
        helpCommentMapper.insert(comment);
        ra.addFlashAttribute("message", "补充说明已提交");
        return "redirect:/helps/" + id;
    }

    @PostMapping("/helps/{id}/ai-answer")
    @AuditLog("AI辅助解答学习求助")
    public String aiAnswer(@PathVariable Long id, RedirectAttributes ra) {
        HelpRequest help = helpService.get(id);
        String answer = aiService.answerHelp(
                currentUserService.currentUser().getId(),
                help,
                helpCommentMapper.findByHelpId(id));
        ra.addFlashAttribute("aiAnswer", answer);
        ra.addFlashAttribute("message", "AI 辅助解答已生成");
        return "redirect:/helps/" + id;
    }
}
