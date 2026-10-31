package edu.sussex.tictactoe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke test for the GameWindow class.
 * This test ensures the basic structure is in place and can be executed.
 */
public class GameWindowTest {
    
    @Test
    public void testGameWindowExists() {
        assertNotNull(GameWindow.class, "GameWindow class should exist");
    }
    
    @Test
    public void testApplicationInheritance() {
        // Verify that GameWindow extends Application
        Class<?> superClass = GameWindow.class.getSuperclass();
        assertEquals("javafx.application.Application", superClass.getName(), 
            "GameWindow should extend javafx.application.Application");
    }
}