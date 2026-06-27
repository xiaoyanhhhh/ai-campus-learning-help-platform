package com.campus.aihelp.domain;

import java.time.LocalDateTime;

public class HelpComment {
    private Long id;
    private Long helpId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private String userName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getHelpId() { return helpId; }
    public void setHelpId(Long helpId) { this.helpId = helpId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
