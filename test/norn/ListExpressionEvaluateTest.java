package norn;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ListExpressionEvaluateTest {
    /*
     Test overview:
       These tests are not designed to ensure that the structure of the ListExpression is proper;
       that is the goal of the parser tests. These tests take ListExpression.parse producing the
       correct ADT as an invariant, and based on that test only the evaluate method, which includes
       checking all of the operators and checking the case insensitivity.
       Note: comments directly preceding a test method describe what partitions the test covers.

     Testing strategy:
       Operators used:
         None
         Set Union
         Set Intersection
         Set Difference
         List definition
         Sequence
         Parallel
       Constructs used:
         Empty
         Recipient
         ListName
       Union:
         Union produces 0, 1, >1 size set
       Intersection:
         Intersection produces 0, 1, >1 size set
       Difference:
         Difference produces 0, 1, >1 size set
       ListName/Definition:
         ListName not previously defined
         ListName defined
         ListName defined and edited
         ListName defined and redefined
         ListName defined as reference to other ListName and other ListName is edited
       Empty:
         Empty by itself
         Empty used in parallel expression
         Empty used to define ListName
         Empty used in regular operator
       Mailing Loops:
         Mailing loop exists by direct reference (a=b;b=a)
         Mailing loop exists by indirect reference
         Mailing loop would have existed with previous definition of variable
       Parallel definition:
         No parallel definition problems
         Parallel definition exception at top level
         Parallel definition exception in subexpression
         Parallel definition exception with indirect reference
       Parallel:
         Subexpression of parallel raises exception
         Subexpression of parallel doesn't raise exception
         Subexpression of parallel defines listname
         Subexpression of parallel doesn't define listname
         Multiple parallels executed at same time
       Context Consistency:
         Error occurs in evaluation before list definition
         Error occurs in evaluation after list definition
    */


    private static final Set<String> INVALID = Set.of("INVALID");
    private static final Consumer<EvaluationException> THROW_ASSERTION_ERROR =
            e -> {
                throw new AssertionError(e);
            };
    private static final Consumer<EvaluationException> ASSERT_PARALLEL_DEFINITION_EXCEPTION =
            e -> assertEquals(ParallelDefinitionException.class, e.getClass());
    private static final Consumer<EvaluationException> ASSERT_MAIL_LOOP_EXCEPTION =
            e -> assertEquals(MailLoopException.class, e.getClass());
    private static final Consumer<EvaluationException> ASSERT_EVALUATION_EXCEPTION =
            Assertions::assertNotNull;

    private Context testMultiple(List<String> inputStrings, List<Set<String>> expectedOutputs) {
        return testMultiple(inputStrings, expectedOutputs, THROW_ASSERTION_ERROR);
    }

    /**
     * @param inputStrings     Strings
     * @param expectedOutputs
     * @param exceptionHandler
     * @return
     */
    private Context testMultiple(List<String> inputStrings, List<Set<String>> expectedOutputs, Consumer<EvaluationException> exceptionHandler) {
        Context context = new Context();
        assert inputStrings.size() == expectedOutputs.size();
        for (int i = 0; i < inputStrings.size(); i++) {
            final String s = inputStrings.get(i);
            final Set<String> expected = expectedOutputs.get(i);
            final ListExpression expr = ListExpression.parse(s);

            try {
                final Set<String> actual = context.evaluate(expr);
                assertEquals(expected, actual);
            } catch (EvaluationException e) {
                exceptionHandler.accept(e);
            }
        }
        return context;
    }

    private Context testSingular(String inputString, Set<String> expectedOutput) {
        return testSingular(inputString, expectedOutput, THROW_ASSERTION_ERROR);
    }

    private Context testSingular(String inputString, Set<String> expectedOutput, Consumer<EvaluationException> exceptionHandler) {
        return testMultiple(List.of(inputString), List.of(expectedOutput), exceptionHandler);
    }
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }

    // Operators used: None
    // Constructs used: Recipient
    @Test
    public void testSingleRecipient() {
        testSingular("bob@home", Set.of("bob@home"));
    }

    // Operators used: Set Union
    // Union: All Test cases
    @Test
    public void testUnion() {
        testSingular("bob@home,mary@work", Set.of("bob@home", "mary@work"));
        testSingular("(bob@home*mary@work),(fred@school*terry@gym)", Set.of());
        testSingular("bob@home,mary@work,fred@school,terry@gym", Set.of("bob@home", "mary@work", "fred@school", "terry@gym"));
    }

    // Operators used: Set Intersection
    // Intersection: All test cases
    @Test
    public void testIntersection() {
        testSingular("bob@home*bob@home", Set.of("bob@home"));
        testSingular("bob@home*mary@work", Set.of());
        testSingular("(mary@work,bob@home,fred@school)*(mary@work,fred@school)", Set.of("mary@work", "fred@school"));
    }

    // Operators used: Set Difference
    // Difference: All test cases
    @Test
    public void testDifference() {
        testSingular("bob@home!bob@home", Set.of());
        testSingular("bob@home!mary@work", Set.of("bob@home"));
        testSingular("(mary@work,bob@home,fred@school,terry@gym)!(terry@gym,fred@school)", Set.of("mary@work", "bob@home"));
    }

    // Operators used: ListName/Definition/Sequence
    // Constructs used: ListName
    @Test
    public void testDefinition() {
        testSingular("a", Set.of());
        testSingular("a=(a@mit,b@mit);a",
                Set.of("a@mit", "b@mit"));
        testMultiple(List.of("a=a@mit,b@mit;", "a"), List.of(Set.of(),
                Set.of("a@mit", "b@mit")));
        testMultiple(List.of("a=a@mit,b@mit;", "a=a,c@mit;", "a"),
                List.of(Set.of(), Set.of(), Set.of("a@mit", "b@mit", "c@mit")));
        testMultiple(List.of("a=a@mit", "a=b@mit", "a"),
                List.of(Set.of("a@mit"), Set.of("b@mit"), Set.of("b@mit")));
        testMultiple(List.of("a=b,a@mit", "b=b@mit", "a"),
                List.of(Set.of("a@mit"), Set.of("b@mit"), Set.of("a@mit", "b@mit")));
    }


    // Constructs used: Empty
    // Empty: All test cases
    @Test
    public void testEmpty() {
        testSingular("", Set.of());
        testSingular("(|)", Set.of());
        testMultiple(List.of("a=a@mit", "a=", "a"), List.of(Set.of("a@mit"), Set.of(), Set.of()));
        testSingular(" ! ", Set.of());
    }

    // Parallel definition: parallel definition at top level, in subexpression, with indirect reference
    // Parallel: subexpression of parallel raises exception
    @Test
    public void testInvalidParallel() {
        testSingular("a=a@mit|b=a,b@mit", INVALID, ASSERT_PARALLEL_DEFINITION_EXCEPTION);
        final String mailLoop = "a=b;b=c;c=a";
        final String parallelDefinition = "a=a@mit|b=a";
        final String parallelDefinitionInSubExpression = "a=a@mit|b=(b@mit,a)";
        final String parallelDefinitionByReference = "b=a;(a=a@mit|c=b,c@mit)";
        final String validExpression = "a@mit,b@mit";
        final Map<String, Consumer<EvaluationException>> invalidExpressionExpectedExceptions =
                Map.of(
                        parallelDefinition, ASSERT_PARALLEL_DEFINITION_EXCEPTION,
                        parallelDefinitionInSubExpression, ASSERT_PARALLEL_DEFINITION_EXCEPTION,
                        parallelDefinitionByReference, ASSERT_PARALLEL_DEFINITION_EXCEPTION,
                        validExpression + "|" + mailLoop, ASSERT_MAIL_LOOP_EXCEPTION,
                        mailLoop + "|" + validExpression, ASSERT_MAIL_LOOP_EXCEPTION,
                        validExpression + "|" + parallelDefinitionInSubExpression, ASSERT_PARALLEL_DEFINITION_EXCEPTION,
                        parallelDefinitionInSubExpression + "|" + validExpression, ASSERT_PARALLEL_DEFINITION_EXCEPTION,
                        /*
                         For the next two cases, we don't care which of the exceptions are thrown,
                         just that they are thrown.
                        */
                        parallelDefinition + "|" + mailLoop, ASSERT_EVALUATION_EXCEPTION,
                        mailLoop + "|" + parallelDefinition, ASSERT_EVALUATION_EXCEPTION
                );
        invalidExpressionExpectedExceptions.forEach(
                (expression, exceptionConsumer) -> testSingular(expression, INVALID, exceptionConsumer)
        );
    }

    // Parallel definition: No parallel definition problems
    // Parallel: Subexpression of parallel doesn't raise exception,
    // subexpression defines/doesn't define listname, multiple parallels
    @Test
    public void testParallel() {
        testMultiple(List.of("a=a@mit|b=b@mit|c=c@mit", "a,b,c"),
                List.of(Set.of(), Set.of("a@mit", "b@mit", "c@mit")));
        testSingular("a=a@mit;(a|b);a", Set.of("a@mit"));
    }

    // Mailing Loops: Mailing loop exists by direct reference, indirect reference, used to exist
    @Test
    public void testMailingLoop() {
        testSingular("a=b;b=a", INVALID, ASSERT_EVALUATION_EXCEPTION);
        testSingular("a=b;b=c;c=a", INVALID, ASSERT_MAIL_LOOP_EXCEPTION);
        testSingular("a=b;b=c;a=d;c=a;d=a@mit,b@mit;c", Set.of("a@mit", "b@mit"));
    }

    // Context consistency: All test cases
    @Test
    public void testContextConsistency() throws EvaluationException {
        Context context = new Context();
        final ListExpression expr1 = ListExpression.parse("(a=a@mit),(b=b@mit)");
        context.evaluate(expr1);
        final ListExpression expr2 = ListExpression.parse("a=b;b=a");
        assertThrows(MailLoopException.class, () -> context.evaluate(expr2));
        assertEquals(Set.of("a@mit"), context.evaluate(ListExpression.parse("a")));
        assertEquals(Set.of("b@mit"), context.evaluate(ListExpression.parse("b")));
    }
}
