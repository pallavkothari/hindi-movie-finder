package com.pkothari.movies;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    private static ExecutorService googleSvc = Executors.newFixedThreadPool(2);
    private static ExecutorService omdbSvc = Executors.newFixedThreadPool(2);
    static Map<String, String> results = Maps.newConcurrentMap();

    private static class OmdbTask implements Runnable{

        private final String id;
        private final String toReturn;

        private OmdbTask(String id, String toReturn) {
            this.id = id;
            this.toReturn = toReturn;
        }

        @Override
        public void run() {
            try {
                if ("Hindi".equals(getMovieLanguageFromOmdb(id))) {
                    System.out.println("YAY!");
                    results.put(toReturn, "foo");
                } else {
                    System.out.println(id);
                }
            } catch (IOException e) {
                e.printStackTrace();    // oh well
            }

        }
    }

    private static class GoogleMoviesTask implements Runnable {

        // hardcoded to 94107 for now
        String url = "http://google.com/movies?near=94107&sort=1&start=";
        // in the above url, sort=1 specifies a movie-oriented view (rather than theater-oriented)
        // this will return the first page (10 results) starting from index=0, so I need to fetch all pages while there is new content.

        private int index;

        public GoogleMoviesTask(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            Document document = null;
            try {
                document = Jsoup.connect(url + index).get();
                Elements movies = document.getElementsByClass("movie");
                if (movies == null || movies.isEmpty()) {
                    return;
                }
                for (Element e : movies) {
                    process(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // just an HTML snippet from here will do for now
    public static String go() {

        Set<String> elements = null;
        try {
            elements = fetchAllMovieShowtimesNearMe();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String divs = emptyJoiner.join(elements);
        // fix img src's
        divs = divs.replaceAll("src=\"//ssl", "src=\"http://ssl");
        // fix other urls
        divs = divs.replaceAll("href=\"/", "href=\"http://google.com/");
        return divs;
    }

    private static Set<String> fetchAllMovieShowtimesNearMe() throws IOException, InterruptedException {
        int index = 0;

        List<Element> imdbLinks = Lists.newArrayList();
        for (int i = 0; i < 50; i+=10) {
            googleSvc.submit(new GoogleMoviesTask(i));
        }

        googleSvc.shutdown();
        googleSvc.awaitTermination(3, TimeUnit.SECONDS);
        omdbSvc.shutdown();
        omdbSvc.awaitTermination(3, TimeUnit.SECONDS);
        return results.keySet();
    }

    private static void process(Element movieDiv) throws IOException {
        Elements imDb = movieDiv.getElementsContainingOwnText("IMDb");
        if (!imDb.isEmpty()) {
            String id = getId(imDb.get(0).attr("href"));
            omdbSvc.submit(new OmdbTask(id, movieDiv.toString()));
        }
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
