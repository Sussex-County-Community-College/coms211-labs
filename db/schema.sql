-- Tic-tac-toe game schema for coms211_tictactoe_lab
-- This script should be applied only to the coms211_tictactoe_lab database

-- Games table
CREATE TABLE IF NOT EXISTS games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    opponent_mode VARCHAR(50) NOT NULL,
    outcome VARCHAR(20) NOT NULL
);

-- Moves table  
CREATE TABLE IF NOT EXISTS moves (
    game_id BIGINT NOT NULL,
    move_number INT NOT NULL,
    player INT NOT NULL,
    position INT NOT NULL,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    PRIMARY KEY (game_id, move_number),
    CONSTRAINT chk_position CHECK (position BETWEEN 0 AND 8),
    CONSTRAINT chk_move_number CHECK (move_number > 0),
    CONSTRAINT uk_game_position UNIQUE (game_id, position)
);