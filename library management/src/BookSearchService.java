package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class BookSearchService {

    public List<Book> searchBooks(String query) {
        List<Book> results = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String apiUrl = "https://openlibrary.org/search.json?q=" + encodedQuery;

            URI uri = URI.create(apiUrl);
            URL url = uri.toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse JSON
            JSONObject json = new JSONObject(response.toString());
            JSONArray docs = json.getJSONArray("docs");

            for (int i = 0; i < Math.min(docs.length(), 5); i++) {
                JSONObject bookJson = docs.getJSONObject(i);

                String title = bookJson.optString("title");
                JSONArray authorArr = bookJson.optJSONArray("author_name");
                String author = (authorArr != null && authorArr.length() > 0) ? authorArr.getString(0) : "Unknown";
                int year = bookJson.optInt("first_publish_year", -1);

                String genre = "N/A";
                String workKey = bookJson.optString("key");

                // Fetch genre from the work detail endpoint
                if (workKey != null && !workKey.isEmpty()) {
                    try {
                        URL workUrl = new URL("https://openlibrary.org" + workKey + ".json");
                        HttpURLConnection workConn = (HttpURLConnection) workUrl.openConnection();
                        workConn.setRequestMethod("GET");

                        BufferedReader workIn = new BufferedReader(new InputStreamReader(workConn.getInputStream()));
                        StringBuilder workResponse = new StringBuilder();
                        String line;
                        while ((line = workIn.readLine()) != null) {
                            workResponse.append(line);
                        }
                        workIn.close();

                        JSONObject workJson = new JSONObject(workResponse.toString());
                        JSONArray subjects = workJson.optJSONArray("subjects");
                        if (subjects != null && subjects.length() > 0) {
                            genre = subjects.getString(0);
                        }

                    } catch (Exception e) {
                        System.out.println("⚠️ Couldn't fetch genre for " + title);
                    }
                }

                Book book = new Book(title, author, genre, year);
                results.add(book);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
    public List<Book> recommendBooksByGenre(String genre) {
    List<Book> recommendations = new ArrayList<>();
    try {
        String encodedGenre = URLEncoder.encode(genre, "UTF-8");
        String apiUrl = "https://openlibrary.org/subjects/" + encodedGenre.toLowerCase() + ".json?limit=5";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        JSONObject json = new JSONObject(response.toString());
        JSONArray works = json.getJSONArray("works");

        for (int i = 0; i < works.length(); i++) {
            JSONObject work = works.getJSONObject(i);
            String title = work.optString("title", "Unknown Title");

            JSONArray authorsArray = work.optJSONArray("authors");
            String author = "Unknown";
            if (authorsArray != null && authorsArray.length() > 0) {
                author = authorsArray.getJSONObject(0).optString("name", "Unknown");
            }

            int year = -1; 
            Book book = new Book(title, author, genre, year);
            recommendations.add(book);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
    return recommendations;
}
public List<Book> recommendOnlineByGenre(String genre) {
    try {
        String subject = genre.toLowerCase()
                      .replaceAll("[^a-z ]", "") // remove punctuation, accents
                      .replace(" ", "_");         // replace spaces with underscores

        
        //String encoded = URLEncoder.encode(genre, "UTF-8");
        String apiUrl = "https://openlibrary.org/subjects/" + subject + ".json?limit=5";

        URL url = URI.create(apiUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        List<Book> books = new ArrayList<>();
        JSONObject json = new JSONObject(response.toString());
        JSONArray works = json.getJSONArray("works");

        for (int i = 0; i < Math.min(works.length(), 5); i++) {
            JSONObject work = works.getJSONObject(i);
            String title = work.optString("title", "Untitled");
            JSONArray authors = work.optJSONArray("authors");
            String author = (authors != null && authors.length() > 0) ?
                authors.getJSONObject(0).optString("name", "Unknown") : "Unknown";

            Book book = new Book(title, author, genre, -1);
            books.add(book);
        }

        return books;

    } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}

}
