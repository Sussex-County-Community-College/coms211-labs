package edu.sussex.tictactoe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GameModel class implementing tic-tac-toe game rules.
 */
public class GameModelTest {
    
    @Test
    public void testInitialGameState() {
        GameModel model = new GameModel();
        
        // Check initial board is empty
        int[] board = model.getBoard();
        for (int i = 0; i < 9; i++) {
            assertEquals(GameModel.EMPTY, board[i], "All positions should be empty initially");
        }
        
        // Check human starts first
        assertEquals(GameModel.HUMAN, model.getCurrentPlayer(), "Human player should start first");
        
        // Check game status is not started
        assertEquals(GameModel.GAME_NOT_STARTED, model.getGameStatus(), "Game should not have started yet");
    }
    
    @Test
    public void testValidMoves() {
        GameModel model = new GameModel();
        
        // Make a valid move - this should start the game automatically
        assertTrue(model.makeMove(4), "Move to center position should be valid");
        
        // Check board state after move
        int[] board = model.getBoard();
        assertEquals(GameModel.HUMAN, board[4], "Human player should have made move at position 4");
        assertEquals(GameModel.COMPUTER, model.getCurrentPlayer(), "Computer player should now be active");
        
        // Make another valid move - Computer's turn (O)
        assertTrue(model.makeMove(0), "Move to top-left position should be valid");
        
        // Check board state after second move
        board = model.getBoard();
        assertEquals(GameModel.HUMAN, board[4], "Human player should have made move at position 4");
        assertEquals(GameModel.COMPUTER, board[0], "Computer player should have made move at position 0");
        assertEquals(GameModel.HUMAN, model.getCurrentPlayer(), "Human player should now be active again");
    }
    
    @Test
    public void testInvalidMoves() {
        GameModel model = new GameModel();
        
        // Make first move to start the game 
        model.makeMove(4);
        
        // Test out-of-range moves
        assertFalse(model.makeMove(-1), "Move to position -1 should be invalid");
        assertFalse(model.makeMove(9), "Move to position 9 should be invalid");
        
        // Test occupied position
        assertFalse(model.makeMove(4), "Move to already occupied position 4 should be invalid");
        
        // Verify board state hasn't changed due to invalid moves
        int[] board = model.getBoard();
        assertEquals(GameModel.HUMAN, board[4], "Position 4 should still be occupied by human player");
    }
    
    @Test
    public void testHumanWinsRow() {
        GameModel model = new GameModel();
        
        // Make moves that lead to human win - top row: X | X | X
        model.makeMove(0); // Human move (position 0)
        model.makeMove(3); // Computer move (position 3)  
        model.makeMove(1); // Human move (position 1)
        model.makeMove(4); // Computer move (position 4)
        model.makeMove(2); // Human move (winning move - position 2)
        
        assertEquals(GameModel.HUMAN_WINS, model.getGameStatus(), "Human should win with top row");
    }
    
    @Test
    public void testComputerWinsColumn() {
        GameModel model = new GameModel();
        
        // Make moves that lead to computer win - left column: O | X | X  
        //                                           O | X | _
        //                                           O | _ | _
        // Using ordered legal moves 1,0,4,3,8,6
        model.makeMove(1); // Human move (position 1)
        model.makeMove(0); // Computer move (position 0) 
        model.makeMove(4); // Human move (position 4)
        model.makeMove(3); // Computer move (position 3)
        model.makeMove(8); // Human move (position 8)
        model.makeMove(6); // Computer move (position 6 - winning move)
        
        assertEquals(GameModel.COMPUTER_WINS, model.getGameStatus(), "Computer should win with left column");
    }
    
    @Test
    public void testHumanWinsDiagonal() {
        GameModel model = new GameModel();
        
        // Make moves that lead to human win - diagonal: X | _ | _
        //                                         _ | X | _
        //                                         _ | _ | X
        model.makeMove(0); // Human move (position 0)
        model.makeMove(1); // Computer move (position 1)  
        model.makeMove(4); // Human move (position 4)
        model.makeMove(2); // Computer move (position 2)
        model.makeMove(8); // Human move (position 8 - winning move)
        
        assertEquals(GameModel.HUMAN_WINS, model.getGameStatus(), "Human should win with diagonal");
    }
    
    @Test
    public void testDraw() {
        GameModel model = new GameModel();
        
        // Make moves to create a draw situation:
        // X | O | X 
        // O | X | O  
        // O | X | _
        // Using ordered legal moves 0,1,2,4,3,5,7,6,8
        model.makeMove(0); // Human (X) move to position 0
        model.makeMove(1); // Computer (O) move to position 1
        model.makeMove(2); // Human (X) move to position 2 
        model.makeMove(4); // Computer (O) move to position 4
        model.makeMove(3); // Human (X) move to position 3
        model.makeMove(5); // Computer (O) move to position 5
        model.makeMove(7); // Human (X) move to position 7
        model.makeMove(6); // Computer (O) move to position 6
        model.makeMove(8); // Human (X) move to position 8 - should be a draw
        
        assertEquals(GameModel.DRAW, model.getGameStatus(), "Game should end in a draw");
    }
    
