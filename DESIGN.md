# Small design to inspect

- GreetingApp selects the default or command-line name and displays output.
- Greeter produces greeting text; validation of the name belongs here.
- For the proposed increment, GreetingApp will handle the specified validation
  exception and display its message. It will not duplicate the name rules.
- GreeterTest is the protected starter test class. New feature tests belong in
  GreeterValidationTest and call the real Greeter method.

This is the supplied design for a small practice increment, not an implemented
feature. After review, the student adds a short "After review" note identifying
what the final source actually does and any remaining limitation.
