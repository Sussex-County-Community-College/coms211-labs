package edu.sussex.tictactoe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

/**
 * Tests for the strong opponent functionality in GameModel.
 */
public class GameModelStrongOpponentTest {
    
    @Test
    public void testSelectStrongMoveNotInProgress() {
        GameModel model = new GameModel();
        
        // Test when game hasn't started yet
        assertEquals(-1, model.selectStrongMove(new Random(0)), "Should return -1 when game has not started");
        
        // Test when human wins 
        model.makeMove(0); // Human move (position 0)
        model.makeMove(3); // Computer move (position 3)  
        model.makeMove(1); // Human move (position 1)
        model.makeMove(4); // Computer move (position 4)
        model.makeMove(2); // Human move (winning move - position 2)
        
        assertEquals(GameModel.HUMAN_WINS, model.getGameStatus());
        assertEquals(-1, model.selectStrongMove(new Random(0)), "Should return -1 when game is over");
    }
    
    @Test
    public void testSelectStrongMoveNotComputersTurn() {
        GameModel model = new GameModel();
        
        // Make TWO legal moves so it's X's turn (after 2 moves, human should be playing)
        model.makeMove(0); // Human move (position 0) 
        model.makeMove(1); // Computer move (position 1)
        // Now it's human's turn again
        assertEquals(GameModel.HUMAN, model.getCurrentPlayer());
        assertEquals(-1, model.selectStrongMove(new Random(0)), "Should return -1 when it's not computer's turn");
    }
    
    @Test
    public void testSelectStrongMoveImmediateWin() {
        GameModel model = new GameModel();
        
        // After X0/O3/X1/O4/X8, O can win at 5 before blocking X at 2.
        model.makeMove(0); // Human (X) at position 0
        model.makeMove(3); // Computer (O) at position 3  
        model.makeMove(1); // Human (X) at position 1
        model.makeMove(4); // Computer (O) at position 4
        model.makeMove(8); // Human (X) at position 8
        
        // Snapshot board/turn/status before selecting move
        int[] originalBoard = model.getBoard();
        int originalPlayer = model.getCurrentPlayer();
        int originalStatus = model.getGameStatus();
        
        // Now O should be able to win by placing at position 5
        int selected = model.selectStrongMove(new Random(0));
        assertEquals(5, selected, "Should select winning move at position 5");
        
        // Verify board state is unchanged 
        assertArrayEquals(originalBoard, model.getBoard(), "Board should remain unchanged after selecting a move");
        assertEquals(originalPlayer, model.getCurrentPlayer(), "Current player should remain unchanged after selecting a move");  
        assertEquals(originalStatus, model.getGameStatus(), "Game status should remain unchanged after selecting a move");
        
        // Apply the selected move and verify it actually wins
        assertTrue(model.makeMove(selected));
        assertEquals(GameModel.COMPUTER_WINS, model.getGameStatus(), "Should be computer win after making winning move");
    }
    
    @Test
    public void testSelectStrongMoveBlockWin() {
        GameModel model = new GameModel();
        
        // After X0/O4/X1, O must block the top row at 2.
        model.makeMove(0); // Human (X) at position 0  
        model.makeMove(4); // Computer (O) at position 4
        model.makeMove(1); // Human (X) at position 1
        
        // Snapshot board/turn/status before selecting move
        int[] originalBoard = model.getBoard();
        int originalPlayer = model.getCurrentPlayer();
        int originalStatus = model.getGameStatus();
        
        // O should block X's win by placing at position 2 
        int selected = model.selectStrongMove(new Random(0));
        assertEquals(2, selected, "Should select blocking move at position 2");
        
        // Verify board state is unchanged
        assertArrayEquals(originalBoard, model.getBoard(), "Board should remain unchanged after selecting a move");
        assertEquals(originalPlayer, model.getCurrentPlayer(), "Current player should remain unchanged after selecting a move");  
        assertEquals(originalStatus, model.getGameStatus(), "Game status should remain unchanged after selecting a move");
        
        // Apply the selected move and verify it blocks win (should not be computer win yet) 
        assertTrue(model.makeMove(selected));
        assertEquals(GameModel.IN_PROGRESS, model.getGameStatus(), "Should still be in progress after blocking move");
    }
    
    @Test
    public void testSelectStrongMoveFallbackToRandom() {
        GameModel model = new GameModel();
        
        // With only X at center 4, O has no immediate win or block.
        model.makeMove(4); // Human (X) at position 4
        
        // Snapshot board/turn/status before selecting move
        int[] originalBoard = model.getBoard();
        int originalPlayer = model.getCurrentPlayer();
        int originalStatus = model.getGameStatus();
        
        // For this state, O should fall back to a random move 
        Random rand = new Random(0);
        int selected = model.selectStrongMove(rand);
        
        assertTrue(selected >= 0 && selected < 9, "Should return valid board position");
        assertTrue(model.isPositionEmpty(selected), "Selected position should be empty (and we can verify it's a legal move)");
        
        // Verify board state is unchanged
        assertArrayEquals(originalBoard, model.getBoard(), "Board should remain unchanged after selecting a move");
        assertEquals(originalPlayer, model.getCurrentPlayer(), "Current player should remain unchanged after selecting a move");  
        assertEquals(originalStatus, model.getGameStatus(), "Game status should remain unchanged after selecting a move");
    }
    
