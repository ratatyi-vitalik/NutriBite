package site.persistent.dto;

import java.util.ArrayList;
import java.util.List;

public class RecipeForm {

    private List<IngredientSectionDto> sections = new ArrayList<>();

    public RecipeForm() {
    }

    public List<IngredientSectionDto> getSections() {
        return sections;
    }

    public void setSections(List<IngredientSectionDto> sections) {
        this.sections = sections;
    }

    public static class IngredientSectionDto {
        private String title;
        private List<IngredientDto> ingredients = new ArrayList<>();

        public IngredientSectionDto() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<IngredientDto> getIngredients() {
            return ingredients;
        }

        public void setIngredients(List<IngredientDto> ingredients) {
            this.ingredients = ingredients;
        }
    }

    public static class IngredientDto {
        private String amount;
        private String name;

        public IngredientDto() {
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}