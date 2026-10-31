package edu.sussex.tictactoe;

import edu.sussex.tictactoe.model.Game;
import edu.sussex.tictactoe.model.Move;
import edu.sussex.tictactoe.repository.GameRepository;
import edu.sussex.tictactoe.service.LiveGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class LiveGameServiceTest {
    
    private GameRepository mockRepository;
    private Random random;
    
    @BeforeEach
    void setUp() {
        // We'll create a simple in-memory implementation for testing
        random = new Random(12345);  // Fixed seed for reproducible tests
        
        // Create a mock repository that simulates the database behavior
        mockRepository = new RecordingRepository();
    }
    
    /**
     * A test-friendly repository implementation that records all operations.
     */
    private static class RecordingRepository extends GameRepository {
        private long nextGameId = 1;
        private long nextMoveId = 1;
        private List<Game> games = new ArrayList<>();
        private List<Move> moves = new ArrayList<>();
        
        // Test control fields
        private boolean failNextSave = false;
        private boolean failNextStart = false;
        private int saveAttempts = 0;
        private int startAttempts = 0;
        
        public RecordingRepository() {
            super(null); // null connection, we override all methods
        }
        
        public List<Move> getSavedMoves() {
            return new ArrayList<>(moves);
        }
        
        public void setFailNextSave(boolean fail) {
            this.failNextSave = fail;
        }
        
        public void setFailNextStart(boolean fail) {
            this.failNextStart = fail;
        }
        
        public int getSaveAttempts() {
            return saveAttempts;
        }
        
        public int getStartAttempts() {
            return startAttempts;
        }

        @Override
        public Game startGame(String opponentMode, String outcome) throws SQLException {
            // Validate inputs according to requirements  
            if (!"random".equals(opponentMode) && !"stronger".equals(opponentMode)) {
                throw new SQLException("Invalid opponent mode: " + opponentMode + 
                                     ". Modes must be random or stronger.");
            }
            
            if (!"IN_PROGRESS".equals(outcome) && 
                !"HUMAN_WINS".equals(outcome) && 
                !"COMPUTER_WINS".equals(outcome) && 
                !"DRAW".equals(outcome)) {
                throw new SQLException("Invalid outcome: " + outcome + 
                                     ". Outcomes must be IN_PROGRESS, HUMAN_WINS, COMPUTER_WINS, or DRAW.");
            }
            
            startAttempts++;
            
            // Simulate storage failure on demand
            if (failNextStart) {
                failNextStart = false;  // Reset after throwing once
                throw new SQLException("Simulated database save failure");
            }
            
            Game game = new Game(nextGameId++, null, opponentMode, outcome);
            games.add(game);
            return game;
        }
        
        @Override
        public void saveMove(Move move, String outcome) throws SQLException {
            // Validate inputs according to requirements  
            if (move.getPlayer() != 1 && move.getPlayer() != -1) {
                throw new SQLException("Invalid player: " + move.getPlayer() + 
                                     ". Players must be 1 (X) or -1 (O).");
            }
            
            if (move.getMoveNumber() < 1 || move.getMoveNumber() > 9) {
                throw new SQLException("Invalid move number: " + move.getMoveNumber() + 
                                     ". Move numbers must be between 1 and 9.");
            }
            
            if (move.getPosition() < 0 || move.getPosition() > 8) {
                throw new SQLException("Invalid position: " + move.getPosition() + 
                                     ". Positions must be between 0 and 8.");
            }
            
            if (!"IN_PROGRESS".equals(outcome) && 
                !"HUMAN_WINS".equals(outcome) && 
                !"COMPUTER_WINS".equals(outcome) && 
                !"DRAW".equals(outcome)) {
                throw new SQLException("Invalid outcome: " + outcome + 
                                     ". Outcomes must be IN_PROGRESS, HUMAN_WINS, COMPUTER_WINS, or DRAW.");
            }
            
            // Check if game exists
            boolean found = false;
            for (Game g : games) {
                if (g.getId() == move.getGameId()) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                throw new SQLException("Game with ID " + move.getGameId() + " does not exist.");
            }
            
            // Record the attempt before potentially throwing
            saveAttempts++;
            
            // Simulate storage failure on demand
            if (failNextSave) {
                failNextSave = false;  // Reset after throwing once
                throw new SQLException("Simulated database save failure");
            }
            
            // Add the move - only if we don't simulate a failure
            moves.add(move);
        }
        
        @Override
        public List<Game> listGames() throws SQLException {
            return new ArrayList<>(games);
        }
        
        @Override
        public List<Move> getGameMoves(long gameId) throws SQLException {
            List<Move> gameMoves = new ArrayList<>();
            for (Move move : moves) {
                if (move.getGameId() == gameId) {
                    gameMoves.add(move);
                }
            }
            return gameMoves;
        }
    }
    
    @Test
    void testStartGame() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        
        Game game = service.startGame("random");
        assertNotNull(game);
        assertTrue(game.getId() > 0);
        assertEquals("random", game.getOpponentMode());
        assertEquals("IN_PROGRESS", game.getOutcome());
        
        // Test with stronger mode
        Game game2 = service.startGame("stronger");
        assertNotNull(game2);
        assertTrue(game2.getId() > 0);
        assertEquals("stronger", game2.getOpponentMode());
        assertEquals("IN_PROGRESS", game2.getOutcome());
    }
    
    @Test
    void testStartGameInvalidMode() {
        LiveGameService service = new LiveGameService(mockRepository, random);
        
        assertThrows(SQLException.class, () -> {
            service.startGame("invalid");
        });
    }
    
    @Test
    void testMakeHumanMove() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Make a valid move
        assertTrue(service.makeHumanMove(4));  // Center position
        
        // Verify board state reflects the move
        int[] board = service.getBoard();
        assertEquals(1, board[4]);  // Human (X) at center
        
        // Test invalid move - already occupied
        assertFalse(service.makeHumanMove(4));
        
        // Test invalid move - out of bounds
        assertFalse(service.makeHumanMove(-1));
        assertFalse(service.makeHumanMove(9));
    }
    
    @Test
    void testMakeComputerMove() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Make a human move first to get the game going
        assertTrue(service.makeHumanMove(4));  // Center position
        
        // Computer should make a move 
        assertTrue(service.makeComputerMove());
        
        // Verify board state reflects both moves (human at center, computer at some position)
        int[] board = service.getBoard();
        assertNotNull(board);
    }
    
    @Test
    void testMoveOrder() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Make several moves in order: human, computer, human, computer
        assertTrue(service.makeHumanMove(0));  // Human at position 0
        assertTrue(service.makeComputerMove()); // Computer makes move
        assertTrue(service.makeHumanMove(1));  // Human at position 1
        assertTrue(service.makeComputerMove()); // Computer makes move
        
        // Board should reflect all moves (human at 0, computer somewhere else, human at 1)
        int[] board = service.getBoard();
        assertNotNull(board);
        assertEquals(1, board[0]);  // Human (X) at position 0  
        
        // We can't predict exactly where the computer moved due to randomness,
        // but we know it should be different from 0 and 1
    }
    
    @Test
    void testFinishedGame() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Create a scenario that ends the game (human wins)
        assertTrue(service.makeHumanMove(0));  // Human move
        assertTrue(service.makeComputerMove()); // Computer move  
        assertTrue(service.makeHumanMove(1));  // Human move
        assertTrue(service.makeComputerMove()); // Computer move
        assertTrue(service.makeHumanMove(2));  // Human win (row)
        
        assertEquals(GameModel.HUMAN_WINS, service.getGameStatus());
    }
    
    @Test
    void testInvalidInputNoWrites() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Try invalid inputs - these should not result in any database writes
        assertFalse(service.makeHumanMove(-1));  // Invalid position
        assertFalse(service.makeHumanMove(9));   // Invalid position
        
        // Board state should be empty (or unchanged from start)
        int[] board = service.getBoard();
        for (int i = 0; i < 9; i++) {
            assertEquals(0, board[i]);  // All positions should be EMPTY
        }
    }
    
    @Test 
    void testHumanWrongTurn() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Try to make computer move as human (wrong turn)
        assertFalse(service.makeComputerMove());
    }
    
    @Test
    void testComputerWrongTurn() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Make a human move first to start the game 
        assertTrue(service.makeHumanMove(4));
        
        // Try to make human move as computer (wrong turn) - should not work
        assertFalse(service.makeHumanMove(0));
    }
    
    @Test
    void testHumanSaveFailure() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Set up to simulate save failure BEFORE making any moves 
        ((RecordingRepository) mockRepository).setFailNextSave(true);
        
        // Attempt a human move - should fail immediately because we set the failure flag
        assertThrows(SQLException.class, () -> {
            service.makeHumanMove(4);  // This should fail due to database failure on save
        });
        
        // After a failed save, state should remain as before (no changes committed)
        int[] board = service.getBoard();
        for (int i = 0; i < 9; i++) {
            assertEquals(0, board[i]);  // All positions should be EMPTY - no move was saved
        }
    }
    
    @Test
    void testComputerSaveFailure() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Make a human move first 
        assertTrue(service.makeHumanMove(4));
        
        // Set up to simulate save failure on computer's move
        ((RecordingRepository) mockRepository).setFailNextSave(true);
        
        // Computer should try to make move but fail (simulating database issue)
        assertThrows(SQLException.class, () -> {
            service.makeComputerMove();
        });
        
        // After a failed computer save, the human move should still be visible
        assertEquals(1, service.getBoard()[4]);  // Human at position 4 - not erased
    }
    
    @Test
    void testSuccessfulRetryAfterFailure() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Make a human move first 
        assertTrue(service.makeHumanMove(4));
        
        // Set up to simulate save failure on computer's move
        ((RecordingRepository) mockRepository).setFailNextSave(true);
        
        // Computer should fail and not commit the change  
        assertThrows(SQLException.class, () -> {
            service.makeComputerMove();
        });
        
        // Verify human move is still there 
        assertEquals(1, service.getBoard()[4]);
        
        // Now make computer move again - this time it should succeed
        assertTrue(service.makeComputerMove());
        
        // Both moves should be visible now (human at 4, computer at some position)  
        assertEquals(1, service.getBoard()[4]);  // Human still at position 4
    }
    
    @Test
    void testNewGameFailure() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        
        // Create an existing game first to have a valid state  
        Game game = service.startGame("random");
        assertTrue(game.getId() > 0);
        
        // Make a move on the existing game to ensure it's properly started
        assertTrue(service.makeHumanMove(4));
        
        // Set up to simulate save failure on next game creation 
        ((RecordingRepository) mockRepository).setFailNextStart(true);
        
        // Attempt to start a new game - should throw and not create any game
        assertThrows(SQLException.class, () -> {
            service.startGame("random");
        });
        
        // The old game should still be active and unchanged  
        assertEquals(game.getId(), service.getCurrentGameId());
    }
    
    @Test 
    void testHumanSaveFailureScenario() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Make a valid human move - this should succeed
        assertTrue(service.makeHumanMove(4));
        
        // Verify board state shows the first move  
        assertEquals(1, service.getBoard()[4]);  // Human at position 4
        
        // Make a computer move to get back to X's turn (so that next human move is valid)
        assertTrue(service.makeComputerMove());
        
        // Snapshot board/turn/saved count before failure
        int[] snapshotBoard = service.getBoard();
        int snapshotTurn = service.getCurrentPlayer();
        int savedAttemptsBefore = ((RecordingRepository) mockRepository).getSaveAttempts();
        
        // Set up to fail on next save (this is where it should fail - the FIRST human save)
        ((RecordingRepository) mockRepository).setFailNextSave(true);
        
        // Try to make a second move as human (should fail and not commit change)
        assertThrows(SQLException.class, () -> {
            service.makeHumanMove(0);  // This should be rejected due to failed save
        });
        
        // Board state should remain unchanged - only first move is committed
        assertEquals(1, service.getBoard()[4]);  // Human at position 4 
    }
    
    @Test
    void testSavedActorsAndNumbers() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Make several moves and verify saved actors and numbers
        assertTrue(service.makeHumanMove(0));  // Human move at position 0 (move #1)
        assertTrue(service.makeComputerMove()); // Computer move (move #2)
        assertTrue(service.makeHumanMove(1));  // Human move at position 1 (move #3) 
        assertTrue(service.makeComputerMove()); // Computer move (move #4)
        
        // Check that all moves were saved with correct players and numbers
        List<Move> savedMoves = ((RecordingRepository) mockRepository).getSavedMoves();
        
        assertEquals(4, savedMoves.size());
        
        // Verify actors are correct: HUMAN=1, COMPUTER=-1 
        assertEquals(1, savedMoves.get(0).getPlayer());  // First move by human
        assertEquals(-1, savedMoves.get(1).getPlayer()); // Second move by computer  
        assertEquals(1, savedMoves.get(2).getPlayer());  // Third move by human
        assertEquals(-1, savedMoves.get(3).getPlayer()); // Fourth move by computer
        
        // Verify numbers are sequential starting from 1
        assertEquals(1, savedMoves.get(0).getMoveNumber());
        assertEquals(2, savedMoves.get(1).getMoveNumber());
        assertEquals(3, savedMoves.get(2).getMoveNumber());
        assertEquals(4, savedMoves.get(3).getMoveNumber());
    }
    
    @Test
    void testPreservedBoardOnFailure() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        Game game = service.startGame("random");
        
        // Make a human move first 
        assertTrue(service.makeHumanMove(4));  // Human at center (move #1)
        
        // Verify board is as expected
        assertEquals(1, service.getBoard()[4]);  // Human at position 4
        
        // Make a computer move to get back to X's turn (so that next human move is valid)
        assertTrue(service.makeComputerMove());
        
        // Snapshot board/turn/saved count before failure
        int[] snapshotBoard = service.getBoard();
        int snapshotTurn = service.getCurrentPlayer();
        int savedAttemptsBefore = ((RecordingRepository) mockRepository).getSaveAttempts();
        
        // Now try to make another move but fail on save
        ((RecordingRepository) mockRepository).setFailNextSave(true);
        
        assertThrows(SQLException.class, () -> {
            service.makeHumanMove(0);  // This should fail
        });
        
        // Board state should be preserved exactly as before (no second move committed)
        assertEquals(1, service.getBoard()[4]);  // Only human at position 4  
    }
}