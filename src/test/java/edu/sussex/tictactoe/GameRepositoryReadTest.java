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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameRepositoryReadTest {
    
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
    public void testListGamesEmptyHistory() throws SQLException {
        // Test listing games when there are no saved games
        List<Game> games = repository.listGames();
        
        assertNotNull(games);
        assertEquals(0, games.size());
    }
    
    @Test
    public void testListGamesWithMultipleGames() throws SQLException {
        // Create multiple games
        Game game1 = repository.startGame("random", "IN_PROGRESS");
        try {
            Thread.sleep(10); // Ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Game game2 = repository.startGame("stronger", "HUMAN_WINS");
        
        // List all games
        List<Game> games = repository.listGames();
        
        assertNotNull(games);
        assertEquals(2, games.size());
        
        // Verify the most recent game is first (ordered by time DESC)
        Game firstGame = games.get(0);
        Game secondGame = games.get(1);
        
        assertEquals(game2.getId(), firstGame.getId());  // game2 was created later
        assertEquals(game1.getId(), secondGame.getId());
        
        // Verify game details are correct
        assertEquals("stronger", firstGame.getOpponentMode());
        assertEquals("HUMAN_WINS", firstGame.getOutcome());
        
        assertEquals("random", secondGame.getOpponentMode());
        assertEquals("IN_PROGRESS", secondGame.getOutcome());
    }
    
    @Test
    public void testGetGameMovesEmptyGame() throws SQLException {
        // Create a game but don't add any moves
        Game game = repository.startGame("random", "IN_PROGRESS");
        
        // Load moves for this game - should return empty list
        List<Move> moves = repository.getGameMoves(game.getId());
        
        assertNotNull(moves);
        assertEquals(0, moves.size());
    }
    
    @Test
    public void testGetGameMovesWithMultipleMoves() throws SQLException {
        // Create a game
        Game game = repository.startGame("stronger", "IN_PROGRESS");
        
        // Add multiple moves in different orders (should be returned in move-number order)
        Move move1 = new Move(game.getId(), 3, -1, 5);
        Move move2 = new Move(game.getId(), 1, 1, 0);
        Move move3 = new Move(game.getId(), 2, -1, 4);
        
        repository.saveMove(move2, "IN_PROGRESS"); // Save move 1 first
        repository.saveMove(move3, "IN_PROGRESS"); // Save move 2 second  
        repository.saveMove(move1, "IN_PROGRESS"); // Save move 3 third
        
        // Load moves for this game - should return them in move-number order
        List<Move> moves = repository.getGameMoves(game.getId());
        
        assertNotNull(moves);
        assertEquals(3, moves.size());
        
        // Verify moves are returned in correct order (by move number)
        assertEquals(1, moves.get(0).getMoveNumber());
        assertEquals(2, moves.get(1).getMoveNumber());
        assertEquals(3, moves.get(2).getMoveNumber());
        
        // Verify the actual move details
        assertEquals(game.getId(), moves.get(0).getGameId());
        assertEquals(1, moves.get(0).getPlayer());
        assertEquals(0, moves.get(0).getPosition());
        
        assertEquals(game.getId(), moves.get(1).getGameId());
        assertEquals(-1, moves.get(1).getPlayer());
        assertEquals(4, moves.get(1).getPosition());
        
        assertEquals(game.getId(), moves.get(2).getGameId());
        assertEquals(-1, moves.get(2).getPlayer());
        assertEquals(5, moves.get(2).getPosition());
    }
    
    @Test
    public void testGetGameMovesIsolation() throws SQLException {
        // Create two separate games
        Game game1 = repository.startGame("random", "IN_PROGRESS");
        Game game2 = repository.startGame("stronger", "IN_PROGRESS");
        
        // Add moves to each game independently
        Move move1 = new Move(game1.getId(), 1, 1, 0);
        Move move2 = new Move(game2.getId(), 1, -1, 8);
        
        repository.saveMove(move1, "IN_PROGRESS");
        repository.saveMove(move2, "IN_PROGRESS");
        
        // Load moves for first game
        List<Move> moves1 = repository.getGameMoves(game1.getId());
        assertEquals(1, moves1.size());
        assertEquals(1, moves1.get(0).getMoveNumber());
        assertEquals(0, moves1.get(0).getPosition());
        
        // Load moves for second game
        List<Move> moves2 = repository.getGameMoves(game2.getId());
        assertEquals(1, moves2.size());
        assertEquals(1, moves2.get(0).getMoveNumber());
        assertEquals(8, moves2.get(0).getPosition());
    }
    
    @Test
    public void testGetGameMovesUnknownGameId() throws SQLException {
        // Try to load moves for a non-existent game ID
        SQLException exception = assertThrows(SQLException.class, () -> {
            repository.getGameMoves(999L);
        });
        
        assertTrue(exception.getMessage().contains("does not exist"));
    }
    
    @Test
    public void testListGamesIsolation() throws SQLException {
        // Create games in different scenarios to ensure isolation
        
        Game game1 = repository.startGame("random", "IN_PROGRESS");
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Game game2 = repository.startGame("stronger", "COMPUTER_WINS");
        
        // Add moves to first game
        Move move1 = new Move(game1.getId(), 1, 1, 4);
        repository.saveMove(move1, "IN_PROGRESS");
        
        // List games - should only show the two created games regardless of moves
        List<Game> games = repository.listGames();
        assertEquals(2, games.size());
        
        // Verify game details are correct 
        Game firstGame = games.get(0);
        Game secondGame = games.get(1);
        
        assertEquals(game2.getId(), firstGame.getId());  // game2 was created later
        assertEquals("stronger", firstGame.getOpponentMode());
        assertEquals("COMPUTER_WINS", firstGame.getOutcome());
    }
}