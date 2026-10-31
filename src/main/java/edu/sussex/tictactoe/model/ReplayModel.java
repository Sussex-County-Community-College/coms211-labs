package edu.sussex.tictactoe.model;

import edu.sussex.tictactoe.GameModel;
import edu.sussex.tictactoe.repository.GameRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Replay model for playing back stored games move by move.
 * This class loads a game's moves and allows stepping through them one at a time,
 * starting from an empty board state. It never modifies the original stored history
 * or writes to storage.
 */
public class ReplayModel {
    private final GameRepository repository;
    private List<Move> moves;
    private int currentMoveIndex;
    private GameModel model;
    private String originalOutcome;
    
    /**
     * Creates a new replay model initialized with an empty board and no loaded game.
     * @param repository The repository to load games from
     */
    public ReplayModel(GameRepository repository) {
        this.repository = repository;
        this.moves = null;
        this.currentMoveIndex = 0;
        this.model = new GameModel(); // Start with clean board
        this.originalOutcome = "IN_PROGRESS";
    }
    
    /**
     * Loads a game's moves for replay.
     * @param gameId The ID of the game to load
     * @throws SQLException if database operation fails or game doesn't exist
     */
    public void loadGame(long gameId) throws SQLException {
        // First, get the actual Game object with its outcome by finding it in listGames()
        List<edu.sussex.tictactoe.model.Game> allGames = repository.listGames();
        edu.sussex.tictactoe.model.Game foundGame = null;
        for (edu.sussex.tictactoe.model.Game game : allGames) {
            if (game.getId() == gameId) {
                foundGame = game;
                break;
            }
        }
        
        if (foundGame == null) {
            throw new SQLException("Game with ID " + gameId + " not found");
        }
        
        // Get the moves for this game
        this.moves = repository.getGameMoves(gameId);
        this.currentMoveIndex = 0;
        this.model = new GameModel(); // Reset board state
        
        // Set original outcome from the actual saved game, not default
        this.originalOutcome = foundGame.getOutcome();
    }
    
    /**
     * Steps forward one move in the replay.
     * @return true if a move was played, false if no more moves or game not loaded
     */
    public boolean step() {
        if (moves == null || currentMoveIndex >= moves.size()) {
            return false;
        }
        
        Move move = moves.get(currentMoveIndex);
        // Make the move on our local model
        int position = move.getPosition();
        if (model.makeMove(position)) {
            currentMoveIndex++;
            return true;
        }
        return false;
    }
    
    /**
     * Gets the current board state of the replay.
     * @return Array representing the board positions 
     */
    public int[] getBoard() {
        return model.getBoard();
    }
    
    /**
     * Gets the current game status in the replay.
     * @return Game status (GAME_NOT_STARTED, IN_PROGRESS, HUMAN_WINS, COMPUTER_WINS, or DRAW)
     */
    public int getGameStatus() {
        return model.getGameStatus();
    }
    
    /**
     * Gets the current player in the replay.
     * @return Player identifier (HUMAN or COMPUTER) 
     */
    public int getCurrentPlayer() {
        return model.getCurrentPlayer();
    }
    
    /**
     * Checks if there are more moves to step through.
     * @return true if there are remaining moves, false otherwise
     */
    public boolean hasMoreMoves() {
        return moves != null && currentMoveIndex < moves.size();
    }
    
    /**
     * Gets the original outcome of the game as stored in database.
     * The value is read from the saved Game metadata during loadGame.  
     * @return The originally recorded outcome
     */
    public String getOriginalOutcome() {
        return originalOutcome;
    }
    
    /**
     * Resets the replay to initial state with empty board.
     */
    public void reset() {
        this.moves = null;
        this.currentMoveIndex = 0;
        this.model = new GameModel(); // Reset to clean board
        this.originalOutcome = "IN_PROGRESS";
    }
}