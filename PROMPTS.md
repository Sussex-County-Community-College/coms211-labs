# Recorded tic-tac-toe prompt sequence

Instructor development record, October 31, 2026. This is the actual assisted path to the reference application, including unsuccessful requests and corrections. It is not yet a clean student-ready recipe. Use the [role prompt](prompts/system.txt) and send one task at a time, inspecting source and assertions and independently running checks before acceptance.

The development started with documentation only: no pom.xml, Java, tests, SQL, or supplied feature solution. The first implementation task created Maven scaffolding. The original empty-code seed and all intermediate submissions remain in the curriculum repository at `benchmarks/model-bench/evidence/runs/run-tictactoe-20261031/`. This branch now contains the completed reference; do not use its final source as a fresh experimental seed.

The progression was setup/testing rules → scaffold → rules → random opponent → UI → schema/write/read storage → service/recovery → saved UI → replay backend/UI → stronger selector/per-game choice → backend integration. The integration request was reduced to a two-move prefix, then extended to a completed game, connection restart/replay, and stronger behavior after larger requests produced invalid tests.

Checkpoint files record context reconstruction, not new feature authorization. Full parent conversations were retained. API tools changed during the experiment (bounded deletion/replacement, selected replacement occurrence, ranged reads, plain-text read/test output, failure/context/time guards); consult the retained per-stage runner and requests. These results do not isolate prompt wording from context/tooling changes or verify the desktop-agent workflow.

## Exact submitted prompts, in execution order

