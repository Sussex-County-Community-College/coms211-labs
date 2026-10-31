package edu.sussex.tictactoe.model;

/**
 * Immutable representation of a tic-tac-toe move.
 */
public class Move {
    private final long gameId;
    private final int moveNumber;
    private final int player;
    private final int position;
    
    public Move(long gameId, int moveNumber, int player, int position) {
        this.gameId = gameId;
        this.moveNumber = moveNumber;
        this.player = player;
        this.position = position;
    }
    
    public long getGameId() {
        return gameId;
    }
    
    public int getMoveNumber() {
        return moveNumber;
    }
    
    public int getPlayer() {
        return player;
    }
    
    public int getPosition() {
        return position;
    }
}