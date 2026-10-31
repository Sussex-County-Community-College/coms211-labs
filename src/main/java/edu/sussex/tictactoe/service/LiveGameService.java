package edu.sussex.tictactoe.service;

import edu.sussex.tictactoe.GameModel;
import edu.sussex.tictactoe.model.Game;
import edu.sussex.tictactoe.model.Move;
import edu.sussex.tictactoe.repository.GameRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * Service that manages live games using the existing rules, random opponent, and repository.
 * Saves a new game, then each accepted human and computer move in order with correct outcome.
 * Never lets storage failure appear to be successful or proceed with computer play after failed human save.
 * Keeps service state consistent with committed moves.
 */
public class LiveGameService {
    private final GameRepository gameRepository;
    private final Random random;
    
    // Current game state
    private long currentGameId = -1;
    private GameModel gameModel;
    private String currentOpponentMode;  // Track the opponent mode for active game
    
    public LiveGameService(GameRepository gameRepository, Random random) {
        this.gameRepository = gameRepository;
        this.random = random;
    }
    
    /**
     * Starts a new game with the specified opponent mode.
     * @param opponentMode The opponent mode ("random" or "stronger")
     * @return The created Game object
     * @throws SQLException if database operation fails
     */
    public Game startGame(String opponentMode) throws SQLException {
        // Validate opponent mode according to requirements
        if (!"random".equals(opponentMode) && !"stronger".equals(opponentMode)) {
            throw new SQLException("Invalid opponent mode: " + opponentMode + 
                                 ". Modes must be random or stronger.");
        }
        
        // Start the game in the repository and get the created game with ID
        Game game = gameRepository.startGame(opponentMode, "IN_PROGRESS");
        currentGameId = game.getId();
        gameModel = new GameModel();
        currentOpponentMode = opponentMode;  // Store the mode for this game
        
        return game;
    }
    
    /**
     * Makes a human move at the specified position.
     * @param position The board position (0-8)
     * @return true if the move was valid and executed, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean makeHumanMove(int position) throws SQLException {
        // Check if we have an active game
        if (currentGameId == -1 || gameModel == null) {
            return false;
        }
        
        // Enforce that human can only play when it's the human's turn  
        if (gameModel.getCurrentPlayer() != GameModel.HUMAN) {
            return false;
        }
        
        // Validate the move in a new model instance to avoid changing state yet
        GameModel candidate = new GameModel();
        // Replay all committed moves into this new model 
        replayCommittedMoves(candidate);
        
        // Check if the move is valid on the candidate model (not affecting service state yet)
        if (!candidate.makeMove(position)) {
            return false;
        }
        
        // If we get here, the move is valid. Now save it
        int moveNumber = calculateNextMoveNumber();
        int currentPlayer = GameModel.HUMAN;  // Human is playing X (1) 
        
        // Create the move object for storage
        Move move = new Move(currentGameId, moveNumber, currentPlayer, position);
        
        // Save the human move to database - if this fails, don't update service state 
        try {
            String outcome = getOutcomeForSave(candidate);  // Use candidate's status
            gameRepository.saveMove(move, outcome);
            
            // Only after successful save, assign the candidate model to our visible one
            gameModel = candidate;
            return true;
        } catch (SQLException e) {
            // If save fails, old state remains intact - no change to service state
            throw e;
        }
    }
    
    /**
     * Makes a computer move.
     * @return true if the move was valid and executed, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean makeComputerMove() throws SQLException {
        // Check if we have an active game 
        if (currentGameId == -1 || gameModel == null) {
            return false;
        }
        
        // Enforce that computer can only play when it's the computer's turn  
        if (gameModel.getCurrentPlayer() != GameModel.COMPUTER) {
            return false;
        }
        
        // Validate we are in a valid state for making move
        if (gameModel.getGameStatus() != GameModel.IN_PROGRESS) {
            return false;
        }
        
        // Create a candidate model to test the computer move 
        GameModel candidate = new GameModel();
        replayCommittedMoves(candidate);
        
        // Select and make the computer's move on candidate (without affecting service state yet)
        int position;
        if ("stronger".equals(currentOpponentMode)) {
            position = candidate.selectStrongMove(random);
        } else {
            position = candidate.selectRandomMove(random);
        }
        
        if (position == -1) {
            return false;  // No valid moves available
        }
        
        if (!candidate.makeMove(position)) {
            return false;
        }
        
        // If we get here, the move is valid. Now save it  
        int moveNumber = calculateNextMoveNumber();
        int currentPlayer = GameModel.COMPUTER;  // Computer is playing O (-1)
        
        // Create the move object for storage
        Move move = new Move(currentGameId, moveNumber, currentPlayer, position);
        
        // Save the computer move to database - if this fails, don't update service state 
        try {
            String outcome = getOutcomeForSave(candidate);  // Use candidate's status  
            gameRepository.saveMove(move, outcome);
            
            // Only after successful save, assign the candidate model to our visible one
            gameModel = candidate;
            return true;
        } catch (SQLException e) {
            // If save fails, old state remains intact - no change to service state 
            throw e;
        }
    }
    
    /**
     * Gets the current board state.
     * @return Array of 9 integers representing the board positions
     */
    public int[] getBoard() {
        if (gameModel == null) {
            return new int[9]; // Return empty board if no game is active
        }
        return gameModel.getBoard();
    }
    
