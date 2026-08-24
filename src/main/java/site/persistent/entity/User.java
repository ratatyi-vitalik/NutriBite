package site.persistent.entity;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import site.persistent.entity.recipes.Nutrition;
import site.persistent.entity.recipes.Recipe;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column
    private boolean subscribed;

    @Column
    private String avatarPath;

    @Column
    private LocalDateTime lastProposeRecipe;

    @ManyToMany(mappedBy = "saved")
    private List<Recipe> saved = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ban_id", referencedColumnName = "id")
    private Ban ban = null;

    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.subscribed = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    @NonNull
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public boolean isBanned() {
        if (ban == null) {
            return false;
        }
        return ban.getPeriod() != null && ban.getPeriod().isAfter(LocalDateTime.now());
    }

    public String getRemainingBanTimeFormatted() {
        if (!isBanned() || ban.getPeriod() == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime period = ban.getPeriod();

        Duration duration = Duration.between(now, period);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        StringBuilder remainingTime = new StringBuilder();
        if (days > 0) remainingTime.append(days).append(days == 1 ? " day " : " days ");
        if (hours > 0) remainingTime.append(hours).append(hours == 1 ? " hour " : " hours ");
        if (minutes > 0 || remainingTime.isEmpty()) remainingTime.append(minutes).append(minutes == 1 ? " minute" : " minutes");

        return remainingTime.toString().trim();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public LocalDateTime getLastProposeRecipe() {
        return lastProposeRecipe;
    }

    public void setLastProposeRecipe(LocalDateTime lastProposeRecipe) {
        this.lastProposeRecipe = lastProposeRecipe;
    }

    public List<Recipe> getSaved() {
        return saved;
    }

    public void setSaved(List<Recipe> saved) {
        this.saved = saved;
    }

    public Ban getBan() {
        return ban;
    }

    public void setBanned(Ban ban) {
        this.ban = ban;
    }
}
