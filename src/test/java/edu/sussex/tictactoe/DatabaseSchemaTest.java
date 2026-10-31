package edu.sussex.tictactoe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Test to verify the database schema works correctly with H2 in MySQL mode.
 */
public class DatabaseSchemaTest {
    
    @Test
    public void testDatabaseSchemaCreation() throws SQLException, IOException {
        // Use H2 in MySQL compatibility mode
        String url = "jdbc:h2:mem:testdb;MODE=MySQL";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            Statement stmt = conn.createStatement();
            
            // Execute the actual schema file to create tables
            String schemaContent = new String(Files.readAllBytes(Paths.get("db/schema.sql")));
            // Split and execute the statements
            String[] statements = schemaContent.split(";");
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
            
            // Test inserting a valid game
            stmt.execute("INSERT INTO games (opponent_mode, outcome) VALUES ('random', 'IN_PROGRESS')");
            long gameId = 1;
            
            // Test inserting valid moves
            stmt.execute("INSERT INTO moves (game_id, move_number, player, position) VALUES (" + 
                    gameId + ", 1, 1, 4)");
            stmt.execute("INSERT INTO moves (game_id, move_number, player, position) VALUES (" + 
                    gameId + ", 2, -1, 0)");
            
            // Verify we can retrieve the data
            ResultSet rs = stmt.executeQuery("SELECT * FROM games WHERE id = " + gameId);
            assertTrue(rs.next());
            assertEquals("random", rs.getString("opponent_mode"));
            assertEquals("IN_PROGRESS", rs.getString("outcome"));
            
            rs = stmt.executeQuery("SELECT * FROM moves WHERE game_id = " + gameId + " ORDER BY move_number");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("move_number"));
            assertEquals(1, rs.getInt("player"));
            assertEquals(4, rs.getInt("position"));
            
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("move_number"));
            assertEquals(-1, rs.getInt("player"));
            assertEquals(0, rs.getInt("position"));
            
            // Test that we can create a second game with same positions (different game_id)
            stmt.execute("INSERT INTO games (opponent_mode, outcome) VALUES ('random', 'HUMAN_WINS')");
            long gameId2 = 2;
            stmt.execute("INSERT INTO moves (game_id, move_number, player, position) VALUES (" + 
                    gameId2 + ", 1, 1, 4)");
            
            // Test that duplicate positions within one game are rejected
            // Try to insert a move with same position in the same game - should fail
            boolean failedAsExpected = false;
            try {
                stmt.execute("INSERT INTO moves (game_id, move_number, player, position) VALUES (" + 
                        gameId + ", 3, 1, 4)");
            } catch (SQLException e) {
                // This constraint violation should occur in H2
                failedAsExpected = true;
            }
            
            assertTrue(failedAsExpected, "Duplicate position within same game should be rejected");
        }
    }
}