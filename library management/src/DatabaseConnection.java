// --- Final Updated DatabaseConnection.java ---
package src;

import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class DatabaseConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/library_db";
        String username = "root";
        String password = "llama123";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(url, username, password)) {
                Scanner scanner = new Scanner(System.in);

                // Prompt for user and set up manager
                User currentUser = getOrCreateUser(con, scanner);
                if (currentUser == null) {
                    System.out.println("Failed to log in or create user.");
                    return;
                }

                LibraryManager manager = new LibraryService(con, currentUser);
                BookSearchService searchService = new BookSearchService();

                int choice;
                do {
                    System.out.println("1. Add Book\n2. Delete Book\n3. Show Books\n4. Search Online\n5. Recommend by Genre\n6. Personal Suggestions\n0. Exit");
                    System.out.print("Enter choice: ");
                    choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            System.out.print("Title: ");
                            String title = scanner.nextLine();
                            System.out.print("Author: ");
                            String author = scanner.nextLine();
                            System.out.print("Genre: ");
                            String genre = scanner.nextLine();
                            System.out.print("Year: ");
                            int year = scanner.nextInt();
                            scanner.nextLine();
                            manager.addBook(new Book(title, author, genre, year));
                            break;
                        case 2:
                            System.out.print("Title to delete: ");
                            String delTitle = scanner.nextLine();
                            manager.deleteBook(delTitle);
                            break;
                        case 3:
                            manager.showBooks();
                            break;
                        case 4:
                            System.out.print("Enter search query: ");
                            String query = scanner.nextLine();
                            var results = searchService.searchBooks(query);
                            if (results.isEmpty()) {
                                System.out.println("No books found.");
                                break;
                            }
                            for (int i = 0; i < results.size(); i++) {
                                Book b = results.get(i);
                                System.out.printf("%d. %s by %s (%d)\n", i + 1, b.getTitle(), b.getAuthor(), b.getYear());
                            }
                            System.out.print("Select a book to add (number): ");
                            int selection = scanner.nextInt();
                            scanner.nextLine();
                            if (selection >= 1 && selection <= results.size()) {
                                manager.addBook(results.get(selection - 1));
                            } else {
                                System.out.println("Invalid selection.");
                            }
                            break;
                        case 5:
                            System.out.print("Enter genre for recommendations: ");
                            String genreQuery = scanner.nextLine();
                            manager.recommendBooks(genreQuery);
                            break;
                        case 6:
                            List<String> genres = manager.getAllGenres();
                            for (String g : genres) {
                                System.out.println("\n📚 Online recommendations for genre: " + g);
                                List<Book> onlineRecs = searchService.recommendOnlineByGenre(g);
                                if (onlineRecs.isEmpty()) {
                                    System.out.println("No results found.");
                                } else {
                                    for (Book b : onlineRecs) {
                                        System.out.printf("→ %s by %s (%d) [%s]\n",
                                                b.getTitle(), b.getAuthor(), b.getYear(), b.getGenre());
                                    }
                                }
                            }
                            break;
                        case 0:
                            System.out.println("Exiting...");
                            break;
                        default:
                            System.out.println("Invalid choice!");
                    }
                } while (choice != 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static User getOrCreateUser(Connection con, Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        try {
            String findSql = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement pst = con.prepareStatement(findSql)) {
                pst.setString(1, username);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    return new User(rs.getInt("id"), username);
                }
            }

            String insertSql = "INSERT INTO users (username) VALUES (?)";
            try (PreparedStatement pst = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, username);
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    System.out.println("✅ New user created: " + username);
                    return new User(rs.getInt(1), username);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
