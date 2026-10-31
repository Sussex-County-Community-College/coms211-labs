package edu.sussex.tictactoe;

import edu.sussex.tictactoe.model.Game;
import edu.sussex.tictactoe.model.Move;
import edu.sussex.tictactoe.repository.GameRepository;
import edu.sussex.tictactoe.service.LiveGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class LiveGameServiceModeTest {
    
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
    void testModePersistenceInDatabase() throws SQLException {
        LiveGameService service = new LiveGameService(mockRepository, random);
        
        // Start a game with "random" mode
        Game game1 = service.startGame("random");
        assertEquals("random", game1.getOpponentMode());
        assertEquals("random", service.getCurrentOpponentMode());
        
        // Start another game with "stronger" mode  
        Game game2 = service.startGame("stronger");
        assertEquals("stronger", game2.getOpponentMode());
        assertEquals("stronger", service.getCurrentOpponentMode());
    }
    
    @Test
    void testStrongerModeComputerMove() throws SQLException {
        // Using controlled random to make the behavior predictable for testing
        Random controlledRandom = new Random(0);  // Fixed seed for reproducibility
        
        LiveGameService service = new LiveGameService(mockRepository, controlledRandom);
        
        // Start a game with "stronger" mode
        Game game = service.startGame("stronger");
        assertEquals("stronger", service.getCurrentOpponentMode());
        
        // Make human move (X at position 0)
        assertTrue(service.makeHumanMove(0));
        
        // Computer should make move. With stronger mode, it must prioritize blocking 
        // or winning moves. In this simple scenario with only one move made,
        // the computer will just pick a random legal move since there's no immediate 
        // win/block situation.
        assertTrue(service.makeComputerMove());
        
        // Game state should be updated
        assertNotNull(service.getBoard());
    }
    
    @Test
    void testModeBehaviorWithControlledRandom() throws SQLException {
        // Test that the mode is correctly stored and retrieved, with proper assertions
        
        LiveGameService service = new LiveGameService(mockRepository, new Random(0)); // Fixed seed for reproducibility
        Game game = service.startGame("stronger");
        
        // Verify that game started with correct opponent mode
        assertEquals("stronger", game.getOpponentMode());
        assertEquals("stronger", service.getCurrentOpponentMode());
        
        // Make human move at position 0 (X0) 
        assertTrue(service.makeHumanMove(0));
        
        // Computer should make move using stronger strategy
        assertTrue(service.makeComputerMove());
        
        // Verify mode is still correct
        assertEquals("stronger", service.getCurrentOpponentMode());  
    }
}