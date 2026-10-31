package edu.sussex.tictactoe.repository;

import edu.sussex.tictactoe.model.Game;
import edu.sussex.tictactoe.model.Move;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository for saving games and moves with atomic updates.
 */
public class GameRepository {
    
    private final Connection connection;
    
    public GameRepository(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Starts a new game and returns the created game with its ID.
     * 
     * @param opponentMode The mode of the opponent ("random" or "stronger")
     * @param outcome The initial outcome of the game
     * @return The created Game object with assigned ID
     * @throws SQLException if database operation fails
     */
    public Game startGame(String opponentMode, String outcome) throws SQLException {
        // Validate inputs according to requirements
        validateOpponentMode(opponentMode);
        validateOutcome(outcome);
        
        String sql = "INSERT INTO games (opponent_mode, outcome) VALUES (?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, opponentMode);
            stmt.setString(2, outcome);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating game failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    return new Game(id, LocalDateTime.now(), opponentMode, outcome);
                } else {
                    throw new SQLException("Creating game failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Records a move for a game and updates the game outcome atomically.
     * 
     * @param move The move to record
     * @param outcome The updated outcome of the game (can be same as previous)
     * @throws SQLException if database operation fails or if validation fails
     */
    public void saveMove(Move move, String outcome) throws SQLException {
        // Validate inputs according to requirements  
        validatePlayer(move.getPlayer());
        validateMoveNumber(move.getMoveNumber());
        validatePosition(move.getPosition());
        validateOutcome(outcome);
        
        // Use a transaction to ensure atomicity
        Connection conn = this.connection;
        boolean originalAutoCommit = conn.getAutoCommit();
        
        try {
            conn.setAutoCommit(false);
            
            // First, check if the game exists and get its current outcome
            String selectGameSql = "SELECT id, outcome FROM games WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(selectGameSql)) {
                stmt.setLong(1, move.getGameId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Game with ID " + move.getGameId() + " does not exist.");
                    }
                }
            }
            
            // Insert the move
            String insertMoveSql = "INSERT INTO moves (game_id, move_number, player, position) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertMoveSql)) {
                stmt.setLong(1, move.getGameId());
                stmt.setInt(2, move.getMoveNumber());
                stmt.setInt(3, move.getPlayer());
                stmt.setInt(4, move.getPosition());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Recording move failed, no rows affected.");
                }
            }
            
            // Update the game outcome
            String updateGameSql = "UPDATE games SET outcome = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateGameSql)) {
                stmt.setString(1, outcome);
                stmt.setLong(2, move.getGameId());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Updating game outcome failed, no rows affected.");
                }
            }
            
            // Commit the transaction
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }
    
    /**
     * Lists all saved games with their identifying information.
     * 
     * @return A list of Game objects representing all saved games
     * @throws SQLException if database operation fails
     */
    public List<Game> listGames() throws SQLException {
        String sql = "SELECT id, start_time, opponent_mode, outcome FROM games ORDER BY start_time DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Game> games = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                String opponentMode = rs.getString("opponent_mode");
                String outcome = rs.getString("outcome");
                
                games.add(new Game(id, startTime, opponentMode, outcome));
            }
            
            return games;
        }
    }
    
    /**
     * Loads all moves for a specific game in move-number order.
     * 
     * @param gameId The ID of the game to load moves for
     * @return A list of Move objects representing the game's moves ordered by move number
     * @throws SQLException if database operation fails or if the game does not exist
     */
    public List<Move> getGameMoves(long gameId) throws SQLException {
        // First check that the game exists
        String selectGameSql = "SELECT id FROM games WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectGameSql)) {
            stmt.setLong(1, gameId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Game with ID " + gameId + " does not exist.");
                }
            }
        }
        
        String sql = "SELECT game_id, move_number, player, position FROM moves WHERE game_id = ? ORDER BY move_number";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, gameId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Move> moves = new ArrayList<>();
                while (rs.next()) {
                    long gameID = rs.getLong("game_id");
                    int moveNumber = rs.getInt("move_number");
                    int player = rs.getInt("player");
                    int position = rs.getInt("position");
                    
                    moves.add(new Move(gameID, moveNumber, player, position));
                }
                
                return moves;
            }
        }
    }
    
    private void validatePlayer(int player) throws SQLException {
        if (player != 1 && player != -1) {
            throw new SQLException("Invalid player: " + player + ". Players must be 1 (X) or -1 (O).");
        }
    }
    
    private void validateMoveNumber(int moveNumber) throws SQLException {
        if (moveNumber < 1 || moveNumber > 9) {
            throw new SQLException("Invalid move number: " + moveNumber + ". Move numbers must be between 1 and 9.");
        }
    }
    
    private void validatePosition(int position) throws SQLException {
        if (position < 0 || position > 8) {
            throw new SQLException("Invalid position: " + position + ". Positions must be between 0 and 8.");
        }
    }
    
    private void validateOutcome(String outcome) throws SQLException {
        // Check that outcome is one of the valid values
        if (!"IN_PROGRESS".equals(outcome) && 
            !"HUMAN_WINS".equals(outcome) && 
            !"COMPUTER_WINS".equals(outcome) && 
            !"DRAW".equals(outcome)) {
            throw new SQLException("Invalid outcome: " + outcome + ". Outcomes must be IN_PROGRESS, HUMAN_WINS, COMPUTER_WINS, or DRAW.");
        }
    }
    
    private void validateOpponentMode(String opponentMode) throws SQLException {
        if (!"random".equals(opponentMode) && !"stronger".equals(opponentMode)) {
            throw new SQLException("Invalid opponent mode: " + opponentMode + ". Modes must be random or stronger.");
        }
    }
}