    @Test
    public void testMovesAfterGameOver() {
        GameModel model = new GameModel();
        
        // Make moves that lead to human win
        model.makeMove(0); // Human move
        model.makeMove(3); // Computer move  
        model.makeMove(1); // Human move
        model.makeMove(4); // Computer move
        model.makeMove(2); // Human move (winning move)
        
        assertEquals(GameModel.HUMAN_WINS, model.getGameStatus(), "Human should win");
        
        // Try to make more moves after game is over - should be rejected
        assertFalse(model.makeMove(5), "Should not be able to make moves after game is over");
        assertFalse(model.makeMove(6), "Should not be able to make moves after game is over");
        
        // Verify board state hasn't changed due to invalid moves after game over
        int[] board = model.getBoard();
        assertEquals(GameModel.HUMAN, board[0], "Position 0 should still be occupied by human player");
        assertEquals(GameModel.HUMAN, board[1], "Position 1 should still be occupied by human player");
        assertEquals(GameModel.HUMAN, board[2], "Position 2 should still be occupied by human player (winning move)");
    }
    
    @Test
    public void testIsPositionEmpty() {
        GameModel model = new GameModel();
        
        // Make first move to start the game 
        model.makeMove(4); // Make a move to position 4
        
        // Position 4 should no longer be empty
        assertFalse(model.isPositionEmpty(4), "Position 4 should not be empty after move");
        
        // Other positions should still be empty
        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                assertTrue(model.isPositionEmpty(i), "Position " + i + " should still be empty");
            }
        }
    }
    
    @Test
    public void testResetGame() {
        GameModel model = new GameModel();
        
        // Make some moves to change the game state - this will automatically start the game 
        model.makeMove(0);
        model.makeMove(1);
        model.makeMove(2);
        
        // Verify game is in progress
        assertEquals(GameModel.IN_PROGRESS, model.getGameStatus(), "Game should be in progress");
        
        // Reset the game
        model.resetGame();
        
        // Check that everything resets properly
        int[] board = model.getBoard();
        for (int i = 0; i < 9; i++) {
            assertEquals(GameModel.EMPTY, board[i], "All positions should be empty after reset");
        }
        
        assertEquals(GameModel.HUMAN, model.getCurrentPlayer(), "Human player should start again");
        assertEquals(GameModel.GAME_NOT_STARTED, model.getGameStatus(), "Game status should be not started");
    }
    
    @Test
    public void testBoardDisplay() {
        GameModel model = new GameModel();
        
        // Make some moves to create a visible board
        model.makeMove(0); // Human move at position 0 (X)
        model.makeMove(4); // Computer move at position 4 (O) 
        
        String display = model.getBoardDisplay();
        assertNotNull(display, "Board display should not be null");
        
        // Should contain X and O in the right positions
        assertTrue(display.contains("X"), "Board display should show human player as X");
        assertTrue(display.contains("O"), "Board display should show computer player as O");
    }
    
    @Test
    public void testDefensiveBoardCopy() {
        GameModel model = new GameModel();
        
        // Make a move to start the game
        model.makeMove(4);
        
        // Get board and modify it externally - this shouldn't affect the internal state
        int[] externalBoard = model.getBoard();
        externalBoard[0] = GameModel.COMPUTER; // Try to modify position 0
        
        // Check that internal board is unchanged (defensive copy)
        int[] internalBoard = model.getBoard();
        assertEquals(GameModel.HUMAN, internalBoard[4], "Internal board should still have HUMAN at position 4");
        assertEquals(GameModel.EMPTY, internalBoard[0], "Internal board should still have EMPTY at position 0");
    }
    
    @Test
    public void testInvalidMoveDoesNotChangeTurn() {
        GameModel model = new GameModel();
        
        // Make first move to start the game 
        model.makeMove(4);
        
        int originalPlayer = model.getCurrentPlayer();
        
        // Try invalid move - should not change turn
        assertFalse(model.makeMove(-1), "Invalid move should be rejected");
        assertEquals(originalPlayer, model.getCurrentPlayer(), "Turn should remain unchanged after invalid move");
        
        // Try another invalid move - should not change turn  
        assertFalse(model.makeMove(4), "Move to occupied position should be rejected");
        assertEquals(originalPlayer, model.getCurrentPlayer(), "Turn should remain unchanged after invalid move");
    }
}