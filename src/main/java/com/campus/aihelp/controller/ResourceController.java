package com.campus.aihelp.controller;

import com.campus.aihelp.domain.ResourceItem;
import com.campus.aihelp.mapper.CourseMapper;
import com.campus.aihelp.service.CurrentUserService;
import com.campus.aihelp.service.FileStorageService;
import com.campus.aihelp.service.ResourceService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResourceController {
    private final ResourceService resourceService;
    private final CourseMapper courseMapper;
    private final CurrentUserService currentUserService;
    private final FileStorageService fileStorageService;

    public ResourceController(ResourceService resourceService, CourseMapper courseMapper,
                              CurrentUserService currentUserService, FileStorageService fileStorageService) {
        this.resourceService = resourceService;
        this.courseMapper = courseMapper;
        this.currentUserService = currentUserService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/resources")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String tag,
                       @RequestParam(defaultValue = "new") String sort,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        int currentPage = Math.max(page, 1);
        model.addAttribute("resources", resourceService.approved(keyword, courseId, type, tag, sort, currentPage));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedTag", tag);
        model.addAttribute("sort", sort);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", resourceService.totalPages(keyword, courseId, type, tag));
        model.addAttribute("courses", courseMapper.findAll());
        model.addAttribute("tags", resourceService.tagNames());
        model.addAttribute("pending", resourceService.pending());
        model.addAttribute("user", currentUserService.currentUser());
        return "resources";
    }

    @GetMapping("/resources/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("resource", resourceService.view(id));
        model.addAttribute("user", currentUserService.currentUser());
        return "resource-detail";
    }

    @PostMapping("/resources")
    public String create(@RequestParam @NotBlank String title,
                         @RequestParam @NotBlank String type,
                         @RequestParam Long courseId,
                         @RequestParam(required = false) String chapter,
                         @RequestParam(required = false) String tags,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) MultipartFile file,
                         RedirectAttributes ra) {
        var user = currentUserService.currentUser();
        ResourceItem item = new ResourceItem();
        item.setTitle(title);
        item.setType(type);
        item.setCourseId(courseId);
        item.setChapter(chapter);
        item.setTagNames(tags);
        item.setDescription(description);
        item.setFilePath(fileStorageService.store(file));
        item.setUploaderId(user.getId());
        item.setAuditStatus(user.getRoles().contains("TEACHER") || user.getRoles().contains("ADMIN") ? "APPROVED" : "PENDING");
        resourceService.create(item);
        ra.addFlashAttribute("message", "资源已提交" + ("PENDING".equals(item.getAuditStatus()) ? "，等待审核" : ""));
        return "redirect:/resources";
    }

    @PostMapping("/resources/{id}/favorite")
    public String favorite(@PathVariable Long id, RedirectAttributes ra) {
        resourceService.favorite(currentUserService.currentUser().getId(), id);
        ra.addFlashAttribute("message", "收藏成功，积分 +1");
        return "redirect:/resources/" + id;
    }

    @PostMapping("/resources/audit/{id}")
    public String audit(@PathVariable Long id, @RequestParam String status, RedirectAttributes ra) {
        resourceService.audit(id, status);
        ra.addFlashAttribute("message", "审核完成：" + status);
        return "redirect:/resources";
    }
}
