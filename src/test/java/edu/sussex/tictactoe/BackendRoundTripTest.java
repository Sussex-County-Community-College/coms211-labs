package edu.sussex.tictactoe;

import edu.sussex.tictactoe.model.Game;
import edu.sussex.tictactoe.model.Move;
import edu.sussex.tictactoe.repository.GameRepository;
import edu.sussex.tictactoe.service.LiveGameService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BackendRoundTripTest {

    @TempDir
    Path tempDir;

    private Connection connection;
    private GameRepository repository;
    
    @BeforeEach
    void setUp() throws SQLException, IOException {
        // Create a temporary FILE H2 database for testing with MySQL mode
        String dbPath = tempDir.resolve("testdb").toString();
        connection = DriverManager.getConnection("jdbc:h2:" + dbPath + ";MODE=MySQL", "sa", "");
        repository = new GameRepository(connection);
        
        // Read and execute the actual schema.sql file to set up tables properly  
        String schemaSql = Files.readString(Path.of("db/schema.sql"));
        try (var stmt = connection.createStatement()) {
            stmt.execute(schemaSql);
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Verifies a random-mode X win, saved history, H2 connection restart, and stepwise replay.
     * The two-move X0/O3 prefix is checked before extending to X1/O4/X2.
     */
    @Test
    void testSimpleBackendRoundTrip() throws SQLException {
        // Create a controlled random - indices 2 then 1 select available board positions 3 then 4
        Random controlledRandom = new Random() {
            private int callCount = 0;
            
            @Override
            public int nextInt(int bound) {
                callCount++;
                if (callCount == 1) return 2; // First RNG index 2 selects board position 3  
                else if (callCount == 2) return 1; // Second RNG index 1 selects board position 4
                else return super.nextInt(bound); 
            }
        };

        LiveGameService service = new LiveGameService(repository, controlledRandom);
        
        // Start a random-mode game
        Game game = service.startGame("random");
        long gameId = game.getId();
        
        // Make human move at position 0 (X0)  
        assertTrue(service.makeHumanMove(0));
        
        // Computer makes first move - with our controlled random returning 2
        assertTrue(service.makeComputerMove());
        
        // Verify board shows X at 0, O at 3 (according to requirements)
        int[] board = service.getBoard();
        assertEquals(1, board[0]); // Human (X) at position 0  
        assertEquals(-1, board[3]); // Computer (O) at position 3
        assertEquals(0, board[2]); // Position 2 should be empty
        
        // Assert the exact required board pattern: [1,0,0,-1,0,0,0,0,0]
        int[] expectedBoard = {1, 0, 0, -1, 0, 0, 0, 0, 0};
        assertArrayEquals(expectedBoard, board);
        
        // Read saved moves from repository and verify sequence
        List<Move> savedMoves = repository.getGameMoves(gameId);
        assertEquals(2, savedMoves.size()); // Should have exactly 2 moves
        
        // Verify move properties for the first two moves:
        // positions [0,3], players [1,-1], move numbers [1,2]
        
        assertEquals(0, savedMoves.get(0).getPosition()); // X at position 0
        assertEquals(1, savedMoves.get(0).getPlayer());   // Human (X)
        assertEquals(1, savedMoves.get(0).getMoveNumber());
        
        assertEquals(3, savedMoves.get(1).getPosition()); // O at position 3  
        assertEquals(-1, savedMoves.get(1).getPlayer());  // Computer (O)
        assertEquals(2, savedMoves.get(1).getMoveNumber());
        
        // Verify game mode and outcome
        Game loadedGame = repository.listGames().stream()
                .filter(g -> g.getId() == gameId)
                .findFirst()
                .orElse(null);
        
        assertNotNull(loadedGame);
        assertEquals("random", loadedGame.getOpponentMode());
        assertEquals("IN_PROGRESS", loadedGame.getOutcome());
        
        // Continue the game to completion
        assertTrue(service.makeHumanMove(1));  // Human move at position 1 (X1)
        assertTrue(service.makeComputerMove()); // Computer move at position 4 (O4) - this should be index 1 from our controlled random
        assertTrue(service.makeHumanMove(2));  // Human move at position 2 (X2) - this wins the game
        
        // Verify final board state: [1,1,1,-1,-1,0,0,0,0] with HUMAN_WINS status
        board = service.getBoard();
        int[] expectedFinalBoard = {1, 1, 1, -1, -1, 0, 0, 0, 0};
        assertArrayEquals(expectedFinalBoard, board);
        
        // Verify game status is HUMAN_WINS (2)
        assertEquals(2, service.getGameStatus());
        
        // Re-read the database and verify all 5 moves are saved correctly
        List<Move> finalSavedMoves = repository.getGameMoves(gameId);
        assertEquals(5, finalSavedMoves.size()); // Should now have exactly 5 moves
        
        // Define expected values using arrays for exact verification
        int[] expectedPositions = {0, 3, 1, 4, 2};
        int[] expectedPlayers = {1, -1, 1, -1, 1};
        int[] expectedMoveNumbers = {1, 2, 3, 4, 5};
        
        // Use loops to verify all move properties exactly as specified
        for (int i = 0; i < 5; i++) {
            assertEquals(expectedPositions[i], finalSavedMoves.get(i).getPosition(), 
                        "Move " + (i+1) + " position mismatch");
            assertEquals(expectedPlayers[i], finalSavedMoves.get(i).getPlayer(), 
                        "Move " + (i+1) + " player mismatch"); 
            assertEquals(expectedMoveNumbers[i], finalSavedMoves.get(i).getMoveNumber(), 
                        "Move " + (i+1) + " move number mismatch");
        }
        
        // Verify game mode and outcome are updated
        Game finalLoadedGame = repository.listGames().stream()
                .filter(g -> g.getId() == gameId)
                .findFirst()
                .orElse(null);
        
        assertNotNull(finalLoadedGame);
        assertEquals("random", finalLoadedGame.getOpponentMode());
        assertEquals("HUMAN_WINS", finalLoadedGame.getOutcome());
        
        // ========== EXTENDED FUNCTIONALITY: H2 Connection Restart Test ==========
        // Close the current connection
        connection.close();
        
        // Reopen the same database file 
        String dbPath = tempDir.resolve("testdb").toString();
        connection = DriverManager.getConnection("jdbc:h2:" + dbPath + ";MODE=MySQL", "sa", "");
        GameRepository reopenedRepository = new GameRepository(connection);
        
        // Reload the existing game ID and verify it still has 5 moves
        List<Game> allGames = reopenedRepository.listGames();
        assertEquals(1, allGames.size());
        assertEquals(gameId, allGames.get(0).getId());
        assertEquals("random", allGames.get(0).getOpponentMode());
        assertEquals("HUMAN_WINS", allGames.get(0).getOutcome());
        
        List<Move> reopenedMoves = reopenedRepository.getGameMoves(gameId);
        assertEquals(5, reopenedMoves.size()); // Should still have exactly 5 moves
        
        // Define expected values using arrays for exact verification
        int[] restartPositions = {0, 3, 1, 4, 2};
        int[] restartPlayers = {1, -1, 1, -1, 1};
        int[] restartMoveNumbers = {1, 2, 3, 4, 5};
        
        // Use loops to verify all move properties exactly as specified
        for (int i = 0; i < 5; i++) {
            assertEquals(restartPositions[i], reopenedMoves.get(i).getPosition(), 
                        "Move " + (i+1) + " position mismatch");
            assertEquals(restartPlayers[i], reopenedMoves.get(i).getPlayer(), 
                        "Move " + (i+1) + " player mismatch"); 
            assertEquals(restartMoveNumbers[i], reopenedMoves.get(i).getMoveNumber(), 
                        "Move " + (i+1) + " move number mismatch");
        }
        
        // Create ReplayModel with reopened repository and load that game
        edu.sussex.tictactoe.model.ReplayModel replay = new edu.sussex.tictactoe.model.ReplayModel(reopenedRepository);
        replay.loadGame(gameId);
        
        // Assert an empty board first (before any steps)
        int[] initialBoard = replay.getBoard();
        int[] expectedEmptyBoard = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        assertArrayEquals(expectedEmptyBoard, initialBoard);
        
        // Use positions [0,3,1,4,2] and players [1,-1,1,-1,1] to build expected board one cell at a time
        int[] restartExpectedBoard = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        
        // Step through each move and assert the board state after each step
        for (int i = 0; i < 5; i++) {
            assertTrue(replay.step(), "Step should succeed for move " + (i+1));
            
            // Update expected board with current player's move  
            int position = restartPositions[i];
            int player = restartPlayers[i];
            restartExpectedBoard[position] = player;
            
            // Assert the whole replay board equals that prefix
            assertArrayEquals(restartExpectedBoard, replay.getBoard(), "Board state mismatch after step " + (i+1));
        }
        
        // At end assert hasMoreMoves false 
        assertFalse(replay.hasMoreMoves());
        
        // getOriginalOutcome() should equal STRING "HUMAN_WINS"
        assertEquals("HUMAN_WINS", replay.getOriginalOutcome());
        
        // An extra step() returns false without changing the board
        assertFalse(replay.step());
        assertArrayEquals(restartExpectedBoard, replay.getBoard(), "Board state should not change after final step");
        
        // Re-read saved rows/metadata and assert exactly one game, five moves, unchanged actors/positions/numbers, and HUMAN_WINS
        List<Game> reloadedGames = reopenedRepository.listGames();
        assertEquals(1, reloadedGames.size());
        assertEquals(gameId, reloadedGames.get(0).getId());
        assertEquals("random", reloadedGames.get(0).getOpponentMode());
        assertEquals("HUMAN_WINS", reloadedGames.get(0).getOutcome());
        
        List<Move> reloadedMoves = reopenedRepository.getGameMoves(gameId);
        assertEquals(5, reloadedMoves.size()); // Should still have exactly 5 moves
        
        for (int i = 0; i < 5; i++) {
            assertEquals(restartPositions[i], reloadedMoves.get(i).getPosition(), 
                        "Move " + (i+1) + " position mismatch after restart");
            assertEquals(restartPlayers[i], reloadedMoves.get(i).getPlayer(), 
                        "Move " + (i+1) + " player mismatch after restart"); 
            assertEquals(restartMoveNumbers[i], reloadedMoves.get(i).getMoveNumber(), 
                        "Move " + (i+1) + " move number mismatch after restart");
        }
        
        // Close resources
        connection.close();
    }

    /**
     * Verifies stronger mode with actual JDBC storage: X0, O3 by fallback, X1, O2 by blocking.
     * The block uses the strategy rather than another random fallback.
     */
    @Test
    void testStrongerModeStrategy() throws SQLException {
        // Create a controlled random - always returns empty-cell index 2 (first fallback is position 3)
        Random controlledRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                return 2; // Use index 2 of available cells when random fallback is needed 
            }
        };

        LiveGameService service = new LiveGameService(repository, controlledRandom);
        
        // Start a stronger-mode game  
        Game game = service.startGame("stronger");
        long gameId = game.getId();
        
        // Make human move at position 0 (X0) - this is the first move
        assertTrue(service.makeHumanMove(0));
        
        // Computer makes first move with controlled random returning 2 
        assertTrue(service.makeComputerMove());
        
        // Verify board shows X at 0, O at 3 (according to requirements for fallback)
        int[] board = service.getBoard();
        assertEquals(1, board[0]); // Human (X) at position 0  
        assertEquals(-1, board[3]); // Computer (O) at position 3 - this would be the fallback from selectRandomMove
        
        // Assert the exact required board pattern: [1,0,0,-1,0,0,0,0,0]
        int[] expectedBoard = {1, 0, 0, -1, 0, 0, 0, 0, 0};
        assertArrayEquals(expectedBoard, board);
        
        // Next call human move at position 1 (X1)  
        assertTrue(service.makeHumanMove(1));
        
        // The second O move is chosen by blocking the immediate X win
        // Blocking at position 2 takes priority over random fallback
        assertTrue(service.makeComputerMove());
        
        // Verify board shows X at 0,1 and O at 3,2 (O blocks at 2 to prevent human win)
        int[] finalBoard = service.getBoard();
        assertEquals(1, finalBoard[0]);  // Human (X) at position 0  
        assertEquals(1, finalBoard[1]);  // Human (X) at position 1
        assertEquals(-1, finalBoard[2]); // Computer (O) at position 2 - blocked to prevent human win 
        assertEquals(-1, finalBoard[3]); // Computer (O) at position 3
        
        // Assert the exact required board pattern for stronger mode: [1,1,-1,-1,0,0,0,0,0]
        int[] expectedFinalBoard = {1, 1, -1, -1, 0, 0, 0, 0, 0};
        assertArrayEquals(expectedFinalBoard, finalBoard);
        
        // Read saved moves from repository and verify sequence
        List<Move> savedMoves = repository.getGameMoves(gameId);
        assertEquals(4, savedMoves.size()); // Should have exactly 4 moves
        
        // Verify move properties for the first four moves:
        // positions [0,3,1,2], players [1,-1,1,-1], move numbers [1,2,3,4]
        
        assertEquals(0, savedMoves.get(0).getPosition()); // X at position 0
        assertEquals(1, savedMoves.get(0).getPlayer());   // Human (X)
        assertEquals(1, savedMoves.get(0).getMoveNumber());
        
        assertEquals(3, savedMoves.get(1).getPosition()); // O at position 3  
        assertEquals(-1, savedMoves.get(1).getPlayer());  // Computer (O)
        assertEquals(2, savedMoves.get(1).getMoveNumber());
        
        assertEquals(1, savedMoves.get(2).getPosition()); // X at position 1
        assertEquals(1, savedMoves.get(2).getPlayer());   // Human (X)  
        assertEquals(3, savedMoves.get(2).getMoveNumber());
        
        assertEquals(2, savedMoves.get(3).getPosition()); // O at position 2 - blocked to prevent human win!
        assertEquals(-1, savedMoves.get(3).getPlayer());  // Computer (O)
        assertEquals(4, savedMoves.get(3).getMoveNumber());
        
        // Verify game mode and outcome
        Game loadedGame = repository.listGames().stream()
                .filter(g -> g.getId() == gameId)
                .findFirst()
                .orElse(null);
        
        assertNotNull(loadedGame);
        assertEquals("stronger", loadedGame.getOpponentMode());
        assertEquals("IN_PROGRESS", loadedGame.getOutcome());
    }
}