package edu.sussex.tasktracker;

import java.sql.SQLException;
import java.util.List;

public final class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) { this.repository = repository; }

    public List<Task> list() throws SQLException { return repository.findAll(); }

    public void add(String title) throws SQLException {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Enter a task title.");
        String cleaned = title.strip();
        if (cleaned.length() > 100) throw new IllegalArgumentException("Use 100 characters or fewer.");
        repository.add(cleaned);
    }
}
