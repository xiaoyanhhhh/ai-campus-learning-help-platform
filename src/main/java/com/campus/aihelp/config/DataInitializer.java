package com.campus.aihelp.config;

import com.campus.aihelp.domain.HelpComment;
import com.campus.aihelp.domain.Course;
import com.campus.aihelp.domain.HelpRequest;
import com.campus.aihelp.domain.PointRecord;
import com.campus.aihelp.domain.ResourceItem;
import com.campus.aihelp.domain.User;
import com.campus.aihelp.mapper.CourseMapper;
import com.campus.aihelp.mapper.HelpCommentMapper;
import com.campus.aihelp.mapper.HelpRequestMapper;
import com.campus.aihelp.mapper.PointMapper;
import com.campus.aihelp.mapper.ResourceMapper;
import com.campus.aihelp.mapper.TagMapper;
import com.campus.aihelp.mapper.UserMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@EnableConfigurationProperties(AppProperties.class)
public class DataInitializer implements ApplicationRunner {
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final ResourceMapper resourceMapper;
    private final TagMapper tagMapper;
    private final HelpRequestMapper helpRequestMapper;
    private final HelpCommentMapper helpCommentMapper;
    private final PointMapper pointMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, CourseMapper courseMapper, ResourceMapper resourceMapper,
                           TagMapper tagMapper, HelpRequestMapper helpRequestMapper, HelpCommentMapper helpCommentMapper,
                           PointMapper pointMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.resourceMapper = resourceMapper;
        this.tagMapper = tagMapper;
        this.helpRequestMapper = helpRequestMapper;
        this.helpCommentMapper = helpCommentMapper;
        this.pointMapper = pointMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        userMapper.insertRoleIfAbsent("ADMIN", "系统管理员");
        userMapper.insertRoleIfAbsent("TEACHER", "教师/助教");
        userMapper.insertRoleIfAbsent("STUDENT", "学生");
        seedDemoData();
    }

    private User user(String username, String realName, String no, String email, int points) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(realName);
        user.setStudentNo(no);
        user.setEmail(email);
        user.setStatus("ACTIVE");
        user.setPoints(points);
        return user;
    }

    private Course course(String name, String description) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        return course;
    }

    private ResourceItem resource(String title, String type, String chapter, String description, Long courseId, Long uploaderId, String status) {
        ResourceItem item = new ResourceItem();
        item.setTitle(title);
        item.setType(type);
        item.setChapter(chapter);
        item.setDescription(description);
        item.setCourseId(courseId);
        item.setUploaderId(uploaderId);
        item.setAuditStatus(status);
        return item;
    }

    private void tag(ResourceItem item, String... names) {
        tag(item.getId(), names);
    }

    private void tag(Long resourceId, String... names) {
        if (resourceId == null) {
            return;
        }
        for (String name : names) {
            tagMapper.insertIfAbsent(name);
            Long tagId = tagMapper.findIdByName(name);
            if (tagId != null) {
                tagMapper.linkResource(resourceId, tagId);
            }
        }
    }

    private void seedDemoTagsForExistingData() {
        tag(resourceMapper.findIdByTitleKeyword("Security"), "Spring", "Security", "RBAC");
        tag(resourceMapper.findIdByTitleKeyword("MyBatis"), "MyBatis", "CRUD", "分页");
        tag(resourceMapper.findIdByTitleKeyword("事务"), "事务", "积分", "数据库");
    }

    private void seedDemoData() {
        User admin = ensureUser("admin", "系统管理员", "A001", "admin@example.com", 500, "ACTIVE", "ADMIN");
        User teacher = ensureUser("teacher", "张老师", "T001", "teacher@example.com", 300, "ACTIVE", "TEACHER");
        User assistant = ensureUser("assistant", "王助教", "T002", "assistant@example.com", 260, "ACTIVE", "TEACHER");
        User student = ensureUser("student", "李同学", "S001", "student@example.com", 120, "ACTIVE", "STUDENT");
        User helper = ensureUser("helper", "赵同学", "S002", "helper@example.com", 180, "ACTIVE", "STUDENT");
        User student2 = ensureUser("student2", "陈同学", "S003", "student2@example.com", 95, "ACTIVE", "STUDENT");
        User pending = ensureUser("pending_student", "待审学生", "S004", "pending@example.com", 100, "PENDING", "STUDENT");
        User disabled = ensureUser("disabled_student", "异常账号", "S005", "disabled@example.com", 60, "DISABLED", "STUDENT");

        Course javaee = ensureCourse("JavaEE应用开发", "Spring Boot、Spring MVC、MyBatis、Security 综合实践");
        Course db = ensureCourse("数据库系统", "SQL、事务、索引与数据库设计");
        Course frontend = ensureCourse("前端基础", "HTML、CSS、JavaScript 与响应式页面");
        Course se = ensureCourse("软件工程", "需求分析、概要设计、测试与项目管理");
        Course os = ensureCourse("操作系统", "进程、线程、调度、内存与文件系统");
        Course ds = ensureCourse("数据结构", "线性表、树、图、查找与排序");
        Course ai = ensureCourse("AI工具与学习方法", "AI 辅助学习、提示词、学习计划与内容审核");

        ResourceItem r1 = ensureResource("Spring Security 登录与 RBAC 示例", "代码示例", "第4章 权限控制",
                "演示表单登录、角色授权、密码加密与页面权限控制。", javaee, teacher, "APPROVED", "Spring", "Security", "RBAC");
        ResourceItem r2 = ensureResource("MyBatis 注解查询速查", "学习笔记", "第5章 数据访问",
                "整理常见 CRUD、分页、动态条件与事务配合方式。", javaee, teacher, "APPROVED", "MyBatis", "CRUD", "分页");
        ResourceItem r3 = ensureResource("事务隔离级别案例", "实验说明", "事务与并发",
                "通过积分转账案例理解事务一致性与回滚。", db, teacher, "APPROVED", "事务", "积分", "数据库");
        ensureResource("Thymeleaf 表单与 CSRF 实战", "课件", "第3章 Web 表单",
                "覆盖表单绑定、CSRF Token、错误提示与安全提交流程。", javaee, assistant, "APPROVED", "Thymeleaf", "CSRF", "安全");
        ensureResource("Spring Boot 文件上传配置清单", "实验说明", "第6章 文件处理",
                "讲解 multipart 限制、上传路径、静态资源访问和异常处理。", javaee, assistant, "APPROVED", "Spring Boot", "文件上传", "配置");
        ensureResource("RESTful 接口与统一异常处理", "课件", "第7章 Web MVC",
                "包含请求参数、JSON 响应、统一异常处理和友好错误页面。", javaee, teacher, "APPROVED", "REST", "异常处理", "MVC");
        ensureResource("数据库索引与慢查询分析", "课件", "索引优化",
                "通过 explain 思路理解索引选择、排序和范围查询优化。", db, teacher, "APPROVED", "索引", "SQL", "性能");
        ensureResource("ER 图到表结构设计样例", "实验说明", "数据库设计",
                "从业务实体抽取主键、外键、索引与字段约束。", db, assistant, "APPROVED", "ER图", "建模", "外键");
        ensureResource("事务传播行为速查卡", "常见问题", "Spring 事务",
                "整理 REQUIRED、REQUIRES_NEW 等传播行为的使用场景。", db, teacher, "APPROVED", "事务", "Spring", "回滚");
        ensureResource("响应式布局与表单可用性", "课件", "CSS 布局",
                "说明移动端表单、栅格布局、按钮状态和可读性细节。", frontend, assistant, "APPROVED", "CSS", "响应式", "表单");
        ensureResource("JavaScript 表单校验示例", "代码示例", "前端交互",
                "包含必填项、邮箱格式、数字范围和提交前提示。", frontend, assistant, "APPROVED", "JavaScript", "校验", "表单");
        ensureResource("软件工程需求分析模板", "课件", "需求分析",
                "提供项目背景、用户角色、功能清单和非功能需求写法。", se, teacher, "APPROVED", "需求分析", "报告", "模板");
        ensureResource("测试用例设计与缺陷记录", "实验说明", "系统测试",
                "演示等价类、边界值、流程测试和缺陷复现记录。", se, teacher, "APPROVED", "测试", "缺陷", "用例");
        ensureResource("操作系统进程调度复习笔记", "学习笔记", "进程调度",
                "整理 FCFS、SJF、RR 等调度算法的例题和易错点。", os, assistant, "APPROVED", "操作系统", "调度", "复习");
        ensureResource("数据结构图遍历题解", "学习笔记", "图",
                "对 BFS、DFS、邻接表和邻接矩阵做题流程进行归纳。", ds, assistant, "APPROVED", "数据结构", "图", "遍历");
        ensureResource("AI 生成学习计划提示词样例", "常见问题", "AI 学习方法",
                "示例如何给出目标日期、知识点和学习记录来生成复习计划。", ai, teacher, "APPROVED", "AI", "学习计划", "提示词");
        ensureResource("学生上传-待审核 Docker 部署笔记", "学习笔记", "部署",
                "学生分享的 Docker 部署草稿，等待教师审核后公开。", javaee, student2, "PENDING", "Docker", "部署", "待审核");
        ensureResource("学生上传-低质量广告示例", "常见问题", "内容审核",
                "包含广告化表达的样例，用于演示资源驳回与内容审核。", ai, student2, "REJECTED", "内容审核", "广告", "驳回");

        HelpRequest open = ensureHelp("Spring Security 登录后如何按角色跳转页面？", "我已经能登录，但不知道如何让管理员和学生看到不同菜单。希望结合 Thymeleaf 给一个思路。",
                javaee, student, "Spring Security,RBAC,Thymeleaf", 20, "OPEN", null, null, null);
        HelpRequest claimed = ensureHelp("积分事务结算失败如何排查？", "确认完成时积分流水写入了，但用户总积分没有变化，想确认事务边界。",
                javaee, student2, "事务,积分,Spring", 25, "CLAIMED", helper, null, null);
        HelpRequest waiting = ensureHelp("前端表单提交后 CSRF 报 403", "登录后提交资源表单提示 403，希望帮忙看 Thymeleaf 表单隐藏字段。",
                frontend, student, "CSRF,Thymeleaf,表单", 15, "WAIT_CONFIRM", helper, "需要确认页面是否使用 th:action，并检查 Spring Security 自动注入的 _csrf 字段。", null);
        HelpRequest completed = ensureHelp("数据库索引为什么没有生效？", "where 条件和 order by 同时出现时索引没有命中，希望给个排查思路。",
                db, student2, "索引,SQL,性能", 30, "COMPLETED", helper, "先查看 explain，再确认联合索引顺序和范围查询位置。", "解释清晰，已按建议调整索引。");
        ensureHelp("操作系统实验报告格式确认", "报告里的调度算法图表格式不确定，先关闭本次求助。",
                os, student, "操作系统,报告", 0, "CLOSED", null, null, null);
        ensureHelp("图遍历 DFS 递归出口怎么写？", "图遍历题中递归会重复访问节点，希望同学讲一下 visited 数组。",
                ds, pending, "DFS,递归,visited", 10, "OPEN", null, null, null);

        ensureComment(open, teacher, "可以先看资源《Spring Security 登录与 RBAC 示例》，里面有菜单权限判断示例。");
        ensureComment(open, helper, "我可以补一段登录成功后根据角色跳转的代码思路。");
        ensureComment(claimed, teacher, "重点检查积分扣减和流水插入是否在同一个 @Transactional 方法中。");
        ensureComment(waiting, assistant, "Thymeleaf 表单建议统一使用 th:action，避免 CSRF token 漏掉。");
        ensureComment(completed, student2, "已确认 explain 结果，联合索引顺序确实写反了。");

        ensurePoint(student.getId(), -20, "HELP_BOUNTY", completed.getId(), "演示：完成互助后支付悬赏积分");
        ensurePoint(helper.getId(), 30, "HELP_COMPLETED", completed.getId(), "演示：完成互助获得悬赏与奖励");
        ensurePoint(teacher.getId(), 5, "RESOURCE_APPROVED", r1.getId(), "演示：优质课程资源通过审核");
        ensurePoint(student.getId(), 1, "FAVORITE_RESOURCE", r2.getId(), "演示：收藏课程资源获得积分");
        ensurePoint(disabled.getId(), -40, "ACCOUNT_PENALTY", disabled.getId(), "演示：异常账号扣分记录");

        seedDemoTagsForExistingData();
        tag(r1, "Spring", "Security", "RBAC");
        tag(r2, "MyBatis", "CRUD", "分页");
        tag(r3, "事务", "积分", "数据库");
        admin.getId();
    }

    private User ensureUser(String username, String realName, String no, String email, int points, String status, String role) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            user = user(username, realName, no, email, points);
            user.setStatus(status);
            userMapper.insert(user);
        }
        userMapper.addRole(user.getId(), role);
        if ("PENDING".equals(status) || "DISABLED".equals(status)) {
            userMapper.updateStatus(user.getId(), status);
            user.setStatus(status);
        }
        return user;
    }

    private Course ensureCourse(String name, String description) {
        Course course = courseMapper.findByName(name);
        if (course == null) {
            course = course(name, description);
            courseMapper.insert(course);
        }
        return course;
    }

    private ResourceItem ensureResource(String title, String type, String chapter, String description,
                                        Course course, User uploader, String status, String... tags) {
        ResourceItem item = resourceMapper.findByTitle(title);
        if (item == null) {
            item = resource(title, type, chapter, description, course.getId(), uploader.getId(), status);
            resourceMapper.insert(item);
        }
        tag(item, tags);
        return item;
    }

    private HelpRequest ensureHelp(String title, String description, Course course, User publisher, String tags,
                                   int bounty, String status, User helper, String solution, String evaluation) {
        HelpRequest request = helpRequestMapper.findByTitle(title);
        if (request == null) {
            request = new HelpRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setCourseId(course.getId());
            request.setTags(tags);
            request.setPublisherId(publisher.getId());
            request.setStatus("OPEN");
            request.setBountyPoints(bounty);
            request.setDeadline(LocalDateTime.now().plusDays(5));
            helpRequestMapper.insert(request);
            if ("CLAIMED".equals(status) || "WAIT_CONFIRM".equals(status) || "COMPLETED".equals(status)) {
                helpRequestMapper.claim(request.getId(), helper.getId());
            }
            if ("WAIT_CONFIRM".equals(status) || "COMPLETED".equals(status)) {
                helpRequestMapper.submitSolution(request.getId(), helper.getId(), solution);
            }
            if ("COMPLETED".equals(status)) {
                helpRequestMapper.complete(request.getId(), publisher.getId(), evaluation);
            }
            if ("CLOSED".equals(status)) {
                helpRequestMapper.close(request.getId(), publisher.getId());
            }
            request = helpRequestMapper.findById(request.getId());
        }
        return request;
    }

    private void ensureComment(HelpRequest help, User user, String content) {
        if (help != null && helpCommentMapper.countSame(help.getId(), user.getId(), content) == 0) {
            HelpComment comment = new HelpComment();
            comment.setHelpId(help.getId());
            comment.setUserId(user.getId());
            comment.setContent(content);
            helpCommentMapper.insert(comment);
        }
    }

    private void ensurePoint(Long userId, int delta, String source, Long bizId, String remark) {
        if (pointMapper.countByUserSourceBiz(userId, source, bizId) > 0) {
            return;
        }
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setChangeValue(delta);
        record.setSource(source);
        record.setBizId(bizId);
        record.setRemark(remark);
        pointMapper.insert(record);
    }
}
