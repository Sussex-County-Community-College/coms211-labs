package edu.sussex.tasktracker;

public record Task(long id, String title, boolean completed) {
    @Override public String toString() {
        return "#" + id + " | " + (completed ? "Complete" : "Open") + " | " + title;
    }
}