| Order | Task or correction | Context reconstruction |
| --- | --- | --- |
| 1 | [00-setup](prompts/00-setup.txt) |  |
| 2 | [01-scaffold](prompts/01-scaffold.txt) |  |
| 3 | [01-scaffold-review](prompts/01-scaffold-review.txt) |  |
| 4 | [02-rules](prompts/02-rules.txt) |  |
| 5 | [02-rules-review](prompts/02-rules-review.txt) | [context checkpoint](prompts/checkpoints/02-rules-review.txt) |
| 6 | [02-rules-fixtures](prompts/02-rules-fixtures.txt) |  |
| 7 | [03-random](prompts/03-random.txt) | [context checkpoint](prompts/checkpoints/03-random.txt) |
| 8 | [03-random-review](prompts/03-random-review.txt) | [context checkpoint](prompts/checkpoints/03-random-review.txt) |
| 9 | [03-random-assertion](prompts/03-random-assertion.txt) |  |
| 10 | [04-window](prompts/04-window.txt) |  |
| 11 | [04-window-review](prompts/04-window-review.txt) |  |
| 12 | [05a-schema](prompts/05a-schema.txt) | [context checkpoint](prompts/checkpoints/05a-schema.txt) |
| 13 | [05a-schema-review](prompts/05a-schema-review.txt) |  |
| 14 | [05b-save](prompts/05b-save.txt) | [context checkpoint](prompts/checkpoints/05b-save.txt) |
| 15 | [05b-save-review](prompts/05b-save-review.txt) |  |
| 16 | [05b-save-artifact](prompts/05b-save-artifact.txt) |  |
| 17 | [05c-load](prompts/05c-load.txt) | [context checkpoint](prompts/checkpoints/05c-load.txt) |
| 18 | [06a-service](prompts/06a-service.txt) | [context checkpoint](prompts/checkpoints/06a-service.txt) |
| 19 | [06a-service-review](prompts/06a-service-review.txt) |  |
| 20 | [06a-service-state](prompts/06a-service-state.txt) | [context checkpoint](prompts/checkpoints/06a-service-state.txt) |
| 21 | [06a-service-fixtures](prompts/06a-service-fixtures.txt) | [context checkpoint](prompts/checkpoints/06a-service-fixtures.txt) |
| 22 | [06b-save-window](prompts/06b-save-window.txt) |  |
| 23 | [06b-save-window-review](prompts/06b-save-window-review.txt) | [context checkpoint](prompts/checkpoints/06b-save-window-review.txt) |
| 24 | [06b-save-window-state](prompts/06b-save-window-state.txt) |  |
| 25 | [07a-replay-model](prompts/07a-replay-model.txt) | [context checkpoint](prompts/checkpoints/07a-replay-model.txt) |
| 26 | [07a-replay-model-review](prompts/07a-replay-model-review.txt) |  |
| 27 | [07a-replay-model-outcome](prompts/07a-replay-model-outcome.txt) | [context checkpoint](prompts/checkpoints/07a-replay-model-outcome.txt) |
| 28 | [07b-replay-window](prompts/07b-replay-window.txt) | [context checkpoint](prompts/checkpoints/07b-replay-window.txt) |
| 29 | [07b-replay-window-review](prompts/07b-replay-window-review.txt) |  |
| 30 | [07b-replay-window-recover](prompts/07b-replay-window-recover.txt) | [context checkpoint](prompts/checkpoints/07b-replay-window-recover.txt) |
| 31 | [07b-replay-window-wiring](prompts/07b-replay-window-wiring.txt) | [context checkpoint](prompts/checkpoints/07b-replay-window-wiring.txt) |
| 32 | [07b-replay-window-duplicates](prompts/07b-replay-window-duplicates.txt) | [context checkpoint](prompts/checkpoints/07b-replay-window-duplicates.txt) |
| 33 | [07b-replay-window-menu](prompts/07b-replay-window-menu.txt) |  |
| 34 | [07b-replay-window-menu-small](prompts/07b-replay-window-menu-small.txt) | [context checkpoint](prompts/checkpoints/07b-replay-window-menu-small.txt) |
| 35 | [07b-replay-window-end](prompts/07b-replay-window-end.txt) | [context checkpoint](prompts/checkpoints/07b-replay-window-end.txt) |
| 36 | [08a-smarter](prompts/08a-smarter.txt) | [context checkpoint](prompts/checkpoints/08a-smarter.txt) |
| 37 | [08a-smarter-review](prompts/08a-smarter-review.txt) |  |
| 38 | [08a-smarter-fixtures](prompts/08a-smarter-fixtures.txt) | [context checkpoint](prompts/checkpoints/08a-smarter-fixtures.txt) |
| 39 | [08a-smarter-fallback](prompts/08a-smarter-fallback.txt) |  |
| 40 | [08b-mode-window](prompts/08b-mode-window.txt) | [context checkpoint](prompts/checkpoints/08b-mode-window.txt) |
| 41 | [08b-mode-window-review](prompts/08b-mode-window-review.txt) | [context checkpoint](prompts/checkpoints/08b-mode-window-review.txt) |
| 42 | [09-integration](prompts/09-integration.txt) | [context checkpoint](prompts/checkpoints/09-integration.txt) |
| 43 | [09a-persistence](prompts/09a-persistence.txt) | [context checkpoint](prompts/checkpoints/09a-persistence.txt) |
| 44 | [09b-prefix](prompts/09b-prefix.txt) | [context checkpoint](prompts/checkpoints/09b-prefix.txt) |
| 45 | [09c-finish-game](prompts/09c-finish-game.txt) |  |
| 46 | [09d-restart-replay](prompts/09d-restart-replay.txt) | [context checkpoint](prompts/checkpoints/09d-restart-replay.txt) |
| 47 | [09e-stronger-database](prompts/09e-stronger-database.txt) |  |
| 48 | [10-comments](prompts/10-comments.txt) | [context checkpoint](prompts/checkpoints/10-comments.txt) |

## Responsibility and limits

Qwen wrote every implementation and executable test statement. The supervising agent wrote requirements/prompts, operated the bounded API tools, reviewed code/assertions, independently ran checks, and wrote handoff documentation. It supplied explicit move examples and API/design guidance in the recorded corrective prompts. It corrected source comments after a failed comment-only Qwen pass; normalized Java tokens excluding comments/whitespace were checked against the accepted implementation. No evaluator-written test/source patches, hidden fault injection, or replacement feature solutions were used.

Passing tests are evidence, not proof. Several passing intermediate submissions were rejected after source/assertion review. Final clean verification is 77 tests with no failures/errors/skips, including actual service/JDBC/H2 file persistence, connection reopen, stepwise replay, and stronger-mode blocking. Real MySQL table setup was checked separately. Actual GUI interaction, live MySQL/JDBC gameplay and restart, Java 21 runtime, fresh desktop/student walkthrough, and unchanged clean-repeat reliability remain pending. Detailed student parts, worked examples, recovery exercises and grading are future work.

Original prompts/prose: CC BY-NC 4.0 under LICENSE-CONTENT. Code/tests: MIT under LICENSE-CODE. Dependencies retain their own licenses.
