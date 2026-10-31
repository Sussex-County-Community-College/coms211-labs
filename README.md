# Tic-tac-toe — instructor development reference

COMS211 Software Engineering with AI, Sussex County Community College.

A small JavaFX game in which human X plays computer O. Random chooses a legal move; Stronger takes an immediate win, otherwise blocks an immediate loss, otherwise chooses a legal move. Each new game and accepted move is stored through JDBC in MySQL. Saved Games opens a menu of recorded games. Selection starts an empty replay board; each Right Arrow reveals one recorded move. Replay is read-only.

This branch contains the reference produced by an instructor-operated Qwen experiment, not an empty student starter or a finished graded lab. See [recorded prompts](PROMPTS.md), [requirements](requirements.md), [development outline](LAB.md), and [verification limits](MANUAL-CHECKS.md).

## Build and run

Use the instructor-prepared Linux VM with Maven, Java 21 or later, MySQL, and the prepared dependency cache. Maven compiles for Java release 21. This experiment used Eclipse's bundled Java 25 runtime; a Java 21 runtime walkthrough remains pending. JavaFX is a Maven dependency; no separate SDK is needed.

1. The instructor prepares a dedicated `coms211_tictactoe_lab` database and an application account with SELECT, INSERT and UPDATE on that schema. Apply [db/schema.sql](db/schema.sql) only to that database. The script creates tables without deleting existing data; database selection is supplied by the client. Never apply it to another schema.
2. Copy [db.properties.example](db.properties.example) to local `db.properties` and enter the supplied connection values yourself. Required keys are `db.url`, `db.username`, and `db.password`. Keep credentials out of agent tools, Git, screenshots, and evidence. The local file and inherited private backups are ignored. Ask for help with Java-properties escaping if a supplied value contains backslashes.
3. In the project root run `mvn -o clean test`. Expected current result: 77 tests, no failures/errors/skips. If prepared dependencies are missing, ask the instructor before changing tools or versions.
4. Run `mvn -o javafx:run`. Choose **Next opponent**, then **New Game**. The choice affects the next game; it does not change a game already in progress. Place X in an empty cell; O responds automatically.
5. Open **Saved Games**, select a record, and press **Right Arrow** once per move. At the end the recorded outcome is shown. **New Game** returns to live play.

In Eclipse, import **Maven > Existing Maven Projects** using the folder containing pom.xml. Use Maven goals `clean test` for checks and `javafx:run` for the application, with the project root as base directory.

## Architecture and checks

`GameModel` owns rules and move selection. `LiveGameService` builds a candidate from saved history and publishes it only after a successful save. `GameRepository` uses prepared SQL and a transaction for one move plus outcome. `Game`/`Move` are immutable data objects. `ReplayModel` loads metadata/history and steps without writes or opponent selection. `GameWindow` owns the JDBC connection and UI controls.

Tests exercise real rules/selectors/service and real JDBC with test-only H2. The backend round-trip test verifies exact saved moves, H2 file connection restart, every replay prefix, unchanged stored history, and actual stronger blocking. H2 mode does not establish MySQL/JDBC compatibility. Source review and backend tests do not verify mouse/keyboard interaction. See [MANUAL-CHECKS.md](MANUAL-CHECKS.md) before any student release.

## Recovery and known limits

Missing settings/connection at launch shows startup feedback; correct local setup and restart. A failed human save leaves the committed board unchanged. If O is pending after a failed save, **Retry Computer Move** retries that turn. A failed New Game leaves the previous game active. A broken connection may require fixing MySQL and restarting the application; this version does not reconnect automatically or resume an unfinished live game after restart. Incomplete saved games remain available for historical replay.

Database work runs on the JavaFX thread, so a slow/unavailable server can pause the window. History assumes valid application-generated move order. Listing has no explicit ID tiebreak for equal timestamps. Detailed student scaffolding, grading, desktop-agent validation, live MySQL play and visual/accessibility walkthroughs remain future work.

Original Java/SQL: MIT ([LICENSE-CODE](LICENSE-CODE)). Original instructional prose/prompts: CC BY-NC 4.0 ([LICENSE-CONTENT](LICENSE-CONTENT)). Dependencies retain their own licenses. No third-party instructional content was adopted.
