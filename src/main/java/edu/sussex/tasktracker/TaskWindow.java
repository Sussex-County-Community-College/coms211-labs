package edu.sussex.tasktracker;

import java.nio.file.Path;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class TaskWindow extends Application {
    private TaskService service;
    private final ListView<Task> tasks = new ListView<>();
    private final Label status = new Label("Ready.");

    @Override public void start(Stage stage) {
        TextField title = new TextField();
        title.setPromptText("Task title (1–100 characters)");
        title.setAccessibleText("Task title");
        Label titleLabel = new Label("Task title:");
        titleLabel.setLabelFor(title);
        tasks.setAccessibleText("Saved tasks: ID, status, and title");
        status.setWrapText(true);
        Button add = new Button("Add task");
        Button refresh = new Button("Refresh");
        add.setOnAction(event -> {
            try {
                service.add(title.getText());
                title.clear();
                reload();
                status.setText("Task saved.");
            } catch (IllegalArgumentException error) { status.setText(error.getMessage()); }
              catch (SQLException error) { status.setText("Could not save or reload tasks. Check the database connection, then refresh before retrying."); }
        });
        refresh.setOnAction(event -> {
            try { reload(); status.setText("Tasks refreshed."); }
            catch (SQLException error) { status.setText("Could not load tasks. Check the database connection and try Refresh."); }
        });
        VBox content = new VBox(10, titleLabel, title, new HBox(10, add, refresh), tasks, status);
        content.setPadding(new Insets(16));
        stage.setTitle("COMS211 Task Tracker");
        stage.setScene(new Scene(content, 600, 420));
        stage.show();
        try { service = new TaskService(new JdbcTaskRepository(Path.of("db.properties"))); }
        catch (java.io.IOException error) {
            status.setText("Database settings unavailable. Check db.properties, then restart.");
            add.setDisable(true); refresh.setDisable(true); return;
        }
        try { reload(); }
        catch (SQLException error) { status.setText("Could not load tasks. Check the database connection and try Refresh."); }
    }

    private void reload() throws SQLException { tasks.getItems().setAll(service.list()); }

    public static void main(String[] args) { launch(args); }
}
