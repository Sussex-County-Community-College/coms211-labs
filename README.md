# Task Tracker starter

A small OpenJFX application for the COMS211 supervised feature lab. The starter adds and lists tasks; students implement mark complete. See requirements.md and AGENTS.md.

## Prerequisites

Use the instructor-prepared Linux VM: JDK 21 or later, Maven 3.9, MySQL, Workbench, Eclipse, Git/GitHub Desktop, and Codex configured with Qwen. OpenJFX is a Maven dependency; no separate JavaFX SDK installation is required. The instructor prepares the Maven dependency cache in the appliance; network access is needed only if dependencies are missing or must be refreshed. Report missing tools to the instructor rather than installing replacements.

## Setup

1. Open your private `coms211-labs-private` repository in GitHub Desktop. Save and commit unfinished work on its current branch, Fetch origin, then select **Current Branch > task-tracker**. Pull updates before starting if offered; ask for help if the branch is missing or a conflict occurs. Do not create an empty lab branch. Open this same folder in Codex. In Eclipse use **File > Import > Maven > Existing Maven Projects** and select the folder containing this `pom.xml`; refresh if already imported. Your clone may live under `~/GitHub` or `~/eclipse-workspace`. Use the existing clone rather than making a second repository.
2. The instructor prepares the practice database using db/schema.sql and db/reset.sql and provides a dedicated account restricted to SELECT, INSERT, UPDATE, DELETE on coms211_task_lab.*. Check the supplied connection in Workbench. Ask for help if it fails. The application never needs administrator privileges.
3. Check that the instructor-provided local db.properties file is present. If missing, copy db.properties.example to db.properties and enter the supplied local account values yourself. The file is ignored by Git; do not send it to Codex or include it in screenshots. Java properties use backslash escapes; ask the instructor for help if supplied credentials contain backslashes.
4. In a terminal opened in the project root, run `java -version`, `mvn -version`, then `mvn clean test`. Confirm Maven uses JDK 21 or later. The baseline has four test methods.
5. Run `mvn javafx:run`. Add a task, select Refresh, close the window, then run it again. Confirm the task persists. The window should show each task's ID, status, and title.
6. In Workbench, run `SELECT id, title, completed FROM coms211_task_lab.tasks ORDER BY id;` and compare with the window.
7. Review changed files in GitHub Desktop. Verify db.properties is absent and Current Branch is task-tracker. Record the supplied baseline commit; commit a new baseline only if you made instructor-approved tracked changes. Keep feature commits on task-tracker and push them to your private origin before submitting.

In Eclipse, right-click the project > Run As > Maven build… with Goals `clean test`. Expect BUILD SUCCESS and four baseline test methods. Create a second Maven run configuration with Goals `javafx:run`; its base directory must be the project root. If settings are unavailable, check that db.properties is beside pom.xml with nonblank url, user, and password values, then restart. Eclipse can use its bundled Maven. For terminal use, open a new terminal after the instructor configures Maven and JAVA_HOME.

## Architecture

TaskWindow is the JavaFX interface. TaskService validates input. TaskRepository defines the storage operations; JdbcTaskRepository implements them with JDBC. Task represents one database row. The starter performs short local database calls on the JavaFX thread to keep the example small; slow/unavailable connections can temporarily freeze the interface. Record this limitation during validation; responsive background work is outside this feature.

## Repeatable checks

Run `mvn clean test` for service unit tests and `mvn javafx:run` for the application. Unit tests use an in-memory stand-in; they do not establish database or UI correctness. Use the lab's evidence table for independent MySQL and interface checks.

## Recovery

- Build failure: check the project folder, JDK shown by Maven, and first error. Preserve a sanitized error summary. Ask the instructor if dependencies cannot download.
- Connection failure: check MySQL in Workbench, local settings, and the selected lab database. Do not post credentials. Use Refresh after recovery or restart after editing settings.
- Unwanted change: inspect GitHub Desktop's diff. Copy wanted work aside, then discard only the selected unwanted changes. Do not discard the entire repository or rewrite history. Git recovery does not undo database changes.
- Practice-data reset: back up wanted rows first. Ask the instructor to run db/reset.sql; it deletes only records in coms211_task_lab.tasks and inserts two examples. Never run it against another database.

## Licensing

Original Java and SQL: MIT, see LICENSE-CODE. Original instructional prose: CC BY-NC 4.0, see LICENSE-CONTENT. Third-party dependencies retain their own licenses. No third-party instructional text or source code is copied into this starter.
