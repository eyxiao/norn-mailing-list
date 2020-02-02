package norn;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import norn.Main.EndOfProgram;

public class MainTest {
    // Partitioned as follows:
    //  - input starts with /save, /load, or an invalid command, or input is empty or just the expression to evaluate
    //      - /load file
    //          - file exists, does not exist
    //          - file is parsable, not parsable
    //      - /save filepath
    //          - able to write file, unable to write file
    //      - invalid command
    //      - input empty
    //      - input expression
    //          - expression can be parsed, can't be parsed
    //          - expression contains a mail loop, contains an illegal parallel definition, 
    //              expression can be evaluated successfully
    
    /*
     * Evaluates a list of lists, where each inner list follows the format [inputIntoMain, expectedOutput]
     */
    private static void testHelper(List<List<String>> evaluateList) throws EndOfProgram, IOException {
        Main m = new Main();
        for (List<String> l : evaluateList) {
            assertEquals(l.get(1), m.getReturn(l.get(0)));
        }
        assertThrows(Main.EndOfProgram.class, () -> m.getReturn(""));
    }
    
    /*
     * Helper method for deleting files created while testing the save method
     */
    private static void deleteFile(String filename) {
        File file = new File(filename);
        file.delete();
    }
    
    /*
     * Helper method for creating files while testing the load method
     */
    private static void createFile(String filename, String text) throws IOException {
        File file = new File(filename);
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(text);
        writer.close();
    }
    
    @Test
    @Tag("no-didit")
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }

    // Covers: input expression can be parsed
    //         input expression can be evaluated successfully

    @Test
    @Tag("no-didit")
    public void testSimple() throws Main.EndOfProgram, IOException {
        List<List<String>> evaluateList = new ArrayList<>();
        evaluateList.add(List.of("a@mit", "a@mit"));
        evaluateList.add(List.of("b=b@mit", "b@mit"));
        evaluateList.add(List.of("a=a@mit,b@mit;a,b", "a@mit, b@mit"));
        testHelper(evaluateList);
    }
    
    // Covers: input expression can be parsed
    //         input expression contains a mail loop
    @Test
    @Tag("no-didit")
    public void testMailLoop() throws Main.EndOfProgram, IOException {
        List<List<String>> evaluateList = new ArrayList<>();
        evaluateList.add(List.of("a=b;b=a", "Expression creates mailing loop."));
        testHelper(evaluateList);
    }
    
    // Covers: input expression can be parsed
    //         input expression contains a mail loop
    @Test
    @Tag("no-didit")
    public void testIllegalParallel() throws Main.EndOfProgram, IOException {
        List<List<String>> evaluateList = new ArrayList<>();
        evaluateList.add(List.of("(x=a@mit|y=x);x", "Invalid parallel definition."));
        testHelper(evaluateList);
    }
    
    // Covers: input expression can be parsed
    //         input expression contains a mail loop
    @Test
    @Tag("no-didit")
    public void testExpressionUnparsable() throws Main.EndOfProgram, IOException {
        List<List<String>> evaluateList = new ArrayList<>();
        evaluateList.add(List.of("a@mit=a", "Unable to parse input."));
        testHelper(evaluateList);
    }
    
    // Covers: /save able to write file
    @Test
    @Tag("no-didit")
    public void testSaveSuccess() throws Main.EndOfProgram, IOException {
        List<List<String>> evaluateList = new ArrayList<>();
        evaluateList.add(List.of("a=a@mit", "a@mit"));
        evaluateList.add(List.of("/save hello.txt", "File saved."));
        testHelper(evaluateList);
        deleteFile("hello.txt");
    }

    // Covers: /save unable to write file
    @Test
    @Tag("no-didit")
    public void testSaveFail() throws Main.EndOfProgram, IOException {
        List<List<String>> evaluateList = new ArrayList<>();
        evaluateList.add(List.of("/save /", "Unable to write file."));
        testHelper(evaluateList);
    }
    
    // Covers: /load file exists
    //               file is parsable
    @Test
    @Tag("no-didit")
    public void testLoadSuccess() throws Main.EndOfProgram, IOException {
        List<List<String>> evaluateList = new ArrayList<>();
        evaluateList.add(List.of("e=e@mit", "e@mit"));
        evaluateList.add(List.of("/save a.txt", "File saved."));
        evaluateList.add(List.of("/load a.txt", "File loaded."));
        evaluateList.add(List.of("e", "e@mit"));
        testHelper(evaluateList);
        deleteFile("a.txt");
    }
    
    // Covers: /load file does not exist
    @Test
    @Tag("no-didit")
    public void testLoadFileDoesNotExist() throws Main.EndOfProgram, IOException {
        List<List<String>> evaluateList = new ArrayList<>();
        evaluateList.add(List.of("/load doesntexist.txt", "Unable to load file."));
        testHelper(evaluateList);
    }
    
    // Covers: /load file exists
    //               file is not parsable
    @Test
    @Tag("no-didit")
    public void testLoadFileNotParsable() throws Main.EndOfProgram, IOException {
        createFile("file.txt", "\"notparsable@\"");
        List<List<String>> evaluateList = new ArrayList<>();
        evaluateList.add(List.of("/load file.txt", "Unable to parse file."));
        testHelper(evaluateList);
        deleteFile("file.txt");
    }

    // Covers: invalid command
    @Test
    @Tag("no-didit")
    public void testInvalidCommand() throws Main.EndOfProgram, IOException {
        List<List<String>> evaluateList = new ArrayList<>();
        evaluateList.add(List.of("/command doesntexist", "INVALID COMMAND"));
        testHelper(evaluateList);
    }
    
    // Covers: empty input
    @Test
    @Tag("no-didit")
    public void testInputEmpty() throws Main.EndOfProgram, IOException {
        Main m = new Main();
        assertThrows(Main.EndOfProgram.class, () -> m.getReturn(""));
    }
}
