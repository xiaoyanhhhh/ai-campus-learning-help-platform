package com.campus.aihelp.domain;

import java.time.LocalDateTime;

public class HelpRequest {
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String tags;
    private Long publisherId;
    private Long helperId;
    private String status;
    private Integer bountyPoints;
    private LocalDateTime deadline;
    private String solution;
    private String evaluation;
    private LocalDateTime createdAt;
    private LocalDateTime claimedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private String courseName;
    private String publisherName;
    private String helperName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public Long getHelperId() { return helperId; }
    public void setHelperId(Long helperId) { this.helperId = helperId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getBountyPoints() { return bountyPoints; }
    public void setBountyPoints(Integer bountyPoints) { this.bountyPoints = bountyPoints; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
    public String getEvaluation() { return evaluation; }
    public void setEvaluation(String evaluation) { this.evaluation = evaluation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public String getHelperName() { return helperName; }
    public void setHelperName(String helperName) { this.helperName = helperName; }
}
