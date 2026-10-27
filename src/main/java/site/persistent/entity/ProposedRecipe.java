package site.persistent.entity;

import jakarta.persistence.*;
import site.persistent.entity.recipes.IngredientSection;
import site.persistent.entity.recipes.InstructionStep;
import site.persistent.entity.recipes.Nutrition;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "proposed_recipes")
public class ProposedRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, name = "prep_time")
    private Integer prepTime;

    @Column(nullable = false, name = "cook_time")
    private Integer cookTime;

    @Column(nullable = false)
    private String servings;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "video_path")
    private String videoPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "author_id")
    private User author;

    @Column(nullable = false)
    private LocalDateTime date;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "nutrition_id", referencedColumnName = "id")
    private Nutrition nutrition;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<IngredientSection> ingredientSections;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    @OrderColumn(name = "step_order")
    private List<InstructionStep> instructions;

    public ProposedRecipe() {
    }

    public ProposedRecipe(String title,
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
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

    public List<InstructionStep> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<InstructionStep> instructions) {
        this.instructions = instructions;
    }
}