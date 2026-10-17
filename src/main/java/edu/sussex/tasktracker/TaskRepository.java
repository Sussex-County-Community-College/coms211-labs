package edu.sussex.tasktracker;

import java.sql.SQLException;
import java.util.List;

public interface TaskRepository {
    List<Task> findAll() throws SQLException;
    void add(String title) throws SQLException;
}
