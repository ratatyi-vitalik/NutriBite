package site.persistent.entity;

import jakarta.persistence.*;
import site.persistent.entity.recipes.Recipe;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "diets")
public class Diet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, name = "short_description", length = 500)
    private String shortDescription;

    @Column(nullable = false, name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Column(nullable = false, name = "allowed_foods", columnDefinition = "TEXT")
    private String allowedFoods;

    @Column(nullable = false, name = "forbidden_foods", columnDefinition = "TEXT")
    private String forbiddenFoods;

    @Column(nullable = false, name = "contraindications", columnDefinition = "TEXT")
    private String contraindications;

    @Column(nullable = false, name = "macronutrient_ratio")
    private String macronutrientRatio;

    @Column(nullable = false, name = "image_path")
    private String imagePath;

    @ManyToMany(mappedBy = "diets")
    private Set<Recipe> recipes = new HashSet<>();

    public Diet() {
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Set<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(Set<Recipe> recipes) {
        this.recipes = recipes;
    }

    public String getAllowedFoods() {
        return allowedFoods;
    }

    public String getForbiddenFoods() {
        return forbiddenFoods;
    }

    public String getContraindications() {
        return contraindications;
    }

    public String getMacronutrientRatio() {
        return macronutrientRatio;
    }

    public void setAllowedFoods(String allowedFoods) {
        this.allowedFoods = allowedFoods;
    }

    public void setContraindications(String contraindications) {
        this.contraindications = contraindications;
    }

    public void setForbiddenFoods(String forbiddenFoods) {
        this.forbiddenFoods = forbiddenFoods;
    }

    public void setMacronutrientRatio(String macronutrientRatio) {
        this.macronutrientRatio = macronutrientRatio;
    }
}