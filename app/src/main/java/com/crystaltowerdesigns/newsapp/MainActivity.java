package com.crystaltowerdesigns.newsapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
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
import java.util.Calendar;

/**
 * News App (Stage 1)
 * <p>
 * Created by Jeff Palutke on 7/22/2018
 * Revised 8/8/2018
 */

public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<ArrayList<NewsEntry>> {

    private final ArrayList<NewsEntry> newsEntryList = new ArrayList<>();
    private final MyNetworkTools network = new MyNetworkTools(this);
    private SharedPreferences sharedPreferences;
    private android.support.v7.widget.RecyclerView recyclerView;
    private TextView statusTextView;
    private NewsEntryAdapter adapter;
    private int daysPast = -1;
    private int pageSize = -1;
    private String searchKey = "";

    /**
     * {@LINK buildURI}
     *
     * @return String containing the modified search URL with the user designated search string inserted into it
     */
    private String buildURI() {
        // set calendar to today's date
        java.util.Calendar calendar = Calendar.getInstance();
        // subtract a day for each 'daysPast' specified bby the user
        for (int day = 1; day < daysPast; day++)
            calendar.roll(Calendar.DATE, false);
        // format into a date string like 2018-01-23
        @SuppressLint("DefaultLocale") String targetFromDate = String.format("%tY-%<tm-%<td", calendar.getTime());

        // see: Hiding API keys from your Android repository
        //  https://medium.com/code-better/hiding-api-keys-from-your-android-repository-b23f5598b906
        String ApiKey = BuildConfig.ApiKey;

        // create a URI to request the desired news from the server
        Uri.Builder builder = new Uri.Builder();
        //noinspection SpellCheckingInspection
        builder.scheme("https")
                .authority("content.guardianapis.com")
                .appendPath("search")
                .appendQueryParameter("q", "\"" + searchKey + "\"")
                .appendQueryParameter("page-size", "" + pageSize)
                .appendQueryParameter("show-tags", "contributor")
                .appendQueryParameter("from-date", targetFromDate)
                .appendQueryParameter("orderBy", "newest")
                .appendQueryParameter("api-key", ApiKey);
        return builder.build().toString();
    }

    private boolean PreferenceUnequalInt(int resourceID, int value) {
        if (sharedPreferences.contains(getString(resourceID))) {
            String storedInt = sharedPreferences.getString(getString(resourceID), "0");
            return !(storedInt.equals("" + value));
        }
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean PreferenceUnequalString(int resourceID, String value) {
        if (sharedPreferences.contains(getString(resourceID))) {
            String storedString = sharedPreferences.getString(getString(resourceID), "");
            return !storedString.equals("" + value);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If we haven't yet set the defaults from
        // our strings file then set the default values
        if (searchKey.length() == 0)
            searchKey = getString(R.string.search_text_default);
        if (pageSize < 1)
            pageSize = Integer.parseInt(this.getString(R.string.page_size_default));
        if (daysPast < 0)
            daysPast = Integer.parseInt(this.getString(R.string.days_prior_default));

        // get access to our preferences/settings
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // if settings/preferences are missing or differ from the current
        // variable values a refresh of the news source is needed.
        boolean refreshNeeded = PreferenceUnequalInt(R.string.days_prior_key, daysPast) ||
                PreferenceUnequalInt(R.string.page_size_key, pageSize) ||
                PreferenceUnequalString(R.string.search_text_key, searchKey);
        if (refreshNeeded) {
            int newDaysPast = Integer.parseInt(sharedPreferences.getString(getString(R.string.days_prior_key), "-1"));
            int newPageSize = Integer.parseInt(sharedPreferences.getString(getString(R.string.page_size_key), "-1"));
            String newSearchKey = sharedPreferences.getString(getString(R.string.search_text_key), "");
            // if values were found in the preferences then update our variables
            if (newDaysPast >= 0)
                daysPast = newDaysPast;
            if (newPageSize >= 1)
                pageSize = newPageSize;
            if (newSearchKey.length() > 0)
                searchKey = newSearchKey;
        }

        // add our toolbar
        Toolbar myToolbar = findViewById(R.id.news_app_toolbar);
        setSupportActionBar(myToolbar);

        // point to our views
        statusTextView = findViewById(R.id.status_textView);
        recyclerView = findViewById(R.id.recycler_view);

        // display network Offline or Loading News
        // in our status view.
        if (network.Offline()) {
            statusTextView.setText(getString(R.string.loading_news));
        } else {
            statusTextView.setText(getString(R.string.no_network_instructions));
        }

        // check to see if our search settings have been altered.
        // If they have, then reset the loader.
        if (refreshNeeded) {
            // restart loader / load fresh news
            android.support.v4.app.LoaderManager.getInstance(MainActivity.this).restartLoader(0, null, MainActivity.this).forceLoad();

        } else
            // existing loader / load news
            android.support.v4.app.LoaderManager.getInstance(this).initLoader(0, null, this).forceLoad();

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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsActivityIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsActivityIntent);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public android.support.v4.content.Loader<ArrayList<NewsEntry>> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new NewsLoader(MainActivity.this, buildURI());
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
        final Context context;

        NewsLoader(Context context, String url) {
            super(context);
            this.url = url;
            this.context = context;
        }

        @Override
        public ArrayList<NewsEntry> loadInBackground() {
            ArrayList<NewsEntry> newNewsEntryList = new ArrayList<>();
            HttpHandler httpHandler = new HttpHandler(context);
            String jsonString;

            // Ask server for the json response
            // store it in jsonString
            try {
                jsonString = httpHandler.makeHttpRequest(createUrl(url));
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
                    JSONArray jsonArrayTags;

                    // looping through all entries
                    for (int i = 0; i < jsonArrayNewsEntries.length(); i++) {
                        JSONObject NewsEntryJSONObject = jsonArrayNewsEntries.getJSONObject(i);

                        // check tags for contributors
                        jsonArrayTags = NewsEntryJSONObject.getJSONArray("tags");

                        StringBuilder contributors = new StringBuilder();
                        String lastName;
                        String firstName;

                        for (int i2 = 0; i2 < jsonArrayTags.length(); i2++) {
                            JSONObject jsonArrayTagsJSONObject = jsonArrayTags.getJSONObject(i2);
                            // is this a contributor?
                            if (jsonArrayTagsJSONObject.getString("type").equals("contributor")) {
                                lastName = jsonArrayTagsJSONObject.getString("lastName");
                                firstName = jsonArrayTagsJSONObject.getString("firstName");
                                // if we found previous contributors,
                                // separate with a space/pipe/space sequence
                                if (!contributors.toString().equals(""))
                                    contributors.append(" | ").append(firstName);
                                else
                                    contributors = new StringBuilder(firstName);
                                if (!firstName.equals(""))
                                    contributors.append(" ").append(lastName);
                            }
                        }

                        // InitCap the combined names
                        String[] splitName = contributors.toString().toLowerCase().split(" ");
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i3 = 0; i3 < splitName.length; i3++) {
                            String individualWord = splitName[i3];
                            if (i3 > 0 && individualWord.length() > 0) {
                                stringBuilder.append(" ");
                            }
                            if (individualWord.length() > 0)
                                stringBuilder.append(individualWord.substring(0, 1).toUpperCase()).append(individualWord.substring(1));
                        }
                        contributors = new StringBuilder(stringBuilder.toString());

                        // add each child node to our newNewsEntryList
                        newNewsEntryList.add(new NewsEntry(
                                NewsEntryJSONObject.getString("id"),
                                NewsEntryJSONObject.getString("webTitle"),
                                NewsEntryJSONObject.getString("sectionName"),
                                NewsEntryJSONObject.getString("webUrl"),
                                contributors.toString(),
                                NewsEntryJSONObject.getString("webPublicationDate")
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
                        newNewsEntryList.add(new NewsEntry(TAG, getContext().getString(R.string.url_connection_response) + errNumber + ": " + errMsg,
                                getContext().getString(R.string.error), getContext().getString(R.string.error),
                                getContext().getString(R.string.error), getContext().getString(R.string.error)));
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
