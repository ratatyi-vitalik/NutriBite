package site.persistent.entity.recipes;

import jakarta.persistence.*;
import site.persistent.entity.ProposedRecipe;
import site.persistent.entity.User;
import site.persistent.entity.comments.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private User author;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "prep_time")
    private Integer prepTime;

    @Column(name = "cook_time")
    private Integer cookTime;

    @Column
    private String servings;

    @Column(name = "video_path")
    private String videoPath;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "nutrition_id", referencedColumnName = "id", nullable = false)
    private Nutrition nutrition;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id", nullable = false)
    private List<IngredientSection> ingredientSections;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<Comment> comments;

    // Связь One-to-Many для шагов приготовления (с сохранением порядка)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    @OrderColumn(name = "step_order", nullable = false) // Гарантирует правильный порядок шагов (iterStat.count)
    private List<InstructionStep> instructions;

    @ManyToMany
    @JoinTable(
            name = "recipe-favs",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> saved = new HashSet<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Rating> ratings = new HashSet<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecipeView> views = new HashSet<>();

    public double getAverageRating() {
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }
        double avg = 0.0;
        for (Rating rating : ratings) {
            avg += rating.getScore();
        }
        return avg / ratings.size();
    }

    public int getUserScore(User user) {
        if (user == null || ratings == null) {
            return 0;
        }
        for (Rating rating : ratings) {
            if (rating.getUser().getId().equals(user.getId())) {
                return rating.getScore();
            }
        }
        return 0;
    }

    public Recipe() {
    }

    public Recipe(String title,
                          String description,
                          Integer prepTime,
                          Integer cookTime,
                          String servings,
                          User author,
                          LocalDateTime date,
                          Nutrition nutrition
    ) {
        this.title = title;
        this.description = description;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.servings = servings;
        this.author = author;
        this.date = date;
        this.nutrition = nutrition;
    }

    public Recipe(ProposedRecipe proposedRecipe) {
        this.title = proposedRecipe.getTitle();
        this.author = proposedRecipe.getAuthor();
        this.date = proposedRecipe.getDate();
        this.description = proposedRecipe.getDescription();
        this.imagePath = proposedRecipe.getImagePath();
        if (proposedRecipe.getVideoPath() != null) {
            this.videoPath = proposedRecipe.getVideoPath();
        }
        this.prepTime = proposedRecipe.getPrepTime();
        this.cookTime = proposedRecipe.getCookTime();
        this.servings = proposedRecipe.getServings();
        this.nutrition = proposedRecipe.getNutrition();
        this.ingredientSections = proposedRecipe.getIngredientSections();
        this.instructions = proposedRecipe.getInstructions();
    }

    private String formatTime(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return "0 mins";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;

        if (hours > 0) {
            if (mins > 0) {
                return String.format("%d hours %d mins", hours, mins); // или "%dh %dm"
            }
            return String.format("%d hours", hours);
        }
        return String.format("%d mins", mins);
    }

    // Вспомогательные геттеры для Thymeleaf (вызываются через recipe.formattedPrepTime)
    public String getFormattedPrepTime() {
        return formatTime(this.prepTime);
    }

    public String getFormattedCookTime() {
        return formatTime(this.cookTime);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Integer getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(Integer prepTime) {
        this.prepTime = prepTime;
    }

    public Integer getCookTime() {
        return cookTime;
    }

    public void setCookTime(Integer cookTime) {
        this.cookTime = cookTime;
    }

    public String getServings() {
        return servings;
    }

    public void setServings(String servings) {
        this.servings = servings;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public Nutrition getNutrition() {
        return nutrition;
    }

    public void setNutrition(Nutrition nutrition) {
        this.nutrition = nutrition;
    }

    public List<IngredientSection> getIngredientSections() {
        return ingredientSections;
    }

    public void setIngredientSections(List<IngredientSection> ingredientSections) {
        this.ingredientSections = ingredientSections;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<InstructionStep> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<InstructionStep> instructions) {
        this.instructions = instructions;
    }

    public  Set<User> getSaved() {
        return saved;
    }

    public void setSaved(Set<User> saved) {
        this.saved = saved;
    }

    public Set<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(Set<Rating> ratings) {
        this.ratings = ratings;
    }

    public Set<RecipeView> getViews() {
        return views;
    }

    public void setViews(Set<RecipeView> views) {
        this.views = views;
    }
}