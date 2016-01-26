package com.simpleapp.food2fork.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.simpleapp.food2fork.R;
import com.simpleapp.food2fork.datamodel.Recipe;
import com.simpleapp.food2fork.network.Food2ForkFetcher;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecipesGalleryFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "RecipesGalleryFragment";

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private List<Recipe> mDownloadedRecipeList = new ArrayList<>();
    private RecipesAdapter mCurrentAdapter;

    private String mCurrentQuery;
    private int mPageToDownload;
    private boolean mIsLastPageDownloaded;

    public static Fragment newInstance() {
        return new RecipesGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_recipe_gallery, container, false);

        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_gallery_recycler_view);
        setupRecyclerView(mRecyclerView);

        defaultSearchQuery();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_recipe_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                newSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mCurrentAdapter = new RecipesAdapter(filter(mDownloadedRecipeList, newText));
                mRecyclerView.swapAdapter(mCurrentAdapter, true);
                return true;
            }
        });

        final MenuItem clearSearch = menu.findItem(R.id.menu_item_clear);
        clearSearch.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                defaultSearchQuery();
                return true;
            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
    }

    private void defaultSearchQuery() {
        newSearchQuery("");
    }

    private void newSearchQuery(String query) {
        mPageToDownload = 1;
        mCurrentQuery = query;
        new Food2FortSearchTask().execute();
    }

    private List<Recipe> filter(List<Recipe> recipeList, String query) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : recipeList) {
            if (recipe.getTitle().toLowerCase().contains(query.toLowerCase())) {
                result.add(recipe);
            }
        }
        return result;
    }

    private void loadNextPage() {
        if (mIsLastPageDownloaded) {
            return;
        }
        mPageToDownload++;
        new Food2FortSearchTask().execute();
    }

    private void showConnectionProblemDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.connection_error_message)
                .setCancelable(false)
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Food2FortSearchTask().execute();
                    }
                })
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .show();
    }

    private boolean isSameQuery() {
        return mCurrentAdapter != null && mCurrentAdapter.mAdapterQuery.equals(mCurrentQuery);
    }

    private void updateRecipeList(List<Recipe> recipes) {
        if (recipes.size() == 0) {
            mIsLastPageDownloaded = true;
            return;
        }
        if (isSameQuery()) {
            mDownloadedRecipeList.addAll(recipes);
        } else {
            mIsLastPageDownloaded = false;
            mDownloadedRecipeList = recipes;
        }
    }

    private void resetRecipeAdapter() {
        if (!isAdded() || mIsLastPageDownloaded) {
            return;
        }
        mCurrentAdapter = new RecipesAdapter(mDownloadedRecipeList);
        if (isSameQuery()) {
            mRecyclerView.swapAdapter(mCurrentAdapter, false);
        } else {
            mRecyclerView.setAdapter(mCurrentAdapter);
        }
    }


    private class RecipesAdapter extends RecyclerView.Adapter<RecipeHolder> {

        private List<Recipe> mGalleryRecipesList;
        private String mAdapterQuery;

        private RecipesAdapter(List<Recipe> recipeList) {
            mAdapterQuery = mCurrentQuery;
            mGalleryRecipesList = recipeList;
        }

        @Override
        public RecipeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new RecipeHolder(view);
        }

        @Override
        public void onBindViewHolder(RecipeHolder holder, int position) {
            if (isLastDownloadedItemShown(position)) {
                loadNextPage();
            }
            Recipe recipe = mGalleryRecipesList.get(position);
            holder.bindRecipe(recipe);
        }

        @Override
        public int getItemCount() {
            return mGalleryRecipesList.size();
        }

        private boolean isLastDownloadedItemShown(int position) {
            return position == mDownloadedRecipeList.size() - 1;
        }

    }


    private class RecipeHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private TextView mTextView;

        private RecipeHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.gallery_item_image_view);
            mTextView = (TextView) itemView.findViewById(R.id.gallery_item_text_view);
        }

        private void bindRecipe(final Recipe recipe) {
            mTextView.setText(recipe.getTitle());
            Picasso.with(getActivity())
                    .load(recipe.getImgUrl())
                    .into(mImageView);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), RecipeDetailsActivity.class);
                    intent.putExtra("recipeId", recipe.getRecipeId());
                    startActivity(intent);
                }
            });
        }
    }


    private class Food2FortSearchTask extends AsyncTask<Void, Void, List<Recipe>> {

        @Override
        protected List<Recipe> doInBackground(Void... params) {
            return new Food2ForkFetcher().search(mPageToDownload, mCurrentQuery);
        }

        @Override
        protected void onPostExecute(List<Recipe> recipes) {
            if (recipes == null) {
                showConnectionProblemDialog();
                return;
            }
            updateRecipeList(recipes);
            resetRecipeAdapter();
        }

    }
}
