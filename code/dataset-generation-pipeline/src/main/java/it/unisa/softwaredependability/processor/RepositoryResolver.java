package it.unisa.softwaredependability.processor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.unisa.softwaredependability.model.GithubMetadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RepositoryResolver {

    private static String getGithubApiMetadata(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
        );
        String inputLine;
        StringBuffer content = new StringBuffer();
        while((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();
        return content.toString();
    }

    private static GithubMetadata parseJson(String content) throws JsonSyntaxException {
        return new Gson().fromJson(content, GithubMetadata.class);
    }

    public static String resolveGithubApiUrl(String apiUrl) throws IOException {
        return parseJson(getGithubApiMetadata(apiUrl)).getHtml_url();
    }
}
