package com.pkothari.movies;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.pkothari.movies.HindiMovieFinder.getMovieLanguageFromOmdb;
import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void buildHtml() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        System.out.println(HindiMovieFinder.go());
        System.out.println("stopwatch = " + stopwatch);
    }

    @Test
    public void testRegex() {
        String href = "/url?q=http://www.imdb.com/title/tt4773934/&sa=X&oi=moviesi&ii=0&usg=AFQjCNG8LSUAaAQGyhXDBBip4rE50DJDyQ";
        Pattern pattern = Pattern.compile("^.*?title/([A-Za-z0-9]+).*$");

        Matcher matcher = pattern.matcher(href);
        if (matcher.matches()) {
            System.out.println("matcher.group(1) = " + matcher.group(1));
        }
    }

    @Test
    public void testLanguage() throws IOException {
        String id = "tt5197544";
        String language = getMovieLanguageFromOmdb(id);
        System.out.println("language = " + language);
    }

    @Test
    public void findMovieName() throws Exception {
        String url = "http://google.com/movies?near=94107&sort=1&start=1";
        Document document = Jsoup.connect(url).get();
        Elements movies = document.getElementsByClass("movie");
        Elements nameElement = movies.get(0).getElementsByAttributeValue("itemprop", "name");
        String text = nameElement.get(0).text();
        System.out.println("nameElement = " + text);
    }

}