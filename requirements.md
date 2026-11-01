# Greeting requirements and bounded Module 2 change

## Existing behavior

The console application prints `COMS211 greeting practice`, then a greeting.
With no argument the name is `student`; otherwise it uses the first argument.
Additional arguments are outside this exercise; use quotes around a full name.
The supplied greet method joins `Hello, `, the name and `!`. It does not currently
validate or trim the name. The three original tests check ordinary names only.

## Authorized increment: handle blank names

Whitespace includes spaces and tabs. An exception is Java's signal that an
operation cannot complete normally; IllegalArgumentException signals an invalid
method argument. The console handles that signal and displays the message.

1. `Greeter.greet(String name)` must reject the empty string and whitespace-only
   strings with `IllegalArgumentException("Please enter a name.")`.
2. For a nonblank name, remove surrounding whitespace and return
   `Hello, ` plus the resulting name plus `!`. Preserve internal spaces.
3. Keep ordinary-name and no-argument application behavior, including the heading.
4. When greet rejects a command-line name, GreetingApp must display
   `Please enter a name.` after its heading, without a stack trace or greeting.
5. Preserve the supplied tests, method signature, dependencies and project layout.

The console always supplies a non-null string. Null handling, more arguments,
new UI features, database storage and new libraries are outside this increment.
The student records expected results before implementation and makes the final
acceptance decision after source review and actual checks.
