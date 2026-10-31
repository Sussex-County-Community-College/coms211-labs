# Tic-tac-toe development boundaries

The student is the engineer and approves each implementation increment.
Goal -> inspect -> plan -> act -> observe -> revise -> repeat -> report.
Read requirements.md and available source/tests before editing. Implement only the current requested step; later requirements are context, not authorization.
Use Java 21, Maven, JavaFX, JDBC/MySQL, JUnit 5, and test-only H2. Keep classes small enough for beginning Java students to inspect and explain. Do not add frameworks, cloud services, or orchestration.
Do not read credentials, change accounts, reset real databases, install software, commit, or push. Do not access .git or local backup files. SQL values must be parameterized and JDBC resources closed.
Preserve existing behavior and tests. Qwen writes application code and new tests; the supervising agent reviews and independently runs checks, reporting assistance explicitly.
Distinguish source review, automated checks, real MySQL checks, and actual UI interaction. Never claim a check you did not perform. Stop after each requested increment.

## Testing Rules

Test requested behavior by calling real application code and checking expected results and relevant errors. Tests of pretend dependencies alone or constant assertions do not establish application behavior. Use pretend storage for service unit tests, recording requests when relevant; assert after calling the real method. Use test-only H2 for JDBC integration tests, calling the actual JDBC repository against a fresh database. Test important boundaries and forbidden side effects without exhaustively testing every permutation. Preserve existing tests and assertions; never weaken checks to pass. Use JUnit 5 and no additional test frameworks. Run mvn -o test after each implementation step. Report observed test counts/results, any failure, and checks still needed. Automated backend tests do not verify JavaFX interaction or real MySQL persistence. Keep tests repeatable; do not rely on random outcomes.