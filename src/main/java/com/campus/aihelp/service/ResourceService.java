package com.campus.aihelp.service;

import com.campus.aihelp.aop.AuditLog;
import com.campus.aihelp.domain.ResourceItem;
import com.campus.aihelp.mapper.ResourceMapper;
import com.campus.aihelp.mapper.TagMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceService {
    public static final int PAGE_SIZE = 10;

    private final ResourceMapper resourceMapper;
    private final TagMapper tagMapper;
    private final PointService pointService;

    public ResourceService(ResourceMapper resourceMapper, TagMapper tagMapper, PointService pointService) {
        this.resourceMapper = resourceMapper;
        this.tagMapper = tagMapper;
        this.pointService = pointService;
    }

    @Cacheable(value = "resources", key = "#keyword + ':' + #courseId + ':' + #type + ':' + #tag + ':' + #sort + ':' + #page")
    public List<ResourceItem> approved(String keyword, Long courseId, String type, String tag, String sort, int page) {
        return resourceMapper.findApproved(
                blankToNull(keyword),
                courseId,
                blankToNull(type),
                blankToNull(tag),
                normalizeSort(sort),
                PAGE_SIZE,
                Math.max(page - 1, 0) * PAGE_SIZE);
    }

    public int totalPages(String keyword, Long courseId, String type, String tag) {
        int count = resourceMapper.countApproved(blankToNull(keyword), courseId, blankToNull(type), blankToNull(tag));
        return Math.max((int) Math.ceil(count / (double) PAGE_SIZE), 1);
    }

    public List<String> tagNames() {
        return tagMapper.findAllNames();
    }

    public List<ResourceItem> pending() {
        return resourceMapper.findPending();
    }

    @AuditLog("查看资源详情")
    public ResourceItem view(Long id) {
        resourceMapper.increaseView(id);
        return resourceMapper.findById(id);
    }

    @CacheEvict(value = "resources", allEntries = true)
    @Transactional
    public void create(ResourceItem item) {
        resourceMapper.insert(item);
        saveTags(item.getId(), item.getTagNames());
    }

    @AuditLog("资源审核")
    @Transactional
    @CacheEvict(value = "resources", allEntries = true)
    public void audit(Long id, String status) {
        resourceMapper.updateAuditStatus(id, status);
        ResourceItem item = resourceMapper.findById(id);
        if ("APPROVED".equals(status)) {
            pointService.changePoints(item.getUploaderId(), 5, "RESOURCE_APPROVED", id, "资源通过审核");
        }
    }

    @AuditLog("收藏资源")
    @Transactional
    public void favorite(Long userId, Long resourceId) {
        resourceMapper.favorite(userId, resourceId);
        pointService.changePoints(userId, 1, "FAVORITE_RESOURCE", resourceId, "收藏学习资源");
    }

    public List<ResourceItem> hot(int limit) {
        return resourceMapper.hotResources(limit);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeSort(String sort) {
        if ("views".equals(sort) || "rating".equals(sort)) {
            return sort;
        }
        return "new";
    }

    private void saveTags(Long resourceId, String tags) {
        String value = blankToNull(tags);
        if (resourceId == null || value == null) {
            return;
        }
        for (String raw : value.split("[,，;；\\s]+")) {
            String name = blankToNull(raw);
            if (name == null) {
                continue;
            }
            tagMapper.insertIfAbsent(name);
            Long tagId = tagMapper.findIdByName(name);
            if (tagId != null) {
                tagMapper.linkResource(resourceId, tagId);
            }
        }
    }
}
