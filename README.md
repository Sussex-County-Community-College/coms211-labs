# Module 2: Exploring Qwen in LM Studio and Codex

COMS211 Software Engineering with AI — Sussex County Community College.

Use Canvas for the full lab and rubric. Work in your existing private
`coms211-private` repository on branch `ai-intro`. Save and commit Module 1 work
before switching. This branch starts from a fresh copy of the familiar program;
its heading is intentionally back to `COMS211 greeting practice`.

## Start and establish a baseline

Use the supplied Linux VM, Java 21 or later, Maven and prepared dependency cache.
In the private project root beside pom.xml run:

```bash
pwd
mvn -o clean package
java -jar target/coms211-greeting.jar
java -jar target/coms211-greeting.jar Maya
java -jar target/coms211-greeting.jar ""
java -jar target/coms211-greeting.jar "  Maya  "
```

Expect three passing baseline tests. The no-argument run prints the heading and
`Hello, student!`. Blank and padded arguments are not yet validated or trimmed.
Record the actual baseline output before authorizing a change.

## Explore, inspect, plan, act, verify

1. In Windows LM Studio, use `exercises/lm-studio.md` for short chat experiments.
   Work with the prepared Qwen model/settings; no download or tuning is assigned.
2. Back in Ubuntu, open this same private folder in Codex. Ask the instructor to
   confirm the prepared Qwen provider/model selection and correlate a request
   with activity in the host's LM Studio server. A successful LM Studio chat or
   an agent's self-description does not by itself verify this connection.
3. Send `prompts/01-inspect.txt`. Locate the reported files yourself. The first
   stage permits no edits. Check GitHub Desktop for unexpected changes.
4. Read `requirements.md`, `DESIGN.md` and `TEST-PLAN.md`. Copy
   `EVIDENCE-TEMPLATE.md` to `notes/ai-report.md`; write expected results yourself.
5. Send `prompts/02-plan.txt`. Review scope, behavior and planned checks before
   approving implementation. Clarify a substantive mismatch with the agent.
6. When ready, send `prompts/03-act.txt`. Inspect the complete diff, including
   test assertions. Run the build yourself and check the console cases below.
7. Accept, revise or reject using evidence. If revision is needed, describe the
   specific failed criterion and authorize one correction within the same scope.
   Ask for help if work stalls; keep the failed attempt and sanitized error.
8. Write your own report. Add "After review" notes to DESIGN.md and TEST-PLAN.md
   describing actual structure/checks and remaining limitations. Inspect all
   changed files, commit accepted work and push to your private ai-intro branch.

## Independent console checks after implementation

Rebuild with `mvn -o clean package`, then run:

```bash
java -jar target/coms211-greeting.jar Maya
java -jar target/coms211-greeting.jar "Grace Hopper"
java -jar target/coms211-greeting.jar ""
java -jar target/coms211-greeting.jar "   "
java -jar target/coms211-greeting.jar "  Maya  "
java -jar target/coms211-greeting.jar
```

Every run keeps the heading. Ordinary names keep their greeting. Empty and
spaces-only names show `Please enter a name.` without a greeting or stack trace.
The padded name becomes `Hello, Maya!`. No argument still greets `student`.
Expected test count after the feature depends on the tests added: record the
actual total and inspect whether their assertions cover the requirements.

## Recovery and boundaries

Missing branch: ask the instructor to transfer it to your private copy; Fetch
origin alone does not copy instructor-repository branches. Connection failure:
confirm the host model/server and prepared VM provider with the instructor; do
not change networking, credentials or providers yourself. Cache/tool failure:
record the first useful error without installing replacements. Wrong source
change: inspect and preserve work before deciding what to restore. Do not
weaken supplied tests or discard the whole working tree.

Keep Qwen chats short and tasks small. Share only fictional practice content.
The agent may assist with the authorized code/tests, but your evidence,
explanations and acceptance decision must be your own.

Original code: MIT (`LICENSE-CODE`). Original prose: CC BY-NC 4.0
(`LICENSE-CONTENT`). Third-party tools/model weights retain their own licenses.
