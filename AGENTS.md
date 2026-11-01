# Module 2 AI exploration boundaries

The student is the software engineer and approves each increment.
Students may delegate implementation, but not responsibility.
Goal -> inspect -> plan -> act -> observe -> revise -> repeat -> report.

Read README.md, requirements.md, DESIGN.md, TEST-PLAN.md and relevant source and
tests before acting. Inspection and planning requests authorize no file changes.
Stop after the requested stage; do not continue to later prompt files on your own.

For the authorized blank-name feature only, you may edit:
- src/main/java/edu/sussex/greeting/Greeter.java
- src/main/java/edu/sussex/greeting/GreetingApp.java
- create src/test/java/edu/sussex/greeting/GreeterValidationTest.java

Keep the existing public method signature and dependencies. Use Java release 21,
Maven and the existing JUnit 5 dependency. Do not modify supplied GreeterTest.java,
pom.xml, these instructions, requirements, design, test plan, exercises, prompts
or evidence. The student reconciles documentation and writes their own report.
Do not install software, change accounts, inspect credentials, commit or push.
Do not read files outside this project for the exercise.

Run mvn -o clean package after implementation. Tests must call the actual greet
method and assert the required returned string or IllegalArgumentException,
including the exception message. Preserve supplied tests. Check normal names,
empty input, whitespace-only input and surrounding whitespace. Report commands
actually run, test totals, failures, changed files and remaining checks. Do not
claim that tests alone prove correctness or that the student accepted the work.
A missing tool/cache/connection is a setup problem: report it without changing
versions or weakening checks. Stop for student inspection after one increment.
