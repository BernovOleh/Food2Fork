package com.simpleapp.food2fork.datamodel;

import java.util.List;

public class RecipeDetails {

    private final String title;
    private final String imageUlr;
    private final List<String> ingredients;
    private final String sourceUrl;

    public RecipeDetails(String title, String imageUlr, List<String> ingredients, String sourceUrl) {
        this.title = title;
        this.imageUlr = imageUlr;
        this.ingredients = ingredients;
        this.sourceUrl = sourceUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUlr() {
        return imageUlr;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }
}
