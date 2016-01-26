package com.simpleapp.food2fork.network;


import android.net.Uri;
import android.util.Log;

import com.simpleapp.food2fork.datamodel.Recipe;
import com.simpleapp.food2fork.datamodel.RecipeDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Food2ForkFetcher {

    private static final String API_KEY = "65cc02d6de2fe781764ce844fa61337a";

    private static final String urlSearch = "http://food2fork.com/api/search";
    private static final String urlGet = "http://food2fork.com/api/get";

    private static final String TAG = "Food2ForkFetcher";

    public List<Recipe> search(Integer param, String query) {
        List<Recipe> list = null;
        try {
            String url = Uri.parse(urlSearch).buildUpon()
                    .appendQueryParameter("key", API_KEY)
                    .appendQueryParameter("page", param.toString())
                    .appendQueryParameter("q", query)
                    .build().toString();
            String jsonString = getUrlString(url);
            list = parseRecipes(jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch recipes", ioe);
        }
        return list;
    }

    public RecipeDetails get(String recipeId) {
        RecipeDetails result = null;
        try {
            String url = Uri.parse(urlGet).buildUpon()
                    .appendQueryParameter("key", API_KEY)
                    .appendQueryParameter("rId", recipeId)
                    .build().toString();
            String jsonString = getUrlString(url);
            result = parseDetails(jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch recipes", ioe);
        }
        return result;
    }

    private RecipeDetails parseDetails(String jsonString) {
        RecipeDetails result = null;
        try {
            JSONObject responseJson = new JSONObject(jsonString);
            JSONObject recipe = responseJson.getJSONObject("recipe");
            String title = recipe.getString("title");
            String imageUrl = recipe.getString("image_url");
            String sourceUrl = recipe.getString("source_url");
            JSONArray ingredients = recipe.getJSONArray("ingredients");
            List<String> temp = new ArrayList<>();
            for (int i = 0; i < ingredients.length(); i++) {
                temp.add(ingredients.getString(i));
            }
            result = new RecipeDetails(title, imageUrl, temp, sourceUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<Recipe> parseRecipes(String jsonString) {
        List<Recipe> result = new ArrayList<>();
        try {
            JSONArray recipes = new JSONObject(jsonString).getJSONArray("recipes");
            for (int i = 0; i < recipes.length(); i++) {
                JSONObject recipe = recipes.getJSONObject(i);
                String title = recipe.getString("title");
                String imageUrl = recipe.getString("image_url");
                String recipeId = recipe.getString("recipe_id");
                double socialRank = recipe.getDouble("social_rank");
                result.add(new Recipe(recipeId, imageUrl, socialRank, title));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON " + e);
        }
        return result;
    }

    private String getUrlString(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        String result = null;
        try {
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            StringBuilder sb = new StringBuilder();
            String input;
            while ((input = reader.readLine()) != null) {
                sb.append(input);
            }
            result = sb.toString();
        } finally {
            connection.disconnect();
        }
        return result;
    }

}