    /**
     * Gets the current game status.
     * @return GAME_NOT_STARTED, IN_PROGRESS, HUMAN_WINS, COMPUTER_WINS, or DRAW
     */
    public int getGameStatus() {
        if (gameModel == null) {
            return GameModel.GAME_NOT_STARTED;
        }
        return gameModel.getGameStatus();
    }
    
    /**
     * Gets the current player.
     * @return HUMAN or COMPUTER
     */
    public int getCurrentPlayer() {
        if (gameModel == null) {
            return GameModel.HUMAN;  // Default to human when no game is active
        }
        return gameModel.getCurrentPlayer();
    }
    
    /**
     * Gets the current game ID.
     * @return The ID of the currently active game, or -1 if none
     */
    public long getCurrentGameId() {
        return currentGameId;
    }
    
    /**
     * Gets the opponent mode for the current game.
     * @return "random" or "stronger", or null if no game is active
     */
    public String getCurrentOpponentMode() {
        return currentOpponentMode;
    }
    
    /**
     * Checks if a position is empty (valid for a move).
     * @param position The board position (0-8)
     * @return true if the position is empty, false otherwise
     */
    public boolean isPositionEmpty(int position) {
        if (gameModel == null) {
            return false;
        }
        return gameModel.isPositionEmpty(position);
    }
    
    /**
     * Calculates the next move number based on committed board state.
     * @return The next move number to be used
     */
    private int calculateNextMoveNumber() {
        try {
            List<Move> moves = gameRepository.getGameMoves(currentGameId);
            return moves.size() + 1;
        } catch (SQLException e) {
            // If we can't access the repository, fall back to existing state for safety
            return gameModel != null ? calculateNextMoveNumberFromModel(gameModel) : 1;
        }
    }
    
    /**
     * Calculates next move number from model directly (fallback method)
     */
    private int calculateNextMoveNumberFromModel(GameModel model) {
        // Count how many non-empty positions there are (move numbers start at 1)
        int moveCount = 0;
        for (int i = 0; i < 9; i++) {
            if (model.getBoard()[i] != GameModel.EMPTY) {
                moveCount++;
            }
        }
        return moveCount + 1;
    }
    
    /**
     * Gets the outcome string to save based on current game state.
     * @param model The model in question
     * @return appropriate outcome string
     */
    private String getOutcomeForSave(GameModel model) {
        switch (model.getGameStatus()) {
            case GameModel.HUMAN_WINS:
                return "HUMAN_WINS";
            case GameModel.COMPUTER_WINS:
                return "COMPUTER_WINS";
            case GameModel.DRAW:
                return "DRAW";
            default:
                return "IN_PROGRESS";
        }
    }
    
    /**
     * Replays all committed moves into the given model.
     */
    private void replayCommittedMoves(GameModel target) throws SQLException {
        if (currentGameId == -1 || gameRepository == null) {
            // Start with empty board
            return;
        }
        
        List<Move> moves = gameRepository.getGameMoves(currentGameId);
        for (Move move : moves) {
            // Apply each move to the target model 
            target.makeMove(move.getPosition());
        }
    }
    
    /**
     * Resets the current game state without saving to database.
     */
    public void resetGame() {
        currentGameId = -1;
        gameModel = null;
        currentOpponentMode = null;  // Reset opponent mode as well
    }
}