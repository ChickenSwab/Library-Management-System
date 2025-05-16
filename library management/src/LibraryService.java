// --- Updated LibraryService.java ---
package src;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryService implements LibraryManager {
    private Connection con;
    private User user;

    public LibraryService(Connection con, User user) {
        this.con = con;
        this.user = user;
    }

    @Override
    public void addBook(Book book) {
        String sql = "INSERT INTO books (title, author, genre, year, user_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, book.getTitle());
            pst.setString(2, book.getAuthor());
            pst.setString(3, book.getGenre());
            pst.setInt(4, book.getYear());
            pst.setInt(5, user.getId());
            pst.executeUpdate();

            System.out.println("✅ Book added: " + book.getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteBook(String title) throws SQLException {
        String query = "DELETE FROM books WHERE title = ? AND user_id = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, title);
            pst.setInt(2, user.getId());
            int rows = pst.executeUpdate();
            System.out.println(rows + " book(s) deleted.");
        }
    }

    @Override
    public void showBooks() throws SQLException {
        String query = "SELECT * FROM books WHERE user_id = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, user.getId());
            ResultSet rs = pst.executeQuery();

            System.out.println("Books in your library:");
            while (rs.next()) {
                System.out.printf("Title: %s | Author: %s | Genre: %s | Year: %d\n",
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getInt("year"));
            }
        }
    }

    @Override
    public void recommendBooks(String genre) {
        recommendBooks(genre, "");
    }

    @Override
    public void recommendBooks(String genre, String excludeTitle) {
        System.out.println("\n📚 Recommendations for genre: " + genre);

        String sql = "SELECT title, author FROM books WHERE genre = ? AND title != ? AND user_id = ? LIMIT 5";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, genre);
            pst.setString(2, excludeTitle);
            pst.setInt(3, user.getId());

            ResultSet rs = pst.executeQuery();

            int count = 0;
            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                System.out.printf("- %s by %s%n", title, author);
                count++;
            }

            if (count == 0) {
                System.out.println("No recommendations found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getAllGenres() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT DISTINCT genre FROM books WHERE genre IS NOT NULL AND genre != '' AND user_id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, user.getId());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                genres.add(rs.getString("genre"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }
}
