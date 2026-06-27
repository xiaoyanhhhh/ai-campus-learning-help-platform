package com.campus.aihelp.domain;

import java.time.LocalDateTime;

public class PointRecord {
    private Long id;
    private Long userId;
    private Integer changeValue;
    private String source;
    private Long bizId;
    private String remark;
    private LocalDateTime createdAt;
    private String realName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getChangeValue() { return changeValue; }
    public void setChangeValue(Integer changeValue) { this.changeValue = changeValue; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
}
