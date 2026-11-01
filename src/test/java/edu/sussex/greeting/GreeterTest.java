package edu.sussex.greeting;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GreeterTest {
    private final Greeter greeter = new Greeter();

    @Test
    void greetsOneName() {
        assertEquals("Hello, Maya!", greeter.greet("Maya"));
    }

    @Test
    void preservesSpacesWithinAName() {
        assertEquals("Hello, Grace Hopper!", greeter.greet("Grace Hopper"));
    }

    @Test
    void greetsTheDefaultName() {
        assertEquals("Hello, student!", greeter.greet("student"));
    }
}
