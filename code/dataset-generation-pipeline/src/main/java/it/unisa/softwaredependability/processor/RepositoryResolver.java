package it.unisa.softwaredependability.processor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.unisa.softwaredependability.model.GithubMetadata;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class RepositoryResolver implements Serializable {

    private static RepositoryResolver repositoryResolver;

    private String githubUser;
    private String githubToken;

    public RepositoryResolver(String githubUser, String githubToken) {
        this.githubUser = githubUser;
        this.githubToken = githubToken;
    }

    public static RepositoryResolver getInstance(String githubUser, String githubToken) {
        if(repositoryResolver == null) repositoryResolver = new RepositoryResolver(githubUser, githubToken);
        return repositoryResolver;
    }

    private String getGithubApiMetadata(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", getBasicAuthHeader(githubUser, githubToken));
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

    private GithubMetadata parseJson(String content) throws JsonSyntaxException {
        return new Gson().fromJson(content, GithubMetadata.class);
    }

    public String resolveGithubApiUrl(String apiUrl) {
        try {
            return parseJson(getGithubApiMetadata(apiUrl)).getHtml_url();
        } catch(FileNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getBasicAuthHeader(String user, String password) {
        String userCredentials = new StringBuilder()
                .append(user).append(":").append(password).toString();
        return "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
    }
}
