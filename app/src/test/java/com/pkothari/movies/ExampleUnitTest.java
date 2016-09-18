package com.pkothari.movies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        String go = HindiMovieFinder.go();
        System.out.println("go = " + go);
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


}