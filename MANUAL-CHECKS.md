# Tic-tac-toe verification record

Instructor development record, October 31, 2026. Distinguish source review, automated evidence, and actually observed interaction.

## Verified

- Final `mvn -o clean test`: 77 tests, zero failures/errors/skips, independently executed with Eclipse's bundled Java 25 and Java release 21 compilation.
- Qwen-authored backend integration checks use the actual service/repository, actual schema and fresh H2 files. They verify full random-game move payloads/outcome, connection close/reopen, exact replay prefixes, end behavior/read-only history, and actual stronger fallback/blocking with saved mode.
- The actual schema file executed twice successfully against dedicated local MySQL 8.4.11 `coms211_tictactoe_lab`. Both tables were empty afterward. Operations were scoped to that schema.
- Final source review covers UI wiring, successful-only mode transitions, O retry, resource ownership, replay read-only calls, parameterized SQL and per-move transactions. This is agent review, not instructor acceptance or observed GUI behavior.

## Manual UI and live MySQL/JDBC checks — all pending

1. Launch with prepared Java/Maven and private settings. Check all controls fit; wrapped messages, keyboard traversal and accessible position/state names are usable.
2. Start a game, place X, and confirm exactly one O response. Occupied/terminal input must change nothing. Verify X win, O win and draw. New Game restores an empty board.
3. Inspect actual MySQL game/move rows after play: IDs, mode, outcome, player/position, sequential move numbers and isolation. Restart the application and confirm historical data remains.
4. Open Saved Games after playing a new game without restarting. Select a record by ID/time/outcome. Confirm empty initial board, exactly one move per Right Arrow including after menu selection, immediate recorded end outcome, unchanged extra keys, switching-game reset and no stored-data changes. Include empty and incomplete history.
5. Choose Random and Stronger for separate new games; check stored modes. Changing the next-opponent choice midgame must not change the active strategy. Check immediate win priority, blocking and legal fallback.
6. Test missing/invalid settings and a temporary save failure. No false success, ghost moves or lost committed X. O retry remains available when appropriate. Failed new-game/history transitions preserve the previous state. Recover the server; restart if connection is broken, documenting that limitation.
7. Confirm local secrets/backups are ignored and operational directions work for a fresh reader. Record sanitized observations rather than credentials.

A bounded startup-process sanity check may be recorded separately; it does not check visuals or gameplay. Java 21 runtime, desktop-agent/student workflow, unchanged clean repeats, full student lab parts/grading, Canvas delivery and student release have not been verified or performed. No learning gains or model ranking are claimed.
