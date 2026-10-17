# Lab: Supervise a JavaFX feature with an AI coding agent

**COMS211 Software Engineering with AI — Sussex County Community College**

**Value:** 10 points. **Time estimate:** approximately four hours of lab work across Modules 9 and 11; allow additional time for corrections.

## Purpose and preparation

You are the software engineer supervising an AI coding agent. Extend a small application, evaluate the proposed changes, and decide whether the evidence supports accepting them. Students may delegate implementation, but not responsibility.

This lab supports LO1 (controlled development workflow), LO3 (integrated design), LO4 (evaluation of generated work), and LO5 (verification). It practices a supplied specification; you are not being assessed on writing new project requirements. This supplied practice lab supports the individual project already underway. It is not a separately graded semester-project milestone. Complete the implementation checkpoint in **Module 9** and the final verification and submission in **Module 11**. Module 10 provides debugging and regression practices you can use between these stages. This is one assignment worth 10 points total, linked from both modules.

Before starting, complete introductory Git, Java testing, agent supervision, and interface/database lessons. You should be able to inspect a diff (the changes between file versions), run a test suite, and read a simple SQL query. Use the instructor's configured Linux VM, Eclipse, GitHub Desktop, Codex with Qwen, MySQL, and Workbench. Do not install or configure these tools during the lab. JavaFX dependencies are supplied through Maven, the build tool.

Use your instructor-provided **private copy of `coms211-labs-private`** inside the Linux VM. Open the same working folder in Eclipse, Codex, and GitHub Desktop. Your folder may be under `~/GitHub` or `~/eclipse-workspace`; use the folder that contains `.git` and, on this lab's branch, `pom.xml`. Do not create a second repository or work in the curriculum repository. If your private copy or the lab branch is missing, ask the instructor for help.

## Before you start: select the task-tracker branch

A Git branch names a line of development in the same repository. Each lab uses its own branch. For this lab, use **`task-tracker`** before opening the Maven project or asking the agent to edit files. Switching branches changes the tracked files in your working folder; Git does not switch database contents or your ignored `db.properties` file.

### GitHub Desktop

1. Open your private `coms211-labs-private` repository and inspect the Changes tab. If another lab has unfinished work, save and commit it on its own branch before switching. Do not discard work to force a switch.
2. Click **Fetch origin** to look for remote updates.
3. Open **Current Branch** and select **task-tracker**. If it is missing, stop and ask the instructor; do not create an empty branch from another lab.
4. If **Pull origin** is offered, inspect the incoming changes and pull before starting. Stop for help if Git reports a conflict.
5. Confirm **Current Branch: task-tracker** and that `pom.xml`, `README.md`, and `src/` are present. Keep your work on this branch.

### Command-line alternative

Open a terminal in your private repository. Check for unfinished work before switching:

```bash
git status
git fetch origin
git switch task-tracker
git pull --ff-only
git branch --show-current
```

The last command must print `task-tracker`. If the branch cannot be found, the pull reports no upstream, or Git refuses to switch or pull, stop and ask for help. Do not use a force option.

**Checkpoint:** correct private repository, branch `task-tracker`, expected starter files present, and no unrelated unfinished changes.

## Build and run in Eclipse

1. Choose **File > Import > Maven > Existing Maven Projects**, select your `coms211-labs-private` folder, and finish the import. If already imported, refresh the project after switching branches. Wait for dependencies to load.
2. Right-click the project and choose **Run As > Maven build…**. Enter `clean test` in Goals and run it. Expect **BUILD SUCCESS** and four baseline test methods with no failures. Maven must use JDK 21 or later.
3. Confirm the instructor has prepared `coms211_task_lab` and your ignored local `db.properties`. If settings are missing, copy `db.properties.example` to `db.properties` beside `pom.xml`, then enter the supplied lab account privately. Do not submit this file.
4. Create another Maven run configuration with Goals `javafx:run`. Use the project root as its base directory. Add a fictional task, refresh, close the app, relaunch, and compare its row in Workbench.
5. If the app says **Database settings unavailable**, check the file name, location, and nonblank `url`, `user`, and `password` entries, then restart. If it cannot load tasks, check the database and account in Workbench. Preserve a sanitized error if you need help.

In a terminal opened in that same folder, the equivalent commands are:

```bash
mvn clean test
mvn javafx:run
```

The evidence-report template is linked in this assignment. Passing unit tests does not verify the JavaFX interface or MySQL persistence.

## Required result

The supplied application adds and lists tasks. Add a clearly labeled **Mark complete** control to the JavaFX interface. A task has an ID, title, and completion status. An ID identifies a specific database row; two tasks may have the same title.

