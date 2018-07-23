package com.crystaltowerdesigns.newsapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * News App (Stage 1)
 * <p>
 * Created by Jeff Palutke on 7/22/2018
 */
public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<ArrayList<NewsEntry>> {

    private final ArrayList<NewsEntry> newsEntryList = new ArrayList<>();
    private final String defaultSearchKey = "US News";
    private final MyNetworkTools network = new MyNetworkTools(this);
    private android.support.v7.widget.RecyclerView recyclerView;
    private TextView statusTextView;
    private NewsEntryAdapter adapter;
    private String searchKey = defaultSearchKey;

    /**
     * {@LINK searchURL}
     *
     * @return String containing the modified search URL with the user designated search string inserted into it
     */
    private String searchURL() {
        String url = this.getString(R.string.base_search_url);
        return (url.replace(getString(R.string.search_key_marker), searchKey));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add our toolbar
        Toolbar myToolbar = findViewById(R.id.news_app_toolbar);
        setSupportActionBar(myToolbar);

        // point to our views
        statusTextView = findViewById(R.id.status_textView);
        recyclerView = findViewById(R.id.recycler_view);

        // load our news
        android.support.v4.app.LoaderManager.getInstance(this).initLoader(0, null, this).forceLoad();

        // display network Offline or Loading News
        // in our status view.
        if (network.Offline()) {
            statusTextView.setText(getString(R.string.loading_news));
        } else {
            statusTextView.setText(getString(R.string.no_network_instructions));
        }

        // create our data adapter
        adapter = new NewsEntryAdapter(this, newsEntryList, new NewsEntryAdapter.OnItemClickListener() {
            // set our onClickListener
            @Override
            public void onItemClick(NewsEntry newsEntry) {
            }
        });

        // setup our recycler view
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        // if records exist, show the recycler and hide the status text, else the reverse effect
        if (adapter.getItemCount() > 0) {
            statusTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            statusTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView =
                (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_for));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // use the user specified query or
                // default search if nothing was entered
                if (query.equals(""))
                    searchKey = defaultSearchKey;
                else
                    searchKey = query;
                // load the news based on the above search pattern
                android.support.v4.app.LoaderManager.getInstance(MainActivity.this).restartLoader(0, null, MainActivity.this).forceLoad();
                // hide the soft keyboard
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @NonNull
    @Override
    public android.support.v4.content.Loader<ArrayList<NewsEntry>> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new NewsLoader(MainActivity.this, searchURL());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<ArrayList<NewsEntry>> loader, ArrayList<NewsEntry> newsEntries) {
        adapter = new NewsEntryAdapter(this, newsEntries, new NewsEntryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NewsEntry newsEntry) {
                // Launch a browser to the URL
                String url = newsEntry.getWebURL();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        recyclerView.setAdapter(adapter);

        if (adapter.getItemCount() == 1) {
            NewsEntry news = newsEntries.get(0);
            // is this one of our custom error notations?
            // the 3 fields checked will contain getString(R.string.error)
            if (news.getWebURL().equals(getString(R.string.error)) &&
                    news.getWebPublicationDate().equals(getString(R.string.error)) &&
                    news.getSectionName().equals(getString(R.string.error))) {
                statusTextView.setText(news.getWebTitle() + getString(R.string.search_pattern_alert));
                statusTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), news.getWebTitle(), Toast.LENGTH_SHORT).show();
                newsEntries.clear();
                adapter.notifyDataSetChanged();
            }
        } else {
            // are there any items to display?
            if (adapter.getItemCount() > 0) {
                statusTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                // we have an empty list...
                // are we online?
                if (network.Offline()) {
                    statusTextView.setText(getString(R.string.no_network_instructions));
                    Toast.makeText(getApplicationContext(), getString(R.string.no_network_instructions), Toast.LENGTH_SHORT).show();
                } else
                    // online, but no news loaded
                    statusTextView.setText(getString(R.string.no_news_loaded));
                // hide the recyclerView and show the statusTextView
                statusTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<ArrayList<NewsEntry>> loader) {
        adapter = new NewsEntryAdapter(this, new ArrayList<NewsEntry>(), new NewsEntryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NewsEntry newsEntry) {
            }
        });
    }

    static class NewsLoader extends AsyncTaskLoader<ArrayList<NewsEntry>> {

        private final String TAG = MainActivity.class.getSimpleName();
        private final String url;

        NewsLoader(Context context, String url) {
            super(context);
            this.url = url;
        }

        @Override
        public ArrayList<NewsEntry> loadInBackground() {
            ArrayList<NewsEntry> newNewsEntryList = new ArrayList<>();
            HttpHandler sh = new HttpHandler();
            String jsonString;

            // Ask server for the json response
            // store it in jsonString
            try {
                jsonString = sh.makeHttpRequest(createUrl(url));
            } catch (IOException e) {
                return null;
            }

            // null or empty mean no entries found,
            // jsonString.startsWith R.string.connection_base_error_message means we encountered a httpResponse error
            // in any of these cases, there is nothing to process for entries
            if (jsonString != null && !jsonString.equals("") && !jsonString.startsWith(getContext().getString(R.string.connection_base_error_message))) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonString);
                    JSONObject jsonResponse = jsonObj.getJSONObject("response");
                    JSONArray jsonArrayNewsEntries = jsonResponse.getJSONArray("results");

                    // looping through all entries
                    for (int i = 0; i < jsonArrayNewsEntries.length(); i++) {
                        JSONObject c = jsonArrayNewsEntries.getJSONObject(i);

                        // add each child node to our newNewsEntryList
                        newNewsEntryList.add(new NewsEntry(
                                c.getString("id"),
                                c.getString("webTitle"),
                                c.getString("sectionName"),
                                c.getString("webUrl"),
                                c.getString("webPublicationDate")
                        ));
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, e.toString());
                }
            } else {
                if (jsonString != null)
                    // if an error was encountered/returned we will return it as the only item in the list
                    if (jsonString.startsWith(getContext().getString(R.string.connection_base_error_message))) {
                        String connection_base_error_message = getContext().getString(R.string.connection_base_error_message);
                        String errNumber = jsonString.substring(connection_base_error_message.length(), jsonString.lastIndexOf("|"));
                        String errMsg = jsonString.substring(jsonString.lastIndexOf("|") + 1);
                        newNewsEntryList.add(new NewsEntry(TAG, getContext().getString(R.string.url_connection_response) + errNumber + ": " + errMsg, getContext().getString(R.string.error), getContext().getString(R.string.error), getContext().getString(R.string.error)));
                    }
            }
            return newNewsEntryList;
        }

        private URL createUrl(String stringUrl) {
            URL url;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                return null;
            }
            return url;
        }

    }
}
