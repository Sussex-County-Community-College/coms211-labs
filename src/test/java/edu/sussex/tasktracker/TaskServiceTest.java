package edu.sussex.tasktracker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {
    // This in-memory stand-in checks service behavior; it does not test JDBC or MySQL.
    private static class MemoryRepository implements TaskRepository {
        final List<Task> tasks = new ArrayList<>();
        @Override public List<Task> findAll() { return List.copyOf(tasks); }
        @Override public void add(String title) { tasks.add(new Task(tasks.size() + 1, title, false)); }
    }

    @Test void rejectsBlankTitlesWithoutSaving() {
        var repository = new MemoryRepository();
        var service = new TaskService(repository);
        for (String title : new String[]{"", " ", "\t"})
            assertThrows(IllegalArgumentException.class, () -> service.add(title));
        assertThrows(IllegalArgumentException.class, () -> service.add(null));
        assertTrue(repository.tasks.isEmpty());
    }

    @Test void trimsAndSavesTitle() throws SQLException {
        var service = new TaskService(new MemoryRepository());
        service.add("  Study Java  ");
        assertEquals(List.of(new Task(1, "Study Java", false)), service.list());
    }

    @Test void checksTitleLengthBoundary() throws SQLException {
        var service = new TaskService(new MemoryRepository());
        service.add("x".repeat(100));
        assertThrows(IllegalArgumentException.class, () -> service.add("x".repeat(101)));
        assertEquals(1, service.list().size());
    }

    @Test void propagatesStorageFailure() {
        var service = new TaskService(new TaskRepository() {
            public List<Task> findAll() throws SQLException { throw new SQLException("Test failure"); }
            public void add(String title) throws SQLException { throw new SQLException("Test failure"); }
        });
        assertThrows(SQLException.class, service::list);
        assertThrows(SQLException.class, () -> service.add("Study"));
    }
}
