package edu.sussex.tictactoe;

import java.util.Arrays;
import java.util.Random;

/**
 * Tic-tac-toe game model that manages the board state and game rules.
 * Human player is X, computer player is O.
 * Board positions are row-major 0-8: 0 1 2 / 3 4 5 / 6 7 8.
 */
public class GameModel {
    public static final int EMPTY = 0;
    public static final int HUMAN = 1;   // X
    public static final int COMPUTER = -1; // O
    
    public static final int GAME_NOT_STARTED = 0;
    public static final int IN_PROGRESS = 1;
    public static final int HUMAN_WINS = 2;
    public static final int COMPUTER_WINS = 3;
    public static final int DRAW = 4;
    
    private final int[] board;
    private int currentPlayer;
    private int gameStatus;
    
    /**
     * Creates a new empty game.
     */
    public GameModel() {
        this.board = new int[9];
        Arrays.fill(board, EMPTY);
        this.currentPlayer = HUMAN; // Human starts first
        this.gameStatus = GAME_NOT_STARTED;
    }
    
    /**
     * Makes a move at the specified position.
     * @param position The board position (0-8)
     * @return true if the move was valid and executed, false otherwise
     */
    public boolean makeMove(int position) {
        // Check if position is valid
        if (position < 0 || position > 8) {
            return false;
        }
        
        // Check if position is already occupied
        if (board[position] != EMPTY) {
            return false;
        }
        
        // If this is the first move, transition to IN_PROGRESS state
        if (gameStatus == GAME_NOT_STARTED) {
            gameStatus = IN_PROGRESS;
        }
        
        // Check if game is over
        if (gameStatus != IN_PROGRESS) {
            return false;
        }
        
        // Make the move
        board[position] = currentPlayer;
        
        // Check for win or draw and update game status
        checkGameStatus();
        
        // Switch player for next turn only if game is still in progress
        if (gameStatus == IN_PROGRESS) {
            currentPlayer = -currentPlayer; // Switches between 1 and -1
        }
        
        return true;
    }
    
