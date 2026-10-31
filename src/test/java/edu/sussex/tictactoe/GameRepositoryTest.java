package edu.sussex.tictactoe;

import edu.sussex.tictactoe.model.Game;
import edu.sussex.tictactoe.model.Move;
import edu.sussex.tictactoe.repository.GameRepository;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class GameRepositoryTest {
    
    private Connection connection;
    private GameRepository repository;
    
    @BeforeEach
    void setUp() throws SQLException, IOException {
        // Create an in-memory H2 database for testing with MySQL mode
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=MySQL", "sa", "");
        repository = new GameRepository(connection);
        
        // Read and execute the actual schema.sql file to set up tables properly
        String schemaSql = Files.readString(Path.of("db/schema.sql"));
        // Split statements by semicolon and execute each one
        try (var stmt = connection.createStatement()) {
            for (String sql : schemaSql.split(";")) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
    }
    
    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    @Test
    public void testStartGame() throws SQLException {
        // Test creating a new game with proper values
        Game game = repository.startGame("random", "IN_PROGRESS");
        
        assertNotNull(game);
        assertTrue(game.getId() > 0);
        assertEquals("random", game.getOpponentMode());
        assertEquals("IN_PROGRESS", game.getOutcome());
        assertNotNull(game.getStartTime());
    }
    
    @Test
    public void testSaveMove() throws SQLException {
        // First, create a game
        Game game = repository.startGame("stronger", "IN_PROGRESS");
        
        // Then save a move with proper player values (1 for X, -1 for O)
        Move move = new Move(game.getId(), 1, 1, 4);
        repository.saveMove(move, "IN_PROGRESS");
        
        // Verify the move was saved correctly
        try (var stmt = connection.prepareStatement("SELECT * FROM moves WHERE game_id = ? AND move_number = ?")) {
            stmt.setLong(1, game.getId());
            stmt.setInt(2, 1);
            
            try (var rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(game.getId(), rs.getLong("game_id"));
                assertEquals(1, rs.getInt("move_number"));
                assertEquals(1, rs.getInt("player"));
                assertEquals(4, rs.getInt("position"));
            }
        }
        
        // Verify game outcome was updated
        try (var stmt = connection.prepareStatement("SELECT outcome FROM games WHERE id = ?")) {
            stmt.setLong(1, game.getId());
            
            try (var rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("IN_PROGRESS", rs.getString("outcome"));
            }
        }
    }
    
    @Test
    public void testSaveMultipleMoves() throws SQLException {
        // First, create a game
        Game game = repository.startGame("random", "IN_PROGRESS");
        
        // Save first move
        Move move1 = new Move(game.getId(), 1, 1, 0);
        repository.saveMove(move1, "IN_PROGRESS");
        
        // Save second move  
        Move move2 = new Move(game.getId(), 2, -1, 4);
        repository.saveMove(move2, "IN_PROGRESS");
        
        // Verify both moves were saved
        try (var stmt = connection.prepareStatement("SELECT * FROM moves WHERE game_id = ? ORDER BY move_number")) {
            stmt.setLong(1, game.getId());
            
            try (var rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("move_number"));
                assertEquals(0, rs.getInt("position"));
                
                assertTrue(rs.next());
                assertEquals(2, rs.getInt("move_number"));
                assertEquals(4, rs.getInt("position"));
                
                assertFalse(rs.next());
            }
        }
    }
    
    @Test
    public void testIsolatedGames() throws SQLException {
        // Create two separate games
        Game game1 = repository.startGame("random", "IN_PROGRESS");
        Game game2 = repository.startGame("stronger", "IN_PROGRESS");
        
        // Save moves for each game independently
        Move move1 = new Move(game1.getId(), 1, 1, 0);
        Move move2 = new Move(game2.getId(), 1, -1, 8);
        
        repository.saveMove(move1, "IN_PROGRESS");
        repository.saveMove(move2, "IN_PROGRESS");
        
        // Verify each game has only its own moves
        try (var stmt = connection.prepareStatement("SELECT COUNT(*) as count FROM moves WHERE game_id = ?")) {
            stmt.setLong(1, game1.getId());
            try (var rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("count"));
            }
            
            stmt.setLong(1, game2.getId());
            try (var rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("count"));
            }
        }
    }
    
    @Test
    public void testDuplicateMoveRejected() throws SQLException {
        // First, create a game
        Game game = repository.startGame("random", "IN_PROGRESS");
        
        // Save first move
        Move move1 = new Move(game.getId(), 1, 1, 4);
        repository.saveMove(move1, "IN_PROGRESS");
        
        // Try to save another move with the same position (should fail)
        Move move2 = new Move(game.getId(), 2, -1, 4); // Same position as first move
        
        SQLException exception = assertThrows(SQLException.class, () -> {
            repository.saveMove(move2, "HUMAN_WINS"); // Changed from "IN_PROGRESS" to "HUMAN_WINS"
        });
        
        // Verify that we're catching the constraint violation properly (not relying on error message text)
        // Check that game still has only one move and outcome is unchanged
        try (var stmt = connection.prepareStatement("SELECT COUNT(*) as count FROM moves WHERE game_id = ?")) {
            stmt.setLong(1, game.getId());
            try (var rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("count")); // Should still only have 1 move
            }
        }
        
        try (var stmt = connection.prepareStatement("SELECT outcome FROM games WHERE id = ?")) {
            stmt.setLong(1, game.getId());
            try (var rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("IN_PROGRESS", rs.getString("outcome")); // Should still be unchanged
            }
        }
    }
    
    @Test
    public void testInvalidGameIdRejected() throws SQLException {
        Move move = new Move(999L, 1, 1, 4); // Non-existent game ID
        
        SQLException exception = assertThrows(SQLException.class, () -> {
            repository.saveMove(move, "IN_PROGRESS");
        });
        
        assertTrue(exception.getMessage().contains("does not exist"));
    }
    
    @Test
    public void testInvalidMoveNumberRejected() throws SQLException {
        Game game = repository.startGame("random", "IN_PROGRESS");
        Move move = new Move(game.getId(), 0, 1, 4); // Invalid move number
        
        SQLException exception = assertThrows(SQLException.class, () -> {
            repository.saveMove(move, "IN_PROGRESS");
        });
        
        assertTrue(exception.getMessage().contains("Invalid move number"));
    }
    
    @Test
    public void testInvalidPositionRejected() throws SQLException {
        Game game = repository.startGame("random", "IN_PROGRESS");
        Move move = new Move(game.getId(), 1, 1, -1); // Invalid position
        
        SQLException exception = assertThrows(SQLException.class, () -> {
            repository.saveMove(move, "IN_PROGRESS");
        });
        
        assertTrue(exception.getMessage().contains("Invalid position"));
    }
    
    @Test
    public void testInvalidPlayerRejected() throws SQLException {
        Game game = repository.startGame("random", "IN_PROGRESS");
        Move move = new Move(game.getId(), 1, 2, 4); // Invalid player (should be only 1 or -1)
        
        SQLException exception = assertThrows(SQLException.class, () -> {
            repository.saveMove(move, "IN_PROGRESS");
        });
        
        assertTrue(exception.getMessage().contains("Invalid player"));
    }
    
    @Test
    public void testInvalidOutcomeRejected() throws SQLException {
        Game game = repository.startGame("random", "IN_PROGRESS");
        Move move = new Move(game.getId(), 1, 1, 4);
        
        SQLException exception = assertThrows(SQLException.class, () -> {
            // This should fail because we're using a lowercase outcome
            repository.saveMove(move, "in_progress");
        });
        
        assertTrue(exception.getMessage().contains("Invalid outcome"));
    }
    
    @Test
    public void testClosedConnectionFailure() throws SQLException {
        Game game = repository.startGame("random", "IN_PROGRESS");
        Move move = new Move(game.getId(), 1, 1, 4);
        
        // Close the connection before trying to use it
        connection.close();
        
        SQLException exception = assertThrows(SQLException.class, () -> {
            repository.saveMove(move, "IN_PROGRESS");
        });
    }
}