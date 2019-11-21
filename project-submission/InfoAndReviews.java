package com.cs255a.reviewapptest;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class InfoAndReviews extends AppCompatActivity {

    String query;
    Document doc = null;

    String linkString = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"; //http://stackoverflow.com/questions/163360/regular-expression-to-match-urls-in-java
    Pattern linkPattern = Pattern.compile(linkString, Pattern.CASE_INSENSITIVE);

    String rottenRating = null;
    String metaRating = null;
    String ebertRating = null;
    String empireRating = null;

    String title = null;
    String summary = null;
    String MPAArated = null;
    String genre = null;
    String director = null;
    String writer = null;
    String release = null;
    String imageURL = null;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_info);

        image = (ImageView) findViewById(R.id.image);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();

        rottenRating = intent.getStringExtra("rottenTomatoes");
        metaRating = intent.getStringExtra("metacritic");
        ebertRating = intent.getStringExtra("ebert");
        empireRating = intent.getStringExtra("empire");
        imageURL = intent.getStringExtra("poster");


        Picasso.get().load(imageURL).into(image);

        ArrayList<String> list = new ArrayList<>();
        if (rottenRating != null)
            list.add(rottenRating);
        if (metaRating != null)
            list.add(metaRating);
        if (ebertRating != null)
            list.add(ebertRating);
        if (empireRating != null)
            list.add(empireRating);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.row_item, R.id.listItem, list);

        final ListView listView = findViewById(android.R.id.list);
        listView.setAdapter(adapter);

        title = intent.getStringExtra("title");
        summary = intent.getStringExtra("summary");
        MPAArated = intent.getStringExtra("rating");
        genre = intent.getStringExtra("genre");
        director = intent.getStringExtra("director");
        writer = intent.getStringExtra("writer");
        release = intent.getStringExtra("release");

        ArrayList<String> list2 = new ArrayList<>();
        if(MPAArated != null)
            list2.add(MPAArated);
        if(genre != null)
            list2.add(genre);
        if(director != null)
            list2.add(director);
        if(writer != null)
            list2.add(writer);
        if(release != null)
            list2.add(release);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getApplicationContext(), R.layout.row_item, R.id.listItem, list2);

        final ListView listView2 = (ListView) findViewById(R.id.list2);
        listView2.setAdapter(adapter2);

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(summary);

        TextView textView2 = (TextView) findViewById(R.id.textView2);
        textView2.setText(title);


        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            new AsyncSearch().execute();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setQuery(title, false);//!!!!

        return true;
    }

    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;

    @Override
    protected Dialog onCreateDialog(int id) { //https://stackoverflow.com/questions/18069678/how-to-use-asynctask-to-display-a-progress-bar-that-counts-down
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Processing...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    private class AsyncSearch extends AsyncTask<Void, String, Void>{ //https://wingoodharry.wordpress.com/2014/08/11/using-jsoup-with-android/

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String temp = query.replaceAll(" ", "-");
                String googleSearch = "http://www.google.com/search?q=" + temp + "-movie-reviews";
                doc = Jsoup.connect(googleSearch).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get();
                Element links = doc.select("a[href*=/m/]").first();
                Element metaLinks = doc.select("a[href*=metacritic.com/movie/]").first();
                Element ebertLinks = doc.select("a[href*=rogerebert.com/reviews/]").first();
                Element empireLinks = doc.select("a[href*=empireonline.com/movies/]").first();

                String rottenLink = null;
                Matcher linkMatch;

                if(links != null) {
                    rottenLink = links.attr("abs:href"); // Rotten Tomatoes Link
                    linkMatch = linkPattern.matcher(rottenLink);
                    if (linkMatch.find()) {
                        doc = Jsoup.connect(rottenLink).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11").get();
                        Element rottenScore = doc.select("#tomato_meter_link > span.meter-value.superPageFontColor > span").first();
                        rottenRating = "Rotten Tomatoes: " + rottenScore.html();
                    }
                }

                if(metaLinks != null) {
                    String metacriticLink = metaLinks.attr("abs:href"); // Metacritic Link
                    linkMatch = linkPattern.matcher(metacriticLink);
                    if (linkMatch.find()) {
                        doc = Jsoup.connect(metacriticLink).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11").get();
                        Element metaScore = doc.select("#nav_to_metascore > div:nth-child(2) > div.distribution > div.score.fl > a > div").first();
                        metaRating = "Metacritic: " + metaScore.html();
                    }
                }

                if(ebertLinks != null) {
                    String ebertLink = ebertLinks.attr("abs:href"); // Roger Ebert Link
                    linkMatch = linkPattern.matcher(ebertLink);
                    if (linkMatch.find()) {
                        doc = Jsoup.connect(ebertLink).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11").get();
                        Element ebertScore = doc.select("#review > div.wrapper > div > section > article > header > p > span > span").first();
                        ebertRating = "Roger Ebert: " + ebertScore.html();
                        ebertRating = ebertRating.replaceAll("<i class=\"icon-star-full\"></i>", "★"); // ? = full star
                        ebertRating = ebertRating.replaceAll("<i class=\"icon-star-half\"></i>", "½"); // ! = half a star
                    }
                }

                if(empireLinks != null) {
                    String empireLink = empireLinks.attr("abs:href"); // Empire Link
                    linkMatch = linkPattern.matcher(empireLink);
                    if (linkMatch.find()) {
                        doc = Jsoup.connect(empireLink).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11").get();
                        Elements empireScore = doc.select("body > main > article > div > div > div.epsilon.col.w-2-3.article__body > div.no-marg.subtitle > div > span"); // using selector syntax
                        empireRating = "Empire: " + empireScore.html();
                    }
                }

                if(rottenLink != null) {
                    doc = Jsoup.connect(rottenLink).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11").get();

                    Element imagePull = doc.select("#movie-image-section > div > img").first();
                    if (imagePull != null) {
                        imageURL = imagePull.attr("src");
                        System.out.println(imageURL);
                    } else {
                        imagePull = doc.select("#poster_link > img").first();
                        imageURL = imagePull.attr("src");
                        System.out.println(imageURL);
                    }

                    Element titlePull = doc.select("#movie-title").first();
                    title = titlePull.text();

                    Elements summaryPull = doc.select("#movieSynopsis");
                    summary = summaryPull.html();

                    Elements mpaaRating = doc.select("#mainColumn > section.panel.panel-rt.panel-box.movie_info.media > div > div.panel-body.content_body > ul > li:nth-child(1) > div.meta-value");
                    MPAArated = "Rated: " + mpaaRating.html();

                    Elements genrePull = doc.select("#mainColumn > section.panel.panel-rt.panel-box.movie_info.media > div > div.panel-body.content_body > ul > li:nth-child(2) > div.meta-value");
                    genre = "Genre: " + genrePull.text();

                    Elements directorPull = doc.select("#mainColumn > section.panel.panel-rt.panel-box.movie_info.media > div > div.panel-body.content_body > ul > li:nth-child(3) > div.meta-value");
                    director = "Directed By: " + directorPull.text();

                    Elements writerPull = doc.select("#mainColumn > section.panel.panel-rt.panel-box.movie_info.media > div > div.panel-body.content_body > ul > li:nth-child(4) > div.meta-value");
                    writer = "Written By: " + writerPull.text();

                    Elements releasePull = doc.select("#mainColumn > section.panel.panel-rt.panel-box.movie_info.media > div > div.panel-body.content_body > ul > li.meta-row.clearfix.js-theater-release-dates > div.meta-value > time");
                    release = "Released: " + releasePull.html();
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(Void result) {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);

            Intent intent = new Intent(getApplicationContext(), InfoAndReviews.class);
            intent.putExtra("rottenTomatoes",rottenRating );
            intent.putExtra("metacritic",metaRating );
            intent.putExtra("ebert",ebertRating );
            intent.putExtra("empire",empireRating );
            intent.putExtra("poster", imageURL);
            intent.putExtra("rating", MPAArated);
            intent.putExtra("title", title);
            intent.putExtra("summary", summary);
            intent.putExtra("genre", genre);
            intent.putExtra("director", director);
            intent.putExtra("writer", writer);
            intent.putExtra("release", release);
            startActivity(intent);
        }
    }
}