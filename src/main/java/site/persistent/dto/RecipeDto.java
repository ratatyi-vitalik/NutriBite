package site.persistent.dto;

public class RecipeDto {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private Double rating;
    private String categoryName;
    private String authorName;

    public RecipeDto(Long id, String title, String description, String imageUrl, Double rating, String categoryName, String authorName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.categoryName = categoryName;
        this.authorName = authorName;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Double getRating() {
        return rating;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}