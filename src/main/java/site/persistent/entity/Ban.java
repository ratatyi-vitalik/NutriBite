package site.persistent.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Ban {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String reason;

    @Column
    private LocalDateTime period;

    public Ban() {

    }

    public Ban(LocalDateTime period) {
        reason = "Banned by admin's decision";
        this.period = period;
    }

    public Ban(String reason, LocalDateTime period) {
        this.reason = reason;
        this.period = period;
    }

    public Long getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getPeriod() {
        return period;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setPeriod(LocalDateTime period) {
        this.period = period;
    }
}