| Criterion | Required behavior |
| --- | --- |
| AC1 | Mark the selected task complete through the JavaFX interface. |
| AC2 | With no selection, show a useful message and change no records. |
| AC3 | Change only the selected ID, including when two task titles match. |
| AC4 | Preserve completion after refresh and a full application restart; verify the row in Workbench. |
| AC5 | Completing an already complete task leaves it complete and creates no rows. |
| AC6 | On database failure, show a useful error, do not falsely report success, and allow a safe retry or refresh. |
| AC7 | Preserve adding and listing, including rejection of blank titles and titles longer than 100 characters. |

Use the existing layers and parameterized SQL (SQL whose user values are supplied separately from the command). Do not add dependencies, filtering, login, deletion, or deployment.

## Repository map and feature path

Java files are under `src/main/java/edu/sussex/tasktracker/`.

| File | Responsibility |
| --- | --- |
| `TaskWindow.java` | JavaFX controls, selection, and messages. |
| `TaskService.java` | Input validation and application operations. |
| `TaskRepository.java` | Storage operation contract. |
| `JdbcTaskRepository.java` | Parameterized SQL and JDBC resource handling. |
| `Task.java` | Task ID, title, and completion status. |
| `src/test/java/edu/sussex/tasktracker/TaskServiceTest.java` | Four baseline service tests using a stand-in repository. |
| `db/schema.sql` and `db/reset.sql` | Instructor-managed schema and practice-data scripts. |

Trace the existing behavior before changing it: **Add task button → TaskService → TaskRepository → JdbcTaskRepository → MySQL**. Trace the returned data back to the list. Plan the completion feature using these same boundaries.

## Permitted AI use

Use Codex with the instructor-selected Qwen configuration. It may inspect source and tests, propose a plan, and implement one approved step at a time. Read the starter AGENTS.md before authorizing changes. You run database setup, inspect the GUI, review diffs, make commits, and decide acceptance. Do not share db.properties, access tokens, personal data, or identifiable student work. Use fictional task titles. Disclose the assistance in your report.

Follow: **Goal -> inspect -> plan -> act -> observe -> revise -> repeat -> report.**

## Module 9, part 1: establish and inspect the baseline

1. Complete the branch and build/run checks above, then follow the project README for baseline verification. Record your branch name and the supplied starter commit. The instructor prepares the tools and practice database.
2. Add a fictional task, refresh, close the window, relaunch, and compare its row in Workbench. Record observed results. Stop and use the recovery guidance if baseline behavior fails.
3. In GitHub Desktop inspect the files and confirm db.properties is ignored. Record the baseline commit on `task-tracker`; if you changed tracked baseline files with instructor approval, commit those changes and record that checkpoint. No new commit is needed when nothing changed.
4. Read TaskWindow, TaskService, TaskRepository, and JdbcTaskRepository. Trace Add task from a button action to an INSERT statement. Write one sentence about each component's responsibility.
5. Read the four supplied test methods. Explain why these service tests cannot establish that MySQL stores a row or that a JavaFX button works.

### Worked example: evaluate a plausible test

Suppose a requirement says that a blank title must be rejected without saving a record. A proposed test submits an empty string and checks that an exception occurs. That check covers the error signal, but does not check whether a row was saved before the exception. Improve the evidence by checking that the stored task list or row count is unchanged. Separately exercise the real interface and MySQL because an in-memory test does not establish JDBC behavior. Use the same reasoning to evaluate the mark-complete tests.

## Module 9, part 2: plan, implement, and review

1. Give Codex the goal and supplied criteria. A starting request is:

   > Read README.md, requirements.md, AGENTS.md, source, and tests. Do not edit yet. Explain how mark complete should pass from the JavaFX interface through the service to storage. Propose small steps, permitted files, and verification for AC1–AC7. Do not read credentials or change the database. Wait for my approval before editing.

2. Compare its plan with the criteria. Check that updates identify a task by ID and account for no selection, duplicate titles, repeated completion, and failure. Revise the plan if needed. Record your approved steps.
3. Authorize one step at a time, such as the storage/service operation and tests, followed by interface integration. After each step, inspect the diff before authorizing more work. Retain the starter tests. Do not approve broader changes without explaining their need to the instructor.
4. Evaluate the generated tests against the requirements. Identify one missing or inadequate test and improve it or describe the independent check that fills the gap. Do not invent a defect if none exists: explain an actual coverage limit and supply the missing evidence.
5. Run `mvn clean test` and exercise the feature through the interface. Begin the independent checks below and record failures for follow-up. A green test suite alone does not justify acceptance.
6. Resolve failures in bounded steps. Re-run the affected checks and relevant regression checks (checks that previously working behavior still works). Record any remaining limitations.
7. Update README instructions if behavior or operation changed. Inspect the diff, confirm secrets are absent, and commit the implementation checkpoint on `task-tracker`. Push that branch to your private origin using GitHub Desktop. Record the checkpoint commit ID and verify the commit appears on GitHub.

