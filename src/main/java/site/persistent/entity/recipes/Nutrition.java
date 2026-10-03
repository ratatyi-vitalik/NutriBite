package site.persistent.entity.recipes;

import jakarta.persistence.*;

@Entity
@Table(name = "nutrition_facts")
public class Nutrition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer calories;

    @Column(nullable = false)
    private Double protein;

    @Column(nullable = false)
    private Double fat;

    @Column(nullable = false)
    private Double carbs;

    @Column
    private Double fiber;

    @Column
    private Double sugar;

    @Column(name = "saturated_fat")
    private Double saturatedFat;

    @Column
    private Double sodium;

    public Nutrition() {
    }



    public Nutrition(Integer calories, Double carbs, Double fat, Double protein, Double fiber, Double sugar, Double saturatedFat, Double sodium) {
        this.calories = calories;
        this.carbs = carbs;
        this.fat = fat;
        this.protein = protein;
        this.fiber = fiber;
        this.sugar = sugar;
        this.saturatedFat = saturatedFat;
        this.sodium = sodium;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Double getCarbs() {
        return carbs;
    }

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getFiber() {
        return fiber;
    }

    public Double getSugar() {
        return sugar;
    }

    public Double getSaturatedFat() {
        return saturatedFat;
    }

    public Double getSodium() {
        return sodium;
    }

    public void setFiber(Double fiber) {
        this.fiber = fiber;
    }

    public void setSugar(Double sugar) {
        this.sugar = sugar;
    }

    public void setSaturatedFat(Double saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public void setSodium(Double sodium) {
        this.sodium = sodium;
    }
}