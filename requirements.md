# Required behavior

Baseline: add a stripped title of 1–100 Java String characters; reject null/blank or longer titles without inserting a row. Display saved tasks ordered by ID with ID, title, and completion status.

Feature acceptance criteria:

- AC1: A clearly labeled JavaFX control marks the selected task complete.
- AC2: With no task selected, show a useful message and change no records.
- AC3: Completing task A changes A only; task B remains unchanged, even when titles match.
- AC4: Refresh and a full application restart show A as complete; verify the same row in MySQL Workbench.
- AC5: Completing an already complete task leaves it complete and creates no rows.
- AC6: If a database operation fails, show a useful error and never report completion as successful. Offer a safe way to retry or refresh.
- AC7: Adding and listing tasks retain baseline behavior, including title validation.

Retain prepared statements. Do not add frameworks, dependencies, login, delete, filtering, networking, or deployment. Database integration and GUI behavior need evidence beyond the supplied unit tests.
