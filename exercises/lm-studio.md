# LM Studio exploration cards

Use the supplied Qwen model in Windows-hosted LM Studio. Keep its prepared
settings. Copy only these public practice snippets; do not attach your repository,
credentials or personal information. A Windows chat cannot be assumed to see
files in your Ubuntu VM. Record the selected model identifier as displayed.

## Card A: explore and follow up

Ask an everyday question of your choice, then request a shorter explanation or
an example. Use fictional details. Record what the follow-up changed.

## Card B: explain and check Java

Paste this method and ask for its result with `Maya`, an empty string and three
spaces. Ask it to explain `+` when used with strings. Predict each result yourself
first; check against the baseline JAR in Ubuntu. The heading is separate.

```java
public String greet(String name) {
    return "Hello, " + name + "!";
}
```

## Card C: supply missing context

In a new chat ask: `How should my greeting program handle a blank name?`
Notice assumptions or clarification questions. Then supply: `This is a Java
console exercise. Empty and whitespace-only names must be rejected. The method
throws IllegalArgumentException with message Please enter a name. The console
prints that message. Explain expected behavior; do not write code.`
Compare how the additional requirements affected the answer. An unsupported
assumption is not necessarily a factual error; explain the distinction.

## Card D: begin a fresh conversation

Start another new chat and ask: `What requirements did I give you for my greeting
program?` Inspect whether the response is justified by the visible conversation.
Do not assume that a plausible answer demonstrates access to the earlier chat.
Then paste the short requirements and observe the difference.

## Card E: evaluate a supplied inaccurate explanation

The statement below was written for this exercise. It is not a captured Qwen
response, and you must evaluate it even if all your Qwen answers were correct:

> The original greet method automatically removes spaces and rejects an empty
> name, so its three passing tests establish that blank input is handled safely.

Identify the unsupported claims. Inspect the original source, run the baseline
with `""` and `"  Maya  "`, and explain what the evidence shows. Ask Qwen to
critique the statement only after recording your own evaluation. Do not require
or claim that Qwen must make a mistake during your session.
