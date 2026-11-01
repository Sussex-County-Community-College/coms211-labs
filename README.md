# Module 1: Development Environment Introduction

COMS211 Software Engineering with AI — Sussex County Community College.

**Complete this lab without AI assistance.** Follow the Canvas assignment,
instructor demonstration and these build instructions. This is a supplied
practice program, not the semester project.

## Start in the correct working copy

The instructor starter is `~/GitHub/coms211-labs`. Your work belongs in your
private `~/GitHub/coms211-private` repository on branch `environment-intro`.
The instructor prepares the branches and dependency cache before class.

First-time setup only: from the clean instructor starter, select
`environment-intro`, then run `bash scripts/create_student_repo.sh`. Read the
account/destination summary before confirming with your own GitHub username.
The script creates your private copy, invites `jsissler` and opens the clone on
`main`. In GitHub Desktop, add that private local repository, then select
`environment-intro`. If a private repository or destination folder already
exists, preserve it and ask for help; do not rerun the creation script.

## Build, test and run

Use the supplied Linux VM, Java 21 or later, and prepared Maven installation.
Open a terminal in the private project root, beside `pom.xml`:

```bash
pwd
java -version
mvn -version
mvn -o clean package
java -jar target/coms211-greeting.jar
java -jar target/coms211-greeting.jar Maya
java -jar target/coms211-greeting.jar "Grace Hopper"
```

The clean build should report **3 tests, zero failures/errors/skips**, followed
by BUILD SUCCESS. `-o` uses the prepared local dependency cache. If a dependency
is missing, ask the instructor to prepare it; do not change versions.

The no-argument run prints:

```text
COMS211 greeting practice
Hello, student!
```

The Maya run ends with `Hello, Maya!`; the quoted full name ends with
`Hello, Grace Hopper!`. Quotes keep a full name together as one argument.
Rebuild after a source edit before running the JAR again.

## Files and the manual change

- `GreetingApp.java`: selects a name and displays the heading and greeting.
- `Greeter.java`: produces the greeting string.
- `GreeterTest.java`: three supplied checks of the greeting method.
- `pom.xml`: repeatable build, test dependency and runnable JAR configuration.
- `.gitignore`: keeps generated `target/` and local editor/settings files out
  of commits. Do not edit it for the exercise.
- `notes/recovery-practice.txt`: safe file for practicing recovery of one edit.
- `EVIDENCE-TEMPLATE.md`: copy into `notes/environment-report.md` and complete.

Import the existing project into Eclipse with **File > Import > Maven > Existing
Maven Projects**, selecting your private folder. Eclipse and GitHub Desktop
must use the same working copy. Refresh the project if external edits are stale.

Manual task: in `GreetingApp.java`, change only the displayed heading from
`COMS211 greeting practice` to `COMS211 environment check`. Save, inspect the
one-line diff, rebuild and run. The greeting stays the same. The supplied tests
check greeting text, so you must separately observe the changed heading.

Recovery task: while the program change is saved, add a temporary third line to
`notes/recovery-practice.txt`. In GitHub Desktop inspect that file, then discard
**only that file's uncommitted change**, confirming the selected filename first.
Verify its original two lines return and your program change remains. Do not
choose Discard All Changes. Ask for help if the selected file is ambiguous.

Commit the reviewed program change and your report on `environment-intro`, push,
and confirm the branch/commit in your private GitHub repository. If recording
the final commit ID in the report requires another commit, identify the earlier
code commit; do not attempt to put a commit's own ID inside itself.

## Recovery and boundaries

Wrong folder/branch: stop and preserve work. Missing tools/cache: record the
first useful error and ask the instructor. Missing branch in a private copy:
ask the instructor to transfer it; Fetch origin alone does not copy branches
from the instructor's separate repository. Build output is generated; do not
commit `target/`. This lab uses no database, model service or network API.

Original code: MIT (`LICENSE-CODE`). Original prose: CC BY-NC 4.0
(`LICENSE-CONTENT`). Third-party tools and dependencies retain their licenses.
