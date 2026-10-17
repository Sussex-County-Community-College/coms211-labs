package edu.sussex.tasktracker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class JdbcTaskRepository implements TaskRepository {
    private final String url;
    private final String user;
    private final String password;

    public JdbcTaskRepository(Path config) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(config)) { properties.load(input); }
        url = required(properties, "url");
        user = required(properties, "user");
        password = required(properties, "password");
    }

    private static String required(Properties properties, String key) throws IOException {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) throw new IOException("Missing database setting: " + key);
        return value;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override public List<Task> findAll() throws SQLException {
        try (Connection connection = connect();
             var statement = connection.prepareStatement("SELECT id, title, completed FROM tasks ORDER BY id");
             var results = statement.executeQuery()) {
            List<Task> tasks = new ArrayList<>();
            while (results.next()) tasks.add(new Task(results.getLong("id"), results.getString("title"), results.getBoolean("completed")));
            return tasks;
        }
    }

    @Override public void add(String title) throws SQLException {
        try (Connection connection = connect();
             var statement = connection.prepareStatement("INSERT INTO tasks (title) VALUES (?)")) {
            statement.setString(1, title);
            statement.executeUpdate();
        }
    }
}
