package bookingticket;

import java.sql.*;
import java.util.Scanner;

public class Booking1 {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/soorya";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Soorya@1107";

    private Connection conn;

    public Booking1() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeDatabase() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Disconnected from the database.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void displayMovies() {
        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM movies";
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("Available Movies:");
            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                String title = rs.getString("title");
                String genre = rs.getString("genre");
                System.out.println(movieId + ". " + title + " (" + genre + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayShowtimes(int movieId) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM showtimes WHERE movie_id = ?");
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("Showtimes for the selected movie:");
            while (rs.next()) {
                int showtimeId = rs.getInt("showtime_id");
                String showDate = rs.getString("show_date");
                String showTime = rs.getString("show_time");
                String theaterRoom = rs.getString("theater_room");
                int availableSeats = rs.getInt("available_seats");

                System.out.println(
                        showtimeId + ". " + showDate + " " + showTime + " (" + theaterRoom + ") - Available Seats: "
                                + availableSeats);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void selectSeats(int showtimeId) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM showtimes WHERE showtime_id = ?");
            pstmt.setInt(1, showtimeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int availableSeats = rs.getInt("available_seats");
                if (availableSeats > 0) {
                    System.out.println("Available seats: " + availableSeats);
                    Scanner scanner = new Scanner(System.in);
                    System.out.print("Enter the number of seats you want to book: ");
                    int bookedSeats = scanner.nextInt();
                    if (bookedSeats > 0 && bookedSeats <= availableSeats) {
                        scanner.nextLine(); // Consume the newline character
                        System.out.print("Enter your name: ");
                        String userName = scanner.nextLine();
                        System.out.print("Enter your email: ");
                        String userEmail = scanner.nextLine();

                        bookTickets(showtimeId, bookedSeats, userName, userEmail);
                    } else {
                        System.out.println("Invalid number of seats. Please try again.");
                    }
                } else {
                    System.out.println("No available seats for this showtime.");
                }
            } else {
                System.out.println("Invalid showtime ID. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void bookTickets(int showtimeId, int bookedSeats, String userName, String userEmail) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM showtimes WHERE showtime_id = ?");
            pstmt.setInt(1, showtimeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int availableSeats = rs.getInt("available_seats");
                if (bookedSeats <= availableSeats) {
                    int updatedAvailableSeats = availableSeats - bookedSeats;
                    PreparedStatement updatePstmt = conn.prepareStatement(
                            "UPDATE showtimes SET available_seats = ? WHERE showtime_id = ?");
                    updatePstmt.setInt(1, updatedAvailableSeats);
                    updatePstmt.setInt(2, showtimeId);
                    updatePstmt.executeUpdate();

                    PreparedStatement insertPstmt = conn.prepareStatement(
                            "INSERT INTO bookings (showtime_id, user_name, user_email, booked_seats) VALUES (?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
                    insertPstmt.setInt(1, showtimeId);
                    insertPstmt.setString(2, userName);
                    insertPstmt.setString(3, userEmail);
                    insertPstmt.setInt(4, bookedSeats);
                    insertPstmt.executeUpdate();

                    ResultSet generatedKeys = insertPstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int bookingId = generatedKeys.getInt(1);
                        System.out.println("Booking confirmed! Your Booking ID: " + bookingId);
                    }
                } else {
                    System.out.println("Insufficient available seats. Please try again.");
                }
            } else {
                System.out.println("Invalid showtime ID. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void searchMovies(String keyword) {
        try {
            String query = "SELECT * FROM movies WHERE title LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nSearch Results:");
            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                String title = rs.getString("title");
                String genre = rs.getString("genre");
                System.out.println(movieId + ". " + title + " (" + genre + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Booking1 bookingSystem = new Booking1();

        int choice;
        do {
            System.out.println("\nWelcome to Movie Ticket Booking System");
            System.out.println("1. Display Movies");
            System.out.println("2. Search Movies");
            System.out.println("3. Display Showtimes for a Movie");
            System.out.println("4. Select Seats and Book Tickets");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    bookingSystem.displayMovies();
                    break;
                case 2:
                    System.out.print("Enter a keyword to search for movies: ");
                    scanner.nextLine(); // Consume the newline character
                    String keyword = scanner.nextLine();
                    bookingSystem.searchMovies(keyword);
                    break;
                case 3:
                    System.out.print("Enter the Movie ID: ");
                    int movieId = scanner.nextInt();
                    bookingSystem.displayShowtimes(movieId);
                    break;
                case 4:
                    System.out.print("Enter the Showtime ID: ");
                    int showtimeId = scanner.nextInt();
                    bookingSystem.selectSeats(showtimeId);
                    break;
                case 0:
                    bookingSystem.closeDatabase();
                    System.out.println("\nThank you for using Movie Ticket Booking System!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);

        scanner.close();
    }
}