    @Test
    public void testSelectStrongMoveComplexScenario() {
        GameModel model = new GameModel();
        
        // After X0/O3/X1/O4/X8, O5 completes the middle row.
        model.makeMove(0); // Human (X) at position 0
        model.makeMove(3); // Computer (O) at position 3  
        model.makeMove(1); // Human (X) at position 1
        model.makeMove(4); // Computer (O) at position 4
        model.makeMove(8); // Human (X) at position 8
        
        // Snapshot board/turn/status before selecting move
        int[] originalBoard = model.getBoard();
        int originalPlayer = model.getCurrentPlayer();
        int originalStatus = model.getGameStatus();
        
        // O occupies 3 and 4; selecting 5 completes the middle row.
        int selected = model.selectStrongMove(new Random(0));
        assertEquals(5, selected, "Should select winning move at position 5");
        
        // Verify board state is unchanged  
        assertArrayEquals(originalBoard, model.getBoard(), "Board should remain unchanged after selecting a move");
        assertEquals(originalPlayer, model.getCurrentPlayer(), "Current player should remain unchanged after selecting a move");  
        assertEquals(originalStatus, model.getGameStatus(), "Game status should remain unchanged after selecting a move");
        
        // Apply the selected move and verify it actually wins
        model.makeMove(5);
        assertEquals(GameModel.COMPUTER_WINS, model.getGameStatus(), "Should be computer win after making winning move");
    }
    
    @Test
    public void testSelectStrongMoveBlockWinSpecificScenario() {
        GameModel model = new GameModel();
        
        // After X0/O4/X1, O must block at 2.
        model.makeMove(0); // Human (X) at position 0  
        model.makeMove(4); // Computer (O) at position 4
        model.makeMove(1); // Human (X) at position 1
        
        // Snapshot board/turn/status before selecting move
        int[] originalBoard = model.getBoard();
        int originalPlayer = model.getCurrentPlayer();
        int originalStatus = model.getGameStatus();
        
        int selected = model.selectStrongMove(new Random(0));
        assertEquals(2, selected, "Should select blocking move at position 2");
        
        // Verify board state is unchanged
        assertArrayEquals(originalBoard, model.getBoard(), "Board should remain unchanged after selecting a move");
        assertEquals(originalPlayer, model.getCurrentPlayer(), "Current player should remain unchanged after selecting a move");  
        assertEquals(originalStatus, model.getGameStatus(), "Game status should remain unchanged after selecting a move");
        
        // Apply the selected move and verify it blocks win (should not be computer win yet) 
        model.makeMove(2);
        assertEquals(GameModel.IN_PROGRESS, model.getGameStatus(), "Should still be in progress after blocking move");
    }
    
    @Test
    public void testSelectStrongMoveFullBoard() {
        GameModel model = new GameModel();
        
        // Play 0,1,2,4,3,5,7,6,8 for a full-board draw.
        model.makeMove(0); // Human (X) at position 0
        model.makeMove(1); // Computer (O) at position 1  
        model.makeMove(2); // Human (X) at position 2
        model.makeMove(4); // Computer (O) at position 4
        model.makeMove(3); // Human (X) at position 3
        model.makeMove(5); // Computer (O) at position 5
        model.makeMove(7); // Human (X) at position 7
        model.makeMove(6); // Computer (O) at position 6
        model.makeMove(8); // Human (X) at position 8 - last move, creates draw
        
        assertEquals(GameModel.DRAW, model.getGameStatus());
        assertEquals(-1, model.selectStrongMove(new Random(0)), "Should return -1 when board is full");
    }
    
    @Test
    public void testSelectStrongMoveDoesNotChangeState() {
        GameModel model = new GameModel();
        
        // Make a move to start the game  
        model.makeMove(4); // Human (X) at position 4
        
        // Get original state values
        int[] originalBoard = model.getBoard();
        int originalPlayer = model.getCurrentPlayer();
        int originalStatus = model.getGameStatus();
        
        // Select strong move - should not change game state
        Random rand = new Random(0);
        int selectedMove = model.selectStrongMove(rand);
        
        // Verify that nothing changed
        assertArrayEquals(originalBoard, model.getBoard(), "Board should remain unchanged");
        assertEquals(originalPlayer, model.getCurrentPlayer(), "Current player should remain unchanged");  
        assertEquals(originalStatus, model.getGameStatus(), "Game status should remain unchanged");
    }
}