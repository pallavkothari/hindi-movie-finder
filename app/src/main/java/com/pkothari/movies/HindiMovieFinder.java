package com.pkothari.movies;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by pkothari on 9/17/16.
 */
public class HindiMovieFinder {
    private static Joiner emptyJoiner = Joiner.on("").skipNulls();
    private static OkHttpClient client = new OkHttpClient();
    private static final Pattern imdbIdPattern = Pattern.compile("^.*?title/([A-Za-z0-9]+).*$");

    // just an HTML snippet from here will do for now
    public static String go() {

        List<Element> elements = null;
        try {
            elements = fetchAllMovieShowtimesNearMe();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String divs = emptyJoiner.join(elements);
        // fix img src's
        divs = divs.replaceAll("src=\"//ssl", "src=\"http://ssl");
        // fix other urls
        divs = divs.replaceAll("href=\"/", "href=\"http://google.com/");
        return divs;
    }

    private static List<Element> fetchAllMovieShowtimesNearMe() throws IOException {
        // hardcoded to 94107 for now
        String url = "http://google.com/movies?near=94107&sort=1&start=";
        // in the above url, sort=1 specifies a movie-oriented view (rather than theater-oriented)
        // this will return the first page (10 results) starting from index=0, so I need to fetch all pages while there is new content.

        int index = 0;
        List<Element> allMovies = Lists.newArrayList();
        List<Element> imdbLinks = Lists.newArrayList();
        while (true) {
            Document document = Jsoup.connect(url + index).get();
            Elements movies = document.getElementsByClass("movie");
            if (movies == null || movies.isEmpty()) {
                break;
            }
            for (Element e : movies) {
                Elements imDb = e.getElementsContainingOwnText("IMDb");
                if (!imDb.isEmpty()) {
                    String id = getId(imDb.get(0).attr("href"));
                    if ("Hindi".equals(getMovieLanguageFromOmdb(id)))
                        allMovies.add(e);
                }
            }
            index += 10;
        }
        return allMovies;
    }


    static String getMovieLanguageFromOmdb(String id) throws IOException {
        String url = "http://www.omdbapi.com/?i=" + id + "&plot=short&r=json";
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        String json = response.body().string();

        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        return jsonObject.get("Language").toString().replaceAll("\"","");
    }


    private static String getId(String href) {
        Matcher matcher = imdbIdPattern.matcher(href);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new Error();
    }
}
