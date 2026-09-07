package site.persistent.entity.recipes;

import jakarta.persistence.*;
import site.persistent.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_views")
public class RecipeView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime viewedAt;

    public RecipeView() {
    }

    public RecipeView(Recipe recipe, String ipAddress) {
        this.recipe = recipe;
        this.ipAddress = ipAddress;
        this.viewedAt = LocalDateTime.now();
    }

    public RecipeView(Recipe recipe, User user) {
        this.recipe = recipe;
        this.user = user;
        this.viewedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public User getUser() {
        return user;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}