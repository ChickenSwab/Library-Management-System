package src;
import java.sql.SQLException;
import java.util.List;

public interface LibraryManager {
    void addBook(Book book) throws SQLException;
    void deleteBook(String title) throws SQLException;
    void showBooks() throws SQLException;
    void recommendBooks(String genre); // for simple recommendations
    void recommendBooks(String genre, String excludeTitle); // to exclude a book
    List<String> getAllGenres();

}