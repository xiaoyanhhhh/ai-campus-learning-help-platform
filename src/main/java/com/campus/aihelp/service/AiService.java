package com.campus.aihelp.service;

import com.campus.aihelp.config.AppProperties;
import com.campus.aihelp.domain.AiCallLog;
import com.campus.aihelp.domain.HelpComment;
import com.campus.aihelp.domain.HelpRequest;
import com.campus.aihelp.domain.ResourceItem;
import com.campus.aihelp.mapper.LogMapper;
import com.campus.aihelp.mapper.ResourceMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiService {
    private final AppProperties properties;
    private final LogMapper logMapper;
    private final ResourceMapper resourceMapper;
    private final ObjectMapper objectMapper;

    public AiService(AppProperties properties, LogMapper logMapper, ResourceMapper resourceMapper, ObjectMapper objectMapper) {
        this.properties = properties;
        this.logMapper = logMapper;
        this.resourceMapper = resourceMapper;
        this.objectMapper = objectMapper;
    }

    public String recommend(Long userId, String tags) {
        long start = System.currentTimeMillis();
        try {
            List<ResourceItem> resources = resourceMapper.hotResources(6);
            String response;
            String request = "tags=" + safe(tags) + "; resources=" + resourceContext(resources);
            if (realModelEnabled()) {
                response = chat("RECOMMEND", List.of(
                        message("system", "你是校园学习互助平台的 AI 推荐助手。请只基于平台资源做推荐，说明推荐依据，避免编造不存在的资源。"),
                        message("user", "学生当前标签或困难：" + safe(tags) + "\n\n平台已审核热门资源：\n" + resourceContext(resources)
                                + "\n\n请推荐 3-5 个最相关资源，并给出简短理由。")
                ));
            } else {
                response = mockRecommend(tags, resources);
            }
            save("RECOMMEND", userId, request, response, start, "SUCCESS");
            return response;
        } catch (Exception ex) {
            save("RECOMMEND", userId, safe(tags), ex.getMessage(), start, "FAILED");
            throw new IllegalStateException("AI 推荐调用失败：" + ex.getMessage(), ex);
        }
    }

    public String summarize(Long userId, String title, String content) {
        long start = System.currentTimeMillis();
        String text = safe(content);
        try {
            String response;
            if (realModelEnabled()) {
                response = chat("SUMMARY", List.of(
                        message("system", "你是课程资源摘要助手。请输出摘要、关键词、适用章节建议，保持简洁，方便教师审核资源。"),
                        message("user", "资源标题：" + safe(title) + "\n资源内容：\n" + text)
                ));
            } else {
                response = "摘要：" + abbreviate(text, 80) + "；关键词：" + keywords(title + " " + text) + "；建议章节：" + guessChapter(text) + "。";
            }
            save("SUMMARY", userId, safe(title), response, start, "SUCCESS");
            return response;
        } catch (Exception ex) {
            save("SUMMARY", userId, safe(title), ex.getMessage(), start, "FAILED");
            throw new IllegalStateException("AI 摘要调用失败：" + ex.getMessage(), ex);
        }
    }

    public String auditContent(Long userId, String content) {
        long start = System.currentTimeMillis();
        String text = safe(content);
        try {
            String response;
            if (realModelEnabled()) {
                response = chat("CONTENT_AUDIT", List.of(
                        message("system", "你是校园学习平台内容审核助手。请判断内容是否存在广告、代写、辱骂、违规引流、低质量灌水，并给出可人工复核的建议。"),
                        message("user", "待审核内容：\n" + text + "\n\n请按：审核结论、风险点、处理建议 三项输出。")
                ));
            } else {
                response = mockAudit(text);
            }
            save("CONTENT_AUDIT", userId, abbreviate(text, 120), response, start, "SUCCESS");
            return response;
        } catch (Exception ex) {
            save("CONTENT_AUDIT", userId, abbreviate(text, 120), ex.getMessage(), start, "FAILED");
            throw new IllegalStateException("AI 内容审核调用失败：" + ex.getMessage(), ex);
        }
    }

    public String answerHelp(Long userId, HelpRequest help, List<HelpComment> comments) {
        long start = System.currentTimeMillis();
        try {
            List<ResourceItem> resources = resourceMapper.hotResources(8);
            String request = "help=" + safe(help.getTitle()) + "; tags=" + safe(help.getTags());
            String response;
            if (realModelEnabled()) {
                response = chat("HELP_ANSWER", List.of(
                        message("system", """
                                你是校园学习互助平台的 AI 助教。你的任务是帮助长期无人认领或暂时搁置的学习求助。
                                必须优先基于平台已审核资源与问题上下文回答；如果资料不足，要明确说明不确定点。
                                不要直接代写完整作业或考试答案，要给出思路、排查步骤、关键概念和可引用资源名称。
                                输出结构：问题判断、解决步骤、参考资源、下一步建议。
                                """),
                        message("user", helpPrompt(help, comments, resources))
                ));
            } else {
                response = mockHelpAnswer(help, resources);
            }
            save("HELP_ANSWER", userId, request, response, start, "SUCCESS");
            return response;
        } catch (Exception ex) {
            save("HELP_ANSWER", userId, safe(help.getTitle()), ex.getMessage(), start, "FAILED");
            throw new IllegalStateException("AI 求助解答调用失败：" + ex.getMessage(), ex);
        }
    }

    private String chat(String feature, List<Map<String, String>> messages) throws Exception {
        AppProperties.Ai ai = properties.getAi();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", safe(ai.getModel()).isBlank() ? "gpt-4o-mini" : ai.getModel());
        body.put("messages", messages);
        body.put("temperature", 0.2);

        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(chatCompletionsUrl(ai.getBaseUrl())))
                .timeout(Duration.ofMillis(Math.max(ai.getTimeoutMs(), 1000)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ai.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(ai.getTimeoutMs(), 1000)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(feature + " HTTP " + response.statusCode() + ": " + abbreviate(response.body(), 300));
        }
        return parseChatContent(response.body());
    }

    private String parseChatContent(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (!content.isMissingNode() && !content.asText().isBlank()) {
            return content.asText();
        }
        JsonNode text = root.path("choices").path(0).path("text");
        if (!text.isMissingNode() && !text.asText().isBlank()) {
            return text.asText();
        }
        throw new IllegalStateException("模型响应缺少 choices[0].message.content");
    }

    private boolean realModelEnabled() {
        AppProperties.Ai ai = properties.getAi();
        return !ai.isMockEnabled() && !safe(ai.getBaseUrl()).isBlank() && !safe(ai.getApiKey()).isBlank();
    }

    private String chatCompletionsUrl(String baseUrl) {
        String base = safe(baseUrl);
        if (base.endsWith("/chat/completions")) {
            return base;
        }
        if (base.endsWith("/")) {
            return base + "chat/completions";
        }
        return base + "/chat/completions";
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String helpPrompt(HelpRequest help, List<HelpComment> comments, List<ResourceItem> resources) {
        return "求助标题：" + safe(help.getTitle())
                + "\n所属课程：" + safe(help.getCourseName())
                + "\n知识点标签：" + safe(help.getTags())
                + "\n求助状态：" + safe(help.getStatus())
                + "\n问题描述：\n" + safe(help.getDescription())
                + "\n\n已有补充说明：\n" + commentContext(comments)
                + "\n\n平台已审核资源：\n" + resourceContext(resources)
                + "\n\n请帮助这位同学先推进问题，必要时指出还需要补充哪些信息。";
    }

    private String resourceContext(List<ResourceItem> resources) {
        if (resources == null || resources.isEmpty()) {
            return "暂无可引用资源。";
        }
        List<String> lines = new ArrayList<>();
        for (ResourceItem item : resources) {
            lines.add("- 《" + safe(item.getTitle()) + "》"
                    + "，课程：" + safe(item.getCourseName())
                    + "，章节：" + safe(item.getChapter())
                    + "，简介：" + abbreviate(item.getDescription(), 90));
        }
        return String.join("\n", lines);
    }

    private String commentContext(List<HelpComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return "暂无补充说明。";
        }
        return comments.stream()
                .map(c -> "- " + safe(c.getUserName()) + "：" + abbreviate(c.getContent(), 160))
                .collect(Collectors.joining("\n"));
    }

    private String mockRecommend(String tags, List<ResourceItem> resources) {
        String titles = resources.stream().map(ResourceItem::getTitle).collect(Collectors.joining("、"));
        return "推荐资源：" + titles + "。依据：你的标签为 [" + safe(tags) + "]，系统优先选择已审核、浏览热度高且与课程章节相关的资料。";
    }

    private String mockAudit(String text) {
        String[] sensitive = {"广告", "代写", "破解", "辱骂"};
        for (String word : sensitive) {
            if (text.contains(word)) {
                return "审核建议：需人工复核，命中敏感词 [" + word + "]。";
            }
        }
        return "审核建议：可通过。内容未命中敏感词，表达质量正常。";
    }

    private String mockHelpAnswer(HelpRequest help, List<ResourceItem> resources) {
        String references = resources.stream()
                .limit(3)
                .map(item -> "《" + item.getTitle() + "》")
                .collect(Collectors.joining("、"));
        return """
                AI 辅助解答：
                1. 先把问题拆成“现象、期望结果、已尝试方案、报错信息”四部分，方便同学或老师继续接手。
                2. 根据当前标签，可以优先回看相关课程资源，并对照示例代码逐步排查。
                3. 如果问题长期无人认领，建议先补充运行截图、核心代码片段和期望截止时间。
                参考资源：%s。
                """.formatted(references.isBlank() ? "暂无可引用资源" : references);
    }

    private String keywords(String text) {
        return List.of("Spring", "MyBatis", "事务", "权限", "数据库", "前端").stream()
                .filter(text::contains)
                .limit(3)
                .collect(Collectors.joining("、"));
    }

    private String guessChapter(String text) {
        if (text.contains("权限") || text.contains("登录")) return "用户认证与 RBAC";
        if (text.contains("事务") || text.contains("积分")) return "事务管理";
        if (text.contains("SQL") || text.contains("MyBatis")) return "数据访问层";
        return "课程综合实践";
    }

    private void save(String feature, Long userId, String request, String response, long start, String status) {
        AiCallLog log = new AiCallLog();
        log.setFeature(feature + "/" + properties.getAi().getProvider());
        log.setUserId(userId);
        log.setRequestSummary(abbreviate(request, 900));
        log.setResponseSummary(abbreviate(response, 900));
        log.setElapsedMs(System.currentTimeMillis() - start);
        log.setStatus(status);
        logMapper.insertAi(log);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String abbreviate(String value, int max) {
        String safe = safe(value);
        return safe.length() <= max ? safe : safe.substring(0, max) + "...";
    }
}
