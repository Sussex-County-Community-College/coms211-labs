# Tic-tac-toe lab — development outline

Instructor development draft; not a finished student assignment or grading instrument.

The reference application and [actual assisted prompt trail](PROMPTS.md) now exist. Students will eventually start without application code, specify acceptance criteria, supervise bounded changes, inspect source/tests and explain acceptance decisions. Students may delegate implementation, but not responsibility.

Candidate parts: requirements and Maven scaffolding; rules and legal test fixtures; random play and UI checks; schema/JDBC transactions; service failures and committed state; read-only historical replay; stronger strategy and maintenance; integrated verification and documentation. Exact parts, worked examples, recovery exercises, submission evidence and grading still need instructor development.

The recorded trial supplies real examples of plausible passing work that was incorrect or incomplete. Test names and model reports did not reliably describe actual assertions. Some tests confused turns, board positions and random indices. Those examples belong in instructor-only evidence until deliberately adapted for students.

Before release, simplify the prompt path and test scaffolding, run a fresh unchanged trial and desktop/student walkthrough, perform the manual UI/live MySQL checks, and define achievable evidence and grading. No Canvas delivery or student release is implied.
