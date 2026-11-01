package edu.sussex.greeting;

/** Runs a small greeting example with an optional name argument. */
public class GreetingApp {
    public static void main(String[] args) {
        System.out.println("COMS211 greeting practice");
        String name = args.length == 0 ? "student" : args[0];
        Greeter greeter = new Greeter();
        System.out.println(greeter.greet(name));
    }
}