    /**
     * Checks if the current game state has a winner or is a draw.
     */
    private void checkGameStatus() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            int rowSum = board[i * 3] + board[i * 3 + 1] + board[i * 3 + 2];
            if (rowSum == 3) {
                gameStatus = HUMAN_WINS;
                return;
            } else if (rowSum == -3) {
                gameStatus = COMPUTER_WINS;
                return;
            }
        }
        
        // Check columns
        for (int i = 0; i < 3; i++) {
            int colSum = board[i] + board[i + 3] + board[i + 6];
            if (colSum == 3) {
                gameStatus = HUMAN_WINS;
                return;
            } else if (colSum == -3) {
                gameStatus = COMPUTER_WINS;
                return;
            }
        }
        
        // Check diagonals
        int diag1Sum = board[0] + board[4] + board[8];
        int diag2Sum = board[2] + board[4] + board[6];
        
        if (diag1Sum == 3 || diag2Sum == 3) {
            gameStatus = HUMAN_WINS;
            return;
        } else if (diag1Sum == -3 || diag2Sum == -3) {
            gameStatus = COMPUTER_WINS;
            return;
        }
        
        // Check for draw
        boolean isDraw = true;
        for (int i = 0; i < 9; i++) {
            if (board[i] == EMPTY) {
                isDraw = false;
                break;
            }
        }
        
        if (isDraw) {
            gameStatus = DRAW;
        }
    }
    
    /**
     * Gets the current board state.
     * @return Array of 9 integers representing the board positions
     */
    public int[] getBoard() {
        return Arrays.copyOf(board, board.length);
    }
    
    /**
     * Gets the current player (HUMAN or COMPUTER).
     * @return HUMAN or COMPUTER
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }
    
    /**
     * Gets the game status.
     * @return GAME_NOT_STARTED, IN_PROGRESS, HUMAN_WINS, COMPUTER_WINS, or DRAW
     */
    public int getGameStatus() {
        return gameStatus;
    }
    
    /**
     * Checks if a position is empty (valid for a move).
     * @param position The board position (0-8)
     * @return true if the position is empty, false otherwise
     */
    public boolean isPositionEmpty(int position) {
        if (position < 0 || position > 8) {
            return false;
        }
        return board[position] == EMPTY;
    }
    
    /**
     * Gets a human-readable representation of the board for display.
     * @return String representation of the board
     */
    public String getBoardDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            switch (board[i]) {
                case EMPTY:
                    sb.append(" ");
                    break;
                case HUMAN:
                    sb.append("X");
                    break;
                case COMPUTER:
                    sb.append("O");
                    break;
            }
            
            // Add separator between columns
            if (i == 2 || i == 5) {
                sb.append("\n-+-+-\n");
            } else if (i < 8) {
                sb.append("|");
            }
        }
        return sb.toString();
    }
    
    /**
     * Resets the game to initial state.
     */
    public void resetGame() {
        Arrays.fill(board, EMPTY);
        currentPlayer = HUMAN;
        gameStatus = GAME_NOT_STARTED;
    }
    
    /**
     * Selects a strong legal move for the computer player (O).
     * First takes an immediate win, then blocks an immediate X win, 
     * otherwise chooses a legal move.
     * @param rand Random number generator to use (for repeatable tests)
     * @return position of selected move (0-8) or -1 if no moves available
     */
    public int selectStrongMove(Random rand) {
        // Check if game is over or not computer's turn
        if (gameStatus != IN_PROGRESS || currentPlayer != COMPUTER) {
            return -1;
        }
        
        // First check for immediate win for O
        for (int i = 0; i < 9; i++) {
            if (board[i] == EMPTY) {
                // Test if placing O at position i would result in a win
                int originalValue = board[i];
                board[i] = COMPUTER;
                
                // Check if this move wins the game by manually checking winning conditions 
                boolean isWin = checkWinForPlayer(COMPUTER);
                
                board[i] = originalValue;  // Revert immediately
                
                if (isWin) {
                    return i;
                }
            }
        }
        
        // Then check for blocking immediate X win (if O cannot win)
        for (int i = 0; i < 9; i++) {
            if (board[i] == EMPTY) {
                // Test if placing X at position i would result in a win  
                int originalValue = board[i];
                board[i] = HUMAN;
                
                // Check if this move wins the game by manually checking winning conditions
                boolean isWin = checkWinForPlayer(HUMAN);
                
                board[i] = originalValue;  // Revert immediately
                
                if (isWin) {
                    return i;
                }
            }
        }
        
        // If no immediate win or block, select a random legal move
        return selectRandomMove(rand);
    }
    
    /**
     * Checks if the specified player has won by manually checking all winning conditions.
     * @param player The player to check for wins (HUMAN or COMPUTER)
     * @return true if the player has won, false otherwise
     */
    private boolean checkWinForPlayer(int player) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            int rowSum = board[i * 3] + board[i * 3 + 1] + board[i * 3 + 2];
            if (rowSum == 3 * player) {
                return true;
            }
        }
        
        // Check columns
        for (int i = 0; i < 3; i++) {
            int colSum = board[i] + board[i + 3] + board[i + 6];
            if (colSum == 3 * player) {
                return true;
            }
        }
        
        // Check diagonals
        int diag1Sum = board[0] + board[4] + board[8];
        int diag2Sum = board[2] + board[4] + board[6];
        
        if (diag1Sum == 3 * player || diag2Sum == 3 * player) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Selects a random legal move for the current player.
     * @param rand Random number generator to use (for repeatable tests)
     * @return position of selected move (0-8) or -1 if no moves available
     */
    public int selectRandomMove(Random rand) {
        // Check if game is over
        if (gameStatus != IN_PROGRESS) {
            return -1;
        }
        
        // Collect all empty positions
        int[] emptyPositions = new int[9];
        int count = 0;
        for (int i = 0; i < 9; i++) {
            if (board[i] == EMPTY) {
                emptyPositions[count++] = i;
            }
        }
        
        // If no moves available, return -1
        if (count == 0) {
            return -1;
        }
        
        // Select a random move from the empty positions
        int randomIndex = rand.nextInt(count);
        return emptyPositions[randomIndex];
    }
}