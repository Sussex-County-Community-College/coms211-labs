# Tic-tac-toe requirements

This lab grows from an empty codebase through bounded, reviewed increments. Students may delegate implementation, but not responsibility.

- A human X plays first against computer O on a 3-by-3 board. Only legal moves change state. Detect either winner or a draw; reject moves after game over.
- Provide a JavaFX interface with board, status, New Game, and opponent selection. Initially use a random legal opponent; later add a stronger mode that wins immediately, otherwise blocks an immediate loss, otherwise chooses a legal move.
- Use Java 21, Maven, JavaFX, MySQL/JDBC, JUnit 5, and test-only H2. No extra application framework or deployment infrastructure.
- Persist each game and every accepted move in a dedicated coms211_tictactoe_lab database. Identify games, record start time, opponent mode, outcome, and ordered player/position moves. Preserve isolation between games. Report storage errors honestly and keep visible state consistent with saved history.
- Offer a menu of historical games. Selecting one shows an empty board; each Right Arrow reveals one stored move. Stop at the end and show outcome. Selecting another history starts over. Replay is read-only and never invokes an opponent. Allow returning to live play.
- Build and tests must be repeatable. Tests call actual application code and check meaningful results, important boundaries, and relevant failures. UI and real MySQL checks require separate evidence.

Success is a small working application plus an inspectable prompt sequence and recorded review evidence. Full student lab parts and grading are future work; this experiment does not establish student learning or desktop-agent compatibility.

## Stored representation

Use player 1 for X and -1 for O; positions are row-major 0–8 (0 1 2 / 3 4 5 / 6 7 8). Move numbers begin at 1. Stored opponent modes are `random` and `stronger`. Outcomes are `IN_PROGRESS`, `HUMAN_WINS`, `COMPUTER_WINS`, and `DRAW`. A fresh model's GAME_NOT_STARTED state maps to IN_PROGRESS when a game record is created. The service/model enforce legal alternating play; the repository validates stored field values and performs atomic writes. The application owns and closes its database connection.
