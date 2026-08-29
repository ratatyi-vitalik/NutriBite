package site.persistent.entity;

import jakarta.persistence.*;
import site.persistent.entity.comments.Comment;
import site.persistent.entity.comments.Reply;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;
    
    private String message;

    private String link;

    private boolean isRead;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id")
    private Reply reply;

    public Notification() {
    }

    public Notification(User user, String title, String message) {
        this.user = user;
        this.title = title;
        this.message = message;
        isRead = false;
        createdAt = LocalDateTime.now();
    }

    public Notification(User user, String title, String message, String link) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.link = link;
        isRead = false;
        createdAt = LocalDateTime.now();
    }

    public Notification(User user, String title, String message, String link, Comment comment) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.link = link;
        this.comment = comment;
        isRead = false;
        createdAt = LocalDateTime.now();
    }

    public Notification(User user, String title, String message, String link, Reply reply) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.link = link;
        this.reply = reply;
        isRead = false;
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getLink() {
        return link;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public Reply getReply() {
        return reply;
    }

    public void setReply(Reply reply) {
        this.reply = reply;
    }
}