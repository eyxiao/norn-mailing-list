package norn;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ListExpressionParserTest {
    /*
     Testing Strategy
          recipient:
              contains whitespace
              Capitalization difference
              parenthesis difference
          listname
              contains whitespace
              Capitalization difference
              parenthesis difference
          empty
              inside definition
              inside sequence
              inside intersection
              inside difference
              inside union
              inside parallel
          intersection:
              contains whitespace
              parenthesis difference
              contains recipient
              contains intersection
              contains difference
              contains union
              contains sequence
              contains definition
              contains parallel
              contains listname
          difference:
              contains whitespace
              parenthesis difference
              contains intersections
              contains difference
              contains recipient
              contains union
              contains sequence
              contains definition
              contains parallel
              contains listname
          definition
              contains whitespace
              parenthesis difference     
              contains intersections
              contains difference
              contains recipient
              contains union
              contains sequence    
              contains definition     
              contains parallel
              contains listname
          union:
              contains whitespace
              parenthesis difference
              contains intersections
              contains difference
              contains recipient
              contains union
              contains sequence
              contains definition
              contains parallel
              contains listname
          sequence
              contains whitespace
              parenthesis difference
              contains intersections
              contains difference
              contains recipient
              contains union
              contains sequence
              contains definition
              contains parallel
              contains listname
          parallel
              contains whitespace
              parenthesis difference
              contains intersections
              contains difference
              contains recipient
              contains union
              contains sequence
              contains definition
              contains parallel
              contains listname
          precedence order:
              intersection has priority over difference
              difference has priority over union
              union has priority over definition
              definition has priority over sequence
              sequence has priority over parallel
              reverse order for definition of definition
          invalids
              characters that don't work
     */

    /**
     * Asserts that two objects are equal and their hashCodes are equal
     *
     * @param o1 an object
     * @param o2 an object
     */
    private static void assertEqualsAndHash(Object o1, Object o2) {
        assertEquals(o1, o2);
        assertEquals(o1.hashCode(), o2.hashCode());
    }

    /**
     * Asserts the spec of ListExpression.toString() and ListExpression.equals() on a given expression
     *
     * @param expression an ListExpression
     * @throws ParallelDefinitionException if error in parallel definition
     */
    private static void assertConsistent(ListExpression expression) throws ParallelDefinitionException {
        final String expressionString = expression.toString();
        final ListExpression parsedExpression = ListExpression.parse(expressionString);
        assertEqualsAndHash(parsedExpression, expression);
        assertEquals(parsedExpression.toString(), expressionString);
    }
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    // Tests
    //   recipient
    //      w & w/o whitespace
    @Test
    public void testRecipientBasic() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob@home");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Recipient("bob@home");
        assertEquals(parseExpression, genExpression);
        final ListExpression parseExpression2 = ListExpression.parse("    bob@home     ");
        assertConsistent(parseExpression2);
        assertEquals(parseExpression2, genExpression);
    }
    
    // Tests
    //   recipient
    //      capitalization difference
    @Test
    public void testRecipientCapitalizationDiff() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("Bob@home");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Recipient("bob@home");
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   recipient
    //      Parenthesis difference
    @Test
    public void testRecipientWithParenthesis() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("(Bob@home)");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Recipient("bob@home");
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   listname
    //      w & w/o whitespace
    @Test
    public void testlistNameBasic() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new ListName("bob");
        assertEquals(parseExpression, genExpression);
        final ListExpression parseExpression2 = ListExpression.parse("    bob     ");
        assertConsistent(parseExpression2);
        assertEquals(parseExpression2, genExpression);
    }
    
    // Tests
    //   listname
    //      capitalization difference
    @Test
    public void testListNameCapitalizationDiff() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("Bob");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new ListName("bob");
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   listname
    //      Parenthesis difference
    @Test
    public void testListNameWithParenthesis() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("(Bob)");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new ListName("bob");
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   intersection
    //      w & w/o whitespace
    //      contains recipient
    @Test
    public void testIntersectionBasic() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob@home*milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Intersection(new Recipient("bob@home"), new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        final ListExpression parseExpression2 = ListExpression.parse("    bob@home  *  milk@juice   ");
        assertConsistent(parseExpression2);
        assertEquals(parseExpression2, genExpression);        
    }
    
    // Tests
    //   intersection
    //      Parenthesis difference
    @Test
    public void testIntersectionWithParenthesis() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("(milk@juice*jane@bird)*milk@juice");
        final ListExpression parseExpression2 = ListExpression.parse("milk@juice*(jane@bird*milk@juice)");
        assertConsistent(parseExpression);
        assertConsistent(parseExpression2);
        final ListExpression genExpression = new Intersection(new Intersection(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        assertNotEquals(parseExpression2, genExpression);
    }
    
    // Tests
    //   intersection
    //      contains intersection in parenthesis
    @Test
    public void testIntersectionWithIntersection() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple*big@small)*milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Intersection(new Intersection(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   intersection
    //      contains union in parenthesis
    @Test
    public void testIntersectionWithUnion() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple,big@small)*milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Intersection(new Union(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   intersection
    //      contains difference in parenthesis
    @Test
    public void testIntersectionWithDifference() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple!big@small)*milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Intersection(new Difference(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   intersection
    //      contains sequence in parenthesis
    @Test
    public void testIntersectionWithSequence() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple;big@small)*milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Intersection(new Sequence(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   intersection
    //      contains parallel in parenthesis
    @Test
    public void testIntersectionWithParallel() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple|big@small)*milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Intersection(new Parallel(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   intersection
    //      contains definition in parenthesis
    //      contains listname
    @Test
    public void testIntersectionWithDefinition() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(apple = big@small)*milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Intersection(new Definition(new ListName("apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    
    // Tests
    //   difference
    //      w & w/o whitespace
    //      contains recipient
    @Test
    public void testDifferenceBasic() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob@home!milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Difference(new Recipient("bob@home"), new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        final ListExpression parseExpression2 = ListExpression.parse("    bob@home  !  milk@juice   ");
        assertConsistent(parseExpression2);
        assertEquals(parseExpression2, genExpression);
    }
    
    // Tests
    //   difference
    //      contains intersections
    @Test
    public void testDifferenceWithIntersection() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice*jane@bird!milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Difference(new Intersection(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   difference
    //      contains union
    @Test
    public void testDifferenceWIthUnion() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("(milk@juice,jane@bird)!milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Difference(new Union(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        
    }
    
    // Tests
    //   difference
    //      contains difference
    @Test
    public void testDifferenceWithDifference() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice!jane@bird!milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Difference(new Difference(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //  difference
    //      parenthesis difference
    @Test
    public void testDifferenceWithParenthesis() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("(milk@juice!jane@bird)!milk@juice");
        final ListExpression parseExpression2 = ListExpression.parse("milk@juice!(jane@bird!milk@juice)");
        assertConsistent(parseExpression);
        assertConsistent(parseExpression2);
        final ListExpression genExpression = new Difference(new Difference(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        assertNotEquals(parseExpression2, genExpression);
    }
    
    // Tests
    //   intersection
    //      contains sequence in parenthesis
    @Test
    public void testDifferenceWithSequence() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple;big@small)!milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Difference(new Sequence(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   intersection
    //      contains parallel in parenthesis
    @Test
    public void testDifferenceWithParallel() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple|big@small)!milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Difference(new Parallel(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   intersection
    //      contains definition in parenthesis
    //      contains listname
    @Test
    public void testDifferenceWithDefinition() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(apple = big@small)!milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Difference(new Definition(new ListName("apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   definition
    //      w & w/o whitespace
    //      contains recipient
    //      contains listname
    @Test
    public void testDefinitionBasic() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob = milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("bob"), new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        final ListExpression parseExpression2 = ListExpression.parse("    bob  =  milk@juice   ");
        assertConsistent(parseExpression2);
        assertEquals(parseExpression2, genExpression);
    }
    
    // Tests
    //   definition
    //      contains intersections
    @Test
    public void testDefinitionWithIntersection() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk = jane@bird*milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("milk"), new Intersection(new Recipient("jane@bird"),
                new Recipient("milk@juice")));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   definition
    //      contains union
    @Test
    public void testDefinitionWithUnion() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk = jane@bird,milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("milk"), new Union(new Recipient("jane@bird"),
                new Recipient("milk@juice")));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   definition
    //      contains difference
    @Test
    public void testDefinitionWithDifference() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk = jane@bird!milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("milk"), new Difference(new Recipient("jane@bird"),
                new Recipient("milk@juice")));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //  definition
    //      parenthesis difference
    @Test
    public void testDefinitionWithParenthesis() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob = (milk@juice)");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("bob"), new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //  definition
    //      contains sequence in paranthesis
    @Test
    public void testDefinitionWithSequence() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob = (milk@juice; milk@juice)");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("bob"), new Sequence(new Recipient("milk@juice"), new Recipient("milk@juice")));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    // definition
    //      contains parallel in paranthesis
    @Test
    public void testDefinitionWithParallel() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob = (milk@juice| milk@juice)");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("bob"), new Parallel(new Recipient("milk@juice"), new Recipient("milk@juice")));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    // definition
    //      contains Definition in paranthesis
    @Test
    public void testDefinitionWithDefinition() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob = (jane = milk@mit)");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("bob"), new Definition(new ListName("jane"), new Recipient("milk@mit")));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   union
    //      w & w/o whitespace
    //      contains recipient
    @Test
    public void testUnionBasic() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob@home,milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Union(new Recipient("bob@home"), new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        final ListExpression parseExpression2 = ListExpression.parse("    bob@home  ,  milk@juice   ");
        assertConsistent(parseExpression2);
        assertEquals(parseExpression2, genExpression);
    }
    
    // Tests
    //   union
    //      contains intersections
    @Test
    public void testUnionWithIntersection() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice*jane@bird,milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Union(new Intersection(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   union
    //      contains union
    @Test
    public void testUnionWIthUnion() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice,jane@bird,milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Union(new Union(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);        
    }
    
    // Tests
    //   union
    //      contains difference
    @Test
    public void testUnionWithDifference() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice!jane@bird,milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Union(new Difference(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //  union
    //      parenthesis difference
    @Test
    public void testUnionWithParenthesis() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("(milk@juice,jane@bird),milk@juice");
        final ListExpression parseExpression2 = ListExpression.parse("milk@juice,(jane@bird,milk@juice)");
        assertConsistent(parseExpression);
        assertConsistent(parseExpression2);
        final ListExpression genExpression = new Union(new Union(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        assertNotEquals(parseExpression2, genExpression);
    }
    
    // Tests
    //   union
    //      contains sequence in parenthesis
    @Test
    public void testUnionWithSequence() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple;big@small),milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Union(new Sequence(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   union
    //      contains parallel in parenthesis
    @Test
    public void testUnionWithParallel() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple|big@small),milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Union(new Parallel(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   union intersection
    //      contains definition in parenthesis
    //      contains listname
    @Test
    public void testUnionWithDefinition() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(apple = big@small),milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Union(new Definition(new ListName("apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   Parallel
    //      w & w/o whitespace
    //      contains recipient
    @Test
    public void testParallelBasic() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob@home|milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Parallel(new Recipient("bob@home"), new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        final ListExpression parseExpression2 = ListExpression.parse("    bob@home  |  milk@juice   ");
        assertConsistent(parseExpression2);
        assertEquals(parseExpression2, genExpression);
    }
    
    // Tests
    //   Parallel
    //      contains intersections
    @Test
    public void testParallelWithIntersection() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice*jane@bird|milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Parallel(new Intersection(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   Parallel
    //      contains union
    @Test
    public void testParallelWIthUnion() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice,jane@bird|milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Parallel(new Union(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);        
    }
    
    // Tests
    //   Parallel
    //      contains difference
    @Test
    public void testParallelWithDifference() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice!jane@bird|milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Parallel(new Difference(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //  Parallel
    //      parenthesis difference
    @Test
    public void testParallelWithParenthesis() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("(milk@juice,jane@bird)|milk@juice");
        final ListExpression parseExpression2 = ListExpression.parse("milk@juice,(jane@bird|milk@juice)");
        assertConsistent(parseExpression);
        assertConsistent(parseExpression2);
        final ListExpression genExpression = new Parallel(new Union(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        assertNotEquals(parseExpression2, genExpression);
    }
    
 // Tests
    //   Parallel
    //      contains sequence
    @Test
    public void testParallelWithSequence() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("jam@apple;big@small|milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Parallel(new Sequence(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   Parallel
    //      contains parallel in parenthesis
    @Test
    public void testParallelWithParallel() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple|big@small)|milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Parallel(new Parallel(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   parallel intersection
    //      contains definition in parenthesis
    //      contains listname
    @Test
    public void testParallelWithDefinition() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(apple = big@small)|milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Parallel(new Definition(new ListName("apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
 // Tests
    //   sequence
    //      w & w/o whitespace
    //      contains recipient
    @Test
    public void testSequenceBasic() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("bob@home;milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Sequence(new Recipient("bob@home"), new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        final ListExpression parseExpression2 = ListExpression.parse("    bob@home  ;  milk@juice   ");
        assertConsistent(parseExpression2);
        assertEquals(parseExpression2, genExpression);
    }
    
    // Tests
    //   sequence
    //      contains intersections
    @Test
    public void testSequenceWithIntersection() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice*jane@bird;milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Sequence(new Intersection(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   sequence
    //      contains union
    @Test
    public void testSequenceWIthUnion() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice,jane@bird;milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Sequence(new Union(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);        
    }
    
    // Tests
    //   sequence
    //      contains difference
    @Test
    public void testSequenceWithDifference() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("milk@juice!jane@bird;milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Sequence(new Difference(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //  sequence
    //      parenthesis difference
    @Test
    public void testSequenceWithParenthesis() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("(milk@juice,jane@bird);milk@juice");
        final ListExpression parseExpression2 = ListExpression.parse("milk@juice,(jane@bird;milk@juice)");
        assertConsistent(parseExpression);
        assertConsistent(parseExpression2);
        final ListExpression genExpression = new Sequence(new Union(new Recipient("milk@juice"), new Recipient("jane@bird")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
        assertNotEquals(parseExpression2, genExpression);
    }
    
 // Tests
    //   sequence
    //      contains sequence
    @Test
    public void testSequenceWithSequence() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("jam@apple;big@small;milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Sequence(new Sequence(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   sequence
    //      contains parallel in parenthesis
    @Test
    public void testSequenceWithParallel() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(jam@apple|big@small);milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Sequence(new Parallel(new Recipient("jam@apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //   sequence intersection
    //      contains definition in parenthesis
    //      contains listname
    @Test
    public void testSequenceWithDefinition() throws ParallelDefinitionException {
        final ListExpression parseExpression =  ListExpression.parse("(apple = big@small);milk@juice");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Sequence(new Definition(new ListName("apple"), new Recipient("big@small")),
                new Recipient("milk@juice"));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //  precedence
    //      difference has priority over Union
    //      
    @Test
    public void testPriorityIntersectionOverDiff() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c! a*b");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Difference(new ListName("c"), new Intersection(
                new ListName("a"), new ListName("b"))); 
        assertEquals(parseExpression, genExpression);      
    }
    
    // Tests
    //  precedence
    //      difference has priority over Union
    //      
    @Test
    public void testPriorityDiffOverUnion() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c, a!b");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Union(new ListName("c"), new Difference(
                new ListName("a"), new ListName("b"))); 
        assertEquals(parseExpression, genExpression);      
    }
    
    // Tests
    //  precedence
    //      union has priority over definition
    //      
    @Test
    public void testPriorityUnionOverDef() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c = a,b");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("c"), new Union(
                new ListName("a"), new ListName("b")));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //  precedence
    //      definition has priority over sequence
    //      
    @Test
    public void testPriorityDefOverSeq() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c; a = b");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Sequence(new ListName("c"), new Definition(
                new ListName("a"), new ListName("b")));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //  precedence
    //      sequence has priority over parallel
    //      
    @Test
    public void testPrioritySeqOverParallel() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c| a ; b");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Parallel(new ListName("c"), new Sequence(
                new ListName("a"), new ListName("b")));
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    //  precedence
    //      Reversed precedence for definition
    //      
    @Test
    public void testPriorityDefOverDef() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c= a = b");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("c"), new Definition(
                new ListName("a"), new ListName("b")));
        assertEquals(parseExpression, genExpression);
    }


    // Tests
    // invalids
    //      characters that don't work
    @Test
    public void testInvalid() {
        List<String> invalidInputs = List.of(
                "+", "%=%", "%,%"
        );

        for (String invalidInput : invalidInputs) {
            assertThrows(IllegalArgumentException.class, () -> ListExpression.parse(invalidInput));
        }
    }
    
    
    // Tests
    // empty
    //      inside definition
    @Test
    public void testEmptyInDefinition() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c= ");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Definition(new ListName("c"), new Empty());
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    // empty
    //      inside sequence
    @Test
    public void testEmptyInSequence() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c; ");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Sequence(new ListName("c"), new Empty());
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    // empty
    //      inside difference
    @Test
    public void testEmptyInDifference() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c! ");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Difference(new ListName("c"), new Empty());
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    // empty
    //      inside union
    @Test
    public void testEmptyInUnion() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c, ");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Union(new ListName("c"), new Empty());
        assertEquals(parseExpression, genExpression);
    }
    
    // Tests
    // empty
    //      inside parallel
    @Test
    public void testEmptyInParallel() throws ParallelDefinitionException {
        final ListExpression parseExpression = ListExpression.parse("c| ");
        assertConsistent(parseExpression);
        final ListExpression genExpression = new Parallel(new ListName("c"), new Empty());
        assertEquals(parseExpression, genExpression);
    }
    
}
