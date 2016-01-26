package com.simpleapp.food2fork.datamodel;

public class Recipe {

    private final String recipeId;
    private final String imgUrl;
    private final double socialRank;
    private final String title;

    public Recipe(String recipeId, String imgUrl, double socialRank, String title) {
        this.recipeId = recipeId;
        this.imgUrl = imgUrl;
        this.socialRank = socialRank;
        this.title = title;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public double getSocialRank() {
        return socialRank;
    }

    public String getTitle() {
        return title;
    }
}