**Module 9 checkpoint:** show your baseline commit, reviewed feature commit, approved plan, automated test results, and known failures to the instructor. This is progress toward the same 10-point assignment; no second submission or separate grade is required.

Use the debugging and regression techniques introduced in Module 10 to investigate failures. Record the cause, repair, and recheck evidence. Do not invent a defect if your implementation passes the checks.

## Module 11: independent verification and acceptance

Resume the same repository and `task-tracker` branch. Review your checkpoint and unresolved issues. Complete the checks below, resolve failures in bounded steps, and re-run affected and regression checks. Update your report and README as needed. Inspect the final diff, commit and push the final work, and record the final commit ID before submitting.

Use the evidence-report template. For each AC record setup/input, expected result, actual result, and an artifact such as a screenshot, sanitized query result, or test name and output. Capture ID and status as text; do not rely on color alone.

- Create two tasks with the same fictional title. Note both IDs in Workbench. Complete one through the GUI. Verify that only its row changed and the count did not change.
- Refresh, then fully close and restart the application. Compare completion in the interface and Workbench.
- Start the application with no task selected and try the completion action. Check the message and unchanged rows. A disabled control with clear selection guidance is also acceptable.
- Complete the same task again. Verify unchanged row count and status.
- Recheck blank, whitespace-only, 100-character, and 101-character titles, plus a title containing an apostrophe. Verify rejected inputs create no rows and accepted input is stored accurately.
- With instructor assistance, use a temporary incorrect database password in your ignored local db.properties and restart the application. Attempt the completion workflow if available; verify that no success is reported. Restore settings, restart, refresh, and verify rows. Never screenshot or submit credentials. Record which operation actually failed: a startup read failure does not establish that an UPDATE failure is handled. Also provide an automated failure test at the completion operation boundary, or conduct an instructor-supervised interruption after loading tasks to exercise a real update failure. Do not stop a shared database service yourself.

## Submission in Canvas

Use **Text Entry** to submit the completed evidence report, or upload it as a Markdown, text, or PDF file. Both routes must include:

- Repository URL accessible to the instructor, branch `task-tracker`, and final commit ID. Keep the repository private unless the instructor directs otherwise; arrange instructor access.
- All application source, tests, schema/reset scripts, and current README in that repository. Exclude credentials and generated build output.
- Completed AC1–AC7 evidence table, test results, and your explanation of a test coverage gap and how you addressed it.
- One reviewed AI change and your reason for accepting, revising, or rejecting it, referring to the relevant file or commit.
- A short AI-assistance disclosure, remaining limitations, and a final acceptance decision supported by evidence.

Be prepared to demonstrate the feature and explain the UI -> service -> repository -> MySQL path without agent assistance. A full chat transcript is not required. The demonstration supports the review criterion rather than adding points beyond the 10-point rubric.

## Submission checklist

- Current branch is `task-tracker`; the instructor can access your private repository.
- Final code, tests, and documentation are committed and pushed on that branch.
- `mvn clean test` passes, and independent interface/database results are recorded honestly.
- The report identifies baseline/final commits and includes AC1–AC7, review judgment, and AI disclosure.
- Credentials and build output are excluded. Submit the evidence report in Canvas; no source archive is required.

## Recovery and stopping points

If the baseline does not run, preserve a sanitized error and ask for instructor help before making feature changes. For an agent mistake, inspect the diff in GitHub Desktop, preserve wanted work, and discard only the unwanted changes; do not rewrite repository history. For database trouble, use Workbench and the README checks. Git does not restore database contents. Only the instructor resets practice data, after you preserve wanted rows. If time runs out, submit your current commit, evidence, and explicit unmet criteria rather than claiming completion.

## Rubric — 10 points

| Criterion | Full evidence | Partial evidence | Insufficient evidence |
| --- | --- | --- | --- |
| Required behavior — 3 | 3: AC1–AC7 met, integrated behavior demonstrated | 2: feature works with one substantive gap; 1: partial implementation with several unmet criteria | 0: no usable feature or no assessable implementation |
| Verification and test evaluation — 3 | 3: AC evidence, test results, independent persistence/failure checks, and justified coverage evaluation | 2: credible checks with one important evidence gap; 1: limited checks or reliance mainly on passing tests | 0: no credible verification evidence |
| Review and explanation — 2 | 2: specific diff-based judgment, AI disclosure, and accurate explanation of the feature's path | 1: review or explanation is incomplete or weakly justified | 0: cannot explain accepted changes or provides no review |
| Reproducibility and Git — 2 | 2: accessible repository and final commit, usable instructions/scripts, baseline checkpoint, no submitted secrets | 1: work recoverable but history, access, or instructions incomplete | 0: work cannot be located or reproduced from the submission |

Original instructional prose: CC BY-NC 4.0 under LICENSE-CONTENT. Starter Java and SQL: MIT under LICENSE-CODE. No external instructional content is adapted. See the instructor README for technical references and verification status.
