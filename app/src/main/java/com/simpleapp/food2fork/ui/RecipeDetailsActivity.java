package com.simpleapp.food2fork.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.simpleapp.food2fork.network.Food2ForkFetcher;
import com.simpleapp.food2fork.R;
import com.simpleapp.food2fork.datamodel.RecipeDetails;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeDetailsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CollapsingToolbarLayout mToolbarLayout;
    private ImageView mImageView;
    private TextView mTitleTextView;
    private TextView mSourceTextView;
    private TextView mIngredientsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mToolbarLayout.setTitle("Recipe");
        mToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mTitleTextView = (TextView) findViewById(R.id.title_text_view);
        mImageView = (ImageView) findViewById(R.id.image_temp);
        mSourceTextView = (TextView) findViewById(R.id.source_link_text_view);
        mIngredientsTextView = (TextView) findViewById(R.id.ingredients_text_view);

        String recipeId = getIntent().getStringExtra("recipeId");
        new Food2FortGetTask().execute(recipeId);

    }

    private class Food2FortGetTask extends AsyncTask<String, Void, RecipeDetails> {

        @Override
        protected RecipeDetails doInBackground(String... params) {
            return new Food2ForkFetcher().get(params[0]);
        }

        @Override
        protected void onPostExecute(RecipeDetails recipeDetails) {
            if (recipeDetails == null) {
                showConnectionFailDialog();
                return;
            }
            showInfo(recipeDetails);
        }
    }

    private void showConnectionFailDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.connection_error_message)
                .setCancelable(false)
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String recipeId = getIntent().getStringExtra("recipeId");
                        new Food2FortGetTask().execute(recipeId);
                    }
                })
                .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void showInfo(final RecipeDetails recipeDetails) {
        Picasso.with(getBaseContext())
                .load(recipeDetails.getImageUlr())
                .into(mImageView);

        mTitleTextView.setText(recipeDetails.getTitle());

        mIngredientsTextView.setText(format(recipeDetails.getIngredients()));

        mSourceTextView.setText(recipeDetails.getSourceUrl());
        mSourceTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        mSourceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(recipeDetails.getSourceUrl()));
                startActivity(intent);
            }
        });
    }

    private String format(List<String> ingredients) {
        StringBuilder result = new StringBuilder();
        for (String ingredient : ingredients) {
            result.append("\t").append(ingredient).append('\n');
        }
        return result.toString();
    }

}
