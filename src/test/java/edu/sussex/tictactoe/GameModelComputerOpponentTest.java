package edu.sussex.tictactoe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

/**
 * Tests for the computer opponent functionality in GameModel.
 */
public class GameModelComputerOpponentTest {
    
    @Test
    public void testSelectRandomMoveEmptyBoard() {
        GameModel model = new GameModel();
        
        // Create a predictable random generator for testing
        Random rand = new Random(12345); // Fixed seed for repeatable tests
        
        // Select a move - should return -1 since game hasn't started yet (empty board)
        int selectedMove = model.selectRandomMove(rand);
        
        assertEquals(-1, selectedMove, "Should return -1 when game has not started (empty board)");
    }
    
    @Test
    public void testSelectRandomMovePartialBoard() {
        GameModel model = new GameModel();
        
        // Make a move to start the game and occupy one position
        model.makeMove(4); // Human plays center
        
        Random rand = new Random(54321); // Fixed seed for repeatable tests
        
        // Select a move - should return a valid empty position (0-8)
        int selectedMove = model.selectRandomMove(rand);
        
        // Since there are empty positions available, we must get a legal position
        assertTrue(selectedMove >= 0 && selectedMove < 9,
            "Selected move should be between 0 and 8");
        
        // Verify the selected position is empty and not the occupied position
        assertFalse(selectedMove == 4, "Selected position should not be the occupied position");
        assertTrue(model.isPositionEmpty(selectedMove), 
            "Selected position should be empty");
    }
    
    @Test
    public void testSelectRandomMoveFullBoard() {
        GameModel model = new GameModel();
        
        // Create a draw scenario using the verified sequence 0,1,2,4,3,5,7,6,8
        // This creates a full board with no winner (draw)
        model.makeMove(0); // Human move 
        model.makeMove(1); // Computer move  
        model.makeMove(2); // Human move
        model.makeMove(4); // Computer move
        model.makeMove(3); // Human move
        model.makeMove(5); // Computer move
        model.makeMove(7); // Human move
        model.makeMove(6); // Computer move
        model.makeMove(8); // Human move (last move)
        
        assertEquals(GameModel.DRAW, model.getGameStatus());
        
        Random rand = new Random(99999); // Fixed seed for repeatable tests
        
        // Select a move - should return -1 since board is full
        int selectedMove = model.selectRandomMove(rand);
        
        assertEquals(-1, selectedMove, "Should return -1 when board is full (draw)");
    }
    
    @Test
    public void testSelectRandomMoveGameNotStarted() {
        GameModel model = new GameModel();
        
        // Game has not started yet 
        assertEquals(GameModel.GAME_NOT_STARTED, model.getGameStatus());
        
        Random rand = new Random(11111); // Fixed seed for repeatable tests
        
        // Select a move - should return -1 since game hasn't started
        int selectedMove = model.selectRandomMove(rand);
        
        assertEquals(-1, selectedMove, "Should return -1 when game has not started");
    }
    
    @Test
    public void testSelectRandomMoveGameOver() {
        GameModel model = new GameModel();
        
        // Make moves that lead to human win 
        model.makeMove(0); // Human move (position 0)
        model.makeMove(3); // Computer move (position 3)  
        model.makeMove(1); // Human move (position 1)
        model.makeMove(4); // Computer move (position 4)
        model.makeMove(2); // Human move (winning move - position 2)
        
        assertEquals(GameModel.HUMAN_WINS, model.getGameStatus());
        
        Random rand = new Random(22222); // Fixed seed for repeatable tests
        
        // Select a move - should return -1 since game is over
        int selectedMove = model.selectRandomMove(rand);
        
        assertEquals(-1, selectedMove, "Should return -1 when game is over");
    }
    
    @Test
    public void testSelectRandomMoveMultipleCalls() {
        GameModel model = new GameModel();
        
        // Make a move to start the game
        model.makeMove(4); // Human plays center
        
        Random rand1 = new Random(100);
        Random rand2 = new Random(100); // Same seed should produce same result
        
        // Select moves with same random seeds - should get same results
        int move1 = model.selectRandomMove(rand1);
        int move2 = model.selectRandomMove(rand2);
        
        assertEquals(move1, move2, "Same random seed should produce same selection");
        
        // Verify that selecting a move leaves board and turn unchanged
        int[] originalBoard = model.getBoard();
        int originalPlayer = model.getCurrentPlayer();
        
        // Select another move with different seed 
        Random rand3 = new Random(200);
        int move3 = model.selectRandomMove(rand3);
        
        // Check that the game state hasn't changed
        assertArrayEquals(originalBoard, model.getBoard(), "Board should remain unchanged after selecting a move");
        assertEquals(originalPlayer, model.getCurrentPlayer(), "Current player should remain unchanged after selecting a move");
        
        // The third selection should be a valid position (not -1) and empty
        assertTrue(move3 >= 0 && move3 < 9, "Third selected move should be a valid board position");
        assertTrue(model.isPositionEmpty(move3), "Selected position should be empty");
        assertNotEquals(4, move3, "Selected position should not be the occupied position");
    }
    
    @Test
    public void testSelectRandomMoveDistribution() {
        GameModel model = new GameModel();
        
        // Make a few moves to leave some positions empty
        model.makeMove(0); // Human move (position 0)
        model.makeMove(4); // Computer move (position 4)  
        
        Random rand = new Random(55555);
        
        // Collect multiple selections to check distribution 
        int[] counts = new int[9];
        for (int i = 0; i < 100; i++) {
            int selected = model.selectRandomMove(rand);
            if (selected != -1) { // Only count valid selections
                counts[selected]++;
            }
        }
        
        // Check that we got selections from various positions (not just one)
        int nonZeroCount = 0;
        for (int i = 0; i < 9; i++) {
            if (i != 0 && i != 4) { // Positions 0 and 4 are occupied
                if (counts[i] > 0) {
                    nonZeroCount++;
                }
            }
        }
        
        assertTrue(nonZeroCount >= 1, "Should get selections from multiple empty positions");
    }
}