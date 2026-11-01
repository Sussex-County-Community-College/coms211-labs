# Checks planned before implementation

A partition is a group of inputs expected to behave similarly. Here the important
groups are ordinary names, empty names, whitespace-only names and padded names.

Worked example: with argument `Maya`, the greeting must be `Hello, Maya!`.
Check the actual Greeter method and separately run the console application.

Before asking for implementation, write expected method results and console
results for these cases in your report. Use requirements.md to determine them:

| Case | Example input |
| --- | --- |
| Ordinary name | `Maya` |
| Internal space | `Grace Hopper` |
| Empty string | `""` |
| Spaces only | `"   "` |
| Tab only (method test) | `"\t"` in a Java string |
| Surrounding whitespace | `"  Maya  "` |
| No command-line argument | No argument supplied |

After implementation, inspect new test assertions, run `mvn -o clean package`,
and run the JAR with ordinary, empty, spaces-only, padded and no-argument inputs.
Record actual results separately. Unit tests of Greeter do not verify the
console's error handling. A report from the agent is not an observed test result.
After review, add a short note here identifying completed checks and remaining
checks; do not mark planned checks as completed before running them.
