package edu.sussex.tictactoe;

import edu.sussex.tictactoe.model.Game;
import edu.sussex.tictactoe.model.Move;
import edu.sussex.tictactoe.model.ReplayModel;
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

public class ReplayModelTest {
    
    private Connection connection;
    private GameRepository repository;
    private ReplayModel replayModel;
    
    @BeforeEach
    void setUp() throws SQLException, IOException {
        // Create an in-memory H2 database for testing with MySQL mode
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=MySQL", "sa", "");
        repository = new GameRepository(connection);
        replayModel = new ReplayModel(repository);
        
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
    public void testInitialEmptyState() {
        // Test that replay starts with empty board and no loaded game 
        int[] board = replayModel.getBoard();
        for (int i = 0; i < 9; i++) {
            assertEquals(GameModel.EMPTY, board[i], "All positions should be empty initially");
        }
        
        assertFalse(replayModel.hasMoreMoves(), "Should not have moves initially");
        assertEquals(GameModel.GAME_NOT_STARTED, replayModel.getGameStatus(), 
                     "Game status should be not started initially");
        assertEquals(GameModel.HUMAN, replayModel.getCurrentPlayer(), 
                     "Human player should start first");
    }
    
    @Test
    public void testLoadGameAndStepOneMove() throws SQLException {
        // Create a game with some moves for testing
        Game game = repository.startGame("random", "IN_PROGRESS");
        
        // Add some moves  
        Move move1 = new Move(game.getId(), 1, 1, 4); // Human at center (X)
        Move move2 = new Move(game.getId(), 2, -1, 0); // Computer at top-left (O)
        
        repository.saveMove(move1, "IN_PROGRESS");
        repository.saveMove(move2, "IN_PROGRESS");
        
        // Load the game for replay
        replayModel.loadGame(game.getId());
        
        assertTrue(replayModel.hasMoreMoves(), "Should have moves after loading game");
        
        // Step once - should make first move (X at center)
        assertTrue(replayModel.step(), "First step should succeed");
        
        // Verify board state after one move 
        int[] board = replayModel.getBoard();
        assertEquals(GameModel.HUMAN, board[4], "Human player should have moved to center position");
        assertEquals(GameModel.COMPUTER, replayModel.getCurrentPlayer(), "Computer player should be active now");
        assertTrue(replayModel.hasMoreMoves(), "Should still have moves after first move");
        
        // Step again - should make second move (O at top-left)
        assertTrue(replayModel.step(), "Second step should succeed"); 
        
        board = replayModel.getBoard();
        assertEquals(GameModel.HUMAN, board[4], "Human player should still be at center position");
        assertEquals(GameModel.COMPUTER, board[0], "Computer player should have moved to top-left position");
        assertFalse(replayModel.hasMoreMoves(), "Should not have more moves after playing all");
    }
    
    @Test
    public void testEndBehavior() throws SQLException {
        // Create a game with some moves that leads to a win 
        Game game = repository.startGame("random", "IN_PROGRESS");
        
        // Add moves leading to human win - top row: X | X | X
        Move move1 = new Move(game.getId(), 1, 1, 0); // Human at position 0  
        Move move2 = new Move(game.getId(), 2, -1, 3); // Computer at position 3 
        Move move3 = new Move(game.getId(), 3, 1, 1); // Human at position 1
        Move move4 = new Move(game.getId(), 4, -1, 4); // Computer at position 4
        Move move5 = new Move(game.getId(), 5, 1, 2); // Human at position 2 (winning)
        
        repository.saveMove(move1, "IN_PROGRESS");
        repository.saveMove(move2, "IN_PROGRESS"); 
        repository.saveMove(move3, "IN_PROGRESS");
        repository.saveMove(move4, "IN_PROGRESS");
        repository.saveMove(move5, "HUMAN_WINS"); // Game ends with human win
        
        // Load the game for replay
        replayModel.loadGame(game.getId());
        
        // Step through all moves to end of game
        assertTrue(replayModel.step(), "First step should succeed");
        assertTrue(replayModel.step(), "Second step should succeed");
        assertTrue(replayModel.step(), "Third step should succeed"); 
        assertTrue(replayModel.step(), "Fourth step should succeed");
        assertTrue(replayModel.step(), "Fifth step should succeed");
        
        assertFalse(replayModel.hasMoreMoves(), "Should not have more moves after end");
        
        // Extra steps should change nothing
        assertFalse(replayModel.step(), "Step after game end should fail");
        assertFalse(replayModel.step(), "Second extra step should also fail");
        
        // Board and status should reflect final state 
        int[] board = replayModel.getBoard();
        assertEquals(GameModel.HUMAN, board[0], "Human player at position 0");
        assertEquals(GameModel.HUMAN, board[1], "Human player at position 1");  
        assertEquals(GameModel.HUMAN, board[2], "Human player at position 2 (winning move)");
        assertEquals(GameModel.COMPUTER, board[3], "Computer player at position 3");
        assertEquals(GameModel.COMPUTER, board[4], "Computer player at position 4");
    }
    
    @Test
    public void testSwitchGames() throws SQLException {
        // Create two games with different move sequences  
        Game game1 = repository.startGame("random", "IN_PROGRESS");
        Game game2 = repository.startGame("stronger", "IN_PROGRESS");
        
        // Add moves to first game 
        Move move1 = new Move(game1.getId(), 1, 1, 4); // Human at center
        repository.saveMove(move1, "IN_PROGRESS");
        
        // Add different moves to second game - Player 1 (X) starts each game
        Move move2 = new Move(game2.getId(), 1, 1, 8); // Human at bottom-right 
        repository.saveMove(move2, "IN_PROGRESS"); 
        
        // Load first game and step once  
        replayModel.loadGame(game1.getId());
        assertTrue(replayModel.step(), "First game step should succeed");
        
        // Verify we're in state of first game
        int[] board = replayModel.getBoard();
        assertEquals(GameModel.HUMAN, board[4], "Human player at center position");
        
        // Load second game - should reset to empty board 
        replayModel.loadGame(game2.getId());
        assertTrue(replayModel.hasMoreMoves(), "Second game should have moves");
        
        // Verify we're in state of second game
        board = replayModel.getBoard();
        assertEquals(GameModel.EMPTY, board[8], "Empty position at bottom-right"); 
        
        // Step once for second game (should be X's move)
        assertTrue(replayModel.step(), "Second game step should succeed");
        board = replayModel.getBoard();
        assertEquals(GameModel.HUMAN, board[8], "Human player at bottom-right position");
    }
    
    @Test
    public void testInputHistoryPreservation() throws SQLException {
        // Create a game and make sure we can play back the exact same moves in order  
        Game game = repository.startGame("random", "IN_PROGRESS");
        
        // Add moves exactly as described in requirements: 0,3,1,4,2 for X win
        Move move1 = new Move(game.getId(), 1, 1, 0); // Human at position 0 (X)
        Move move2 = new Move(game.getId(), 2, -1, 3); // Computer at position 3 (O) 
        Move move3 = new Move(game.getId(), 3, 1, 1); // Human at position 1 (X)
        Move move4 = new Move(game.getId(), 4, -1, 4); // Computer at position 4 (O)
        Move move5 = new Move(game.getId(), 5, 1, 2); // Human at position 2 (X) - win
        
        repository.saveMove(move1, "IN_PROGRESS");
        repository.saveMove(move2, "IN_PROGRESS"); 
        repository.saveMove(move3, "IN_PROGRESS");
        repository.saveMove(move4, "IN_PROGRESS");
        repository.saveMove(move5, "HUMAN_WINS"); // This is the final outcome
        
        // Load and replay game exactly as stored
        replayModel.loadGame(game.getId());
        
        // Verify original outcome is correctly set (this was the main fix)
        assertEquals("HUMAN_WINS", replayModel.getOriginalOutcome(), "Original outcome should be HUMAN_WINS");
        
        // Step through all moves in order - should be able to reproduce exact sequence 
        assertEquals(GameModel.EMPTY, replayModel.getBoard()[0], "Position 0 should be empty initially");
        assertTrue(replayModel.step(), "Step 1 should succeed");
        assertEquals(GameModel.HUMAN, replayModel.getBoard()[0], "Human at position 0 after step 1");
        
        assertTrue(replayModel.step(), "Step 2 should succeed"); 
        assertEquals(GameModel.COMPUTER, replayModel.getBoard()[3], "Computer at position 3 after step 2");
        
        assertTrue(replayModel.step(), "Step 3 should succeed");
        assertEquals(GameModel.HUMAN, replayModel.getBoard()[1], "Human at position 1 after step 3");
        
        assertTrue(replayModel.step(), "Step 4 should succeed"); 
        assertEquals(GameModel.COMPUTER, replayModel.getBoard()[4], "Computer at position 4 after step 4");
        
        assertTrue(replayModel.step(), "Step 5 should succeed");
        assertEquals(GameModel.HUMAN, replayModel.getBoard()[2], "Human at position 2 (win) after step 5");
        
        // After playing all moves, original outcome should still be HUMAN_WINS
        assertEquals("HUMAN_WINS", replayModel.getOriginalOutcome(), "Original outcome should remain HUMAN_WINS after replay");
        
        assertFalse(replayModel.hasMoreMoves(), "Should have no more moves after final step");
    }
    
    @Test
    public void testResetReplay() throws SQLException {
        // Create a game and load it for replay 
        Game game = repository.startGame("random", "IN_PROGRESS");
        Move move1 = new Move(game.getId(), 1, 1, 4); // Human at center
        repository.saveMove(move1, "IN_PROGRESS");
        
        replayModel.loadGame(game.getId());
        assertTrue(replayModel.step(), "Step should succeed");
        
        // Reset the replay  
        replayModel.reset();
        
        // Verify reset state
        int[] board = replayModel.getBoard();
        for (int i = 0; i < 9; i++) {
            assertEquals(GameModel.EMPTY, board[i], "All positions should be empty after reset");
        }
        
        assertFalse(replayModel.hasMoreMoves(), "Should not have moves after reset");
        assertEquals(GameModel.GAME_NOT_STARTED, replayModel.getGameStatus(), 
                     "Game status should be not started after reset");
        assertEquals(GameModel.HUMAN, replayModel.getCurrentPlayer(), 
                     "Human player should start first after reset");
    }
}