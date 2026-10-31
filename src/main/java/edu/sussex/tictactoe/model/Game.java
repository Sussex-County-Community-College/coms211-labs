package edu.sussex.tictactoe.model;

import java.time.LocalDateTime;

/**
 * Immutable representation of a tic-tac-toe game.
 */
public class Game {
    private final long id;
    private final LocalDateTime startTime;
    private final String opponentMode;
    private final String outcome;
    
    public Game(long id, LocalDateTime startTime, String opponentMode, String outcome) {
        this.id = id;
        this.startTime = startTime;
        this.opponentMode = opponentMode;
        this.outcome = outcome;
    }
    
    public long getId() {
        return id;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public String getOpponentMode() {
        return opponentMode;
    }
    
    public String getOutcome() {
        return outcome;
    }
}