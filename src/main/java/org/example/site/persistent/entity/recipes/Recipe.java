package org.example.site.persistent.entity.recipes;

import jakarta.persistence.*;
import org.example.site.persistent.entity.User;
import org.example.site.persistent.entity.comments.Comment;

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

    // В зависимости от логики можно использовать LocalDate/LocalDateTime
    @Column(nullable = false)
    private String date;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "prep_time")
    private String prepTime;

    @Column(name = "cook_time")
    private String cookTime;

    @Column
    private String servings;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "nutrition_id", referencedColumnName = "id")
    private Nutrition nutrition;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<IngredientSection> ingredientSections;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<Comment> comments;

    // Связь One-to-Many для шагов приготовления (с сохранением порядка)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    @OrderColumn(name = "step_order") // Гарантирует правильный порядок шагов (iterStat.count)
    private List<InstructionStep> instructions;

    @ManyToMany
    @JoinTable(
            name = "recipe-favs",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> saved = new HashSet<>();

    public Recipe() {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getCookTime() {
        return cookTime;
    }

    public void setCookTime(String cookTime) {
        this.cookTime = cookTime;
    }

    public String getServings() {
        return servings;
    }

    public void setServings(String servings) {
        this.servings = servings;
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
}