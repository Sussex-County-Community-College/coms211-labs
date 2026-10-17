-- Deletes task records ONLY in the dedicated practice database.
-- Export any wanted records before running. This is not a Git recovery operation.
USE coms211_task_lab;
DELETE FROM tasks;
INSERT INTO tasks (title, completed) VALUES ('Review requirements', FALSE), ('Run baseline tests', FALSE);
