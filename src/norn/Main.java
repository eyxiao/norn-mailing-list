/* Copyright (c) 2018 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package norn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Start the Norn mailing list system console interface and web server.
 */
public class Main {
    private static final int DEFAULT_PORT = 8080;
    private static final int AUTO_GRADE_PORT = 5050;
    private final Context context = new Context();
    private final WebServer web;
    
    // AF(context, web) = a mailing list system console interface and its web server
    //
    // RI:
    // - true
    //
    // SRE:
    // - all fields are private and final
    // - references to context and web are never returned by methods
    //
    // TSA:
    // - context is threadsafe and web operates with thread safety

    /**
     * Main constructor.
     * @throws IOException if input or output operation has failed
     */
    Main() throws IOException {
        this(AUTO_GRADE_PORT);
    }

    /**
     * Main constructor with specified port.
     * @param port port number to use
     * @throws IOException if input or output operation has failed
     */
    Main(int port) throws IOException {
        web = new WebServer(port, context);
        web.start();
    }

    /**
     * Read expression and command inputs from the console and output results,
     * and start a web server to handle requests from remote clients.
     * An empty console input terminates the program.
     * @param args unused
     * @throws IOException if there is an error reading the input
     */
    public static void main(String[] args) throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        final Main m = new Main(DEFAULT_PORT);

        while (true) {
            System.out.print("> ");
            final String input = in.readLine();
            try {
                System.out.println(m.getReturn(input));
            } catch (EndOfProgram endOfProgram) {
                return;
            }
        }
    }

    /**
     * Handles the different input of the console:
     * - /load: loads the file of the corresponding path and updates the console with listname definitions
     * - /save: saves the listname definitions to a corresponding filepath
     * - listExpression grammar: parses and generates a mailing list
     * - else: nothing, as it is an invalid command
     * @param input the different input that can be parsed in the console as decribed above
     * @return the output of the console based on the input:
     * - /load: "File loaded."
     * - /save: "File saved."
     * - ListExpression grammar: the corresponding mailing list
     * - else: "INVALID COMMAND"
     * @throws EndOfProgram if input is empty
     */
    String getReturn(String input) throws EndOfProgram {
        if (input.isEmpty()) {
            web.stop();
            throw new EndOfProgram(); // exits the program
        } else if (input.startsWith("/")) {
            final String[] split = input.split("\\s+", 2);
            assert split.length == 2;
            final String command = split[0];
            final Path path = Paths.get(split[1]);
            switch (command) {
                case "/load":
                    String load;
                    try {
                        load = Files.readString(path);
                    } catch (IOException e) {
                        return "Unable to load file.";
                    }
                    try {
                        context.load(load);
                    } catch (UnableToLoadException e) {
                        return "Unable to parse file.";
                    }
                    return "File loaded.";
                case "/save":
                    String save = context.save();
                    try {
                        Files.write(path, save.getBytes());
                    } catch (IOException e) {
                        return "Unable to write file.";
                    }
                    return "File saved.";
                default:
                    return "INVALID COMMAND";
            }
        } else {
            try {
                Set<String> result = context.evaluate(ListExpression.parse(input));
                return Util.setToOrderedString(result);
            } catch (EvaluationException e) {
                if (e instanceof MailLoopException) {
                    return "Expression creates mailing loop.";
                } else if (e instanceof ParallelDefinitionException) {
                    return "Invalid parallel definition.";
                } else {
                    return "Unexpected exception in evaluation";
                }
            } catch (IllegalArgumentException e) {
                return "Unable to parse input.";
            }
        }
    }

    static class EndOfProgram extends Exception {

    }
}
