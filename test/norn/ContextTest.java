package norn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class ContextTest {
    //  Tests for Context methods, excluding evaluate(), which is tested in ListExpressionEvaluateTest.java.
    //  Further (indirect) testing of these getRelevantExpression(), updateMap(), and getDependencies() is
    //  done in ListExpressionEvaluateTest.java, as these methods are only used in the evaluate methods of 
    //  the ListExpression variants.
    //
    //  Partitioned as follows:
    //      getRelevantExpression()
    //          listname has been defined already, listname has not been defined yet
    //          listname's definition uses itself, doesn't use itself
    //      updateMap()
    //          listname has been defined already, listname has not been defined yet
    //          expr contains a mail loop, doesn't contain a mail loop
    //          expr is a recursive redefinition, or is not a recursive redefinition
    //      getDependencies()
    //          listname is not dependent on any listname, is only dependent on its direct
    //              children, or is dependent on listnames that are not its direct children
    //          listname is dependent on itself, not dependent on itself
    //      save()
    //          save a context with 0, 1, >1 definitions
    //      load()
    //          load a String that can be parsed, can't be parsed
    //              if it can be parsed, it can either be able to be evaluated or not be
    //              able to be evaluated
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    // Covers: getRelevantExpression()
    //              listname has not yet been defined
    //              listname has been defined already
    //              listname's definition uses itself
    //              listname's definition does not use itself
    //              expr does not contain a mail loop
    @Test
    public void testGetRelevantExpression() throws EvaluationException {
        Context context = new Context();
        ListExpression expr1 = ListExpression.parse("a=a@mit");
        context.evaluate(expr1);
        assertEquals(ListExpression.parse("a@mit"), context.getRelevantExpression(new ListName("a")));
        ListExpression expr2 = ListExpression.parse("a=b@mit");
        context.evaluate(expr2);
        assertEquals(ListExpression.parse("b@mit"), context.getRelevantExpression(new ListName("a")));
        ListExpression expr3 = ListExpression.parse("a=a,c@mit");
        context.evaluate(expr3);
        assertEquals(ListExpression.parse("b@mit, c@mit"), context.getRelevantExpression(new ListName("a")));
    }
    
    // Covers: updateMap()
    //              listname has not yet been defined
    //              listname has been defined already
    //              expr is not a recursive redefinition
    //              expr is a recursive redefinition
    @Test
    public void testUpdateMap() throws EvaluationException {
        Context context = new Context();
        ListExpression expr1 = ListExpression.parse("a@mit, b@mit");
        ListName a = new ListName("a");
        context.updateMap(a, expr1);
        assertEquals(ListExpression.parse("a@mit, b@mit"), context.getRelevantExpression(a));
        ListExpression expr2 = ListExpression.parse("a, c@mit");
        ListExpression newExpr = expr2.getUpdatedExpression(context, a);
        context.updateMap(a, newExpr);
        assertEquals(ListExpression.parse("a@mit, b@mit, c@mit"), context.getRelevantExpression(a));
    }
    
    // Covers: updateMap()
    //              expr contains a mail loop
    @Test
    public void testUpdateMapMailLoop() throws EvaluationException {
        Context context = new Context();
        ListName a = new ListName("a");
        context.updateMap(a, ListExpression.parse("b"));
        assertEquals(ListExpression.parse("b"), context.getRelevantExpression(a));
        ListName b  = new ListName("b");
        assertThrows(MailLoopException.class, () -> context.updateMap(b, ListExpression.parse("a")));
    }
    
    // Covers: getDependencies()
    //              listname is not dependent on any listname
    //              listname is only dependent on its direct children
    //              listname is dependent on itself
    //              listname is not dependent on itself
    @Test
    public void testGetDependenciesRecursiveRedefinition() throws EvaluationException {
        Context context = new Context();
        ListName a = new ListName("a");
        ListExpression expr1 = ListExpression.parse("a=a@mit");
        context.evaluate(expr1);
        assertEquals(Set.of(), context.getDependencies(a));
        ListExpression expr2 = ListExpression.parse("a=a,b,c");
        context.evaluate(expr2);
        assertEquals(Set.of(new ListName("b"), new ListName("c")), context.getDependencies(a));
    }
    
    // Covers: getDependencies()
    //              listname is dependent on listnames that are not its direct children
    @Test
    public void testGetDependenciesIndirectDependencies() throws EvaluationException {
        Context context = new Context();
        ListName a = new ListName("a");
        ListName b = new ListName("b");
        ListExpression expr1 = ListExpression.parse("a=c");
        context.evaluate(expr1);
        assertEquals(Set.of(new ListName("c")), context.getDependencies(a));
        ListExpression expr2 = ListExpression.parse("b=a");
        context.evaluate(expr2);
        assertEquals(Set.of(new ListName("a"), new ListName("c")), context.getDependencies(b));
    }
    
    // Covers: save()
    //              context with 0 definitions
    @Test
    public void testSaveZeroDefinitions() {
        Context context = new Context();
        assertEquals("", context.save());
    }
    
    // Covers: save()
    //              context with 1 definition
    //              context with >1 definition
    @Test
    public void testSaveOneDefinition() throws EvaluationException {
        Context context = new Context();
        ListExpression expr1 = ListExpression.parse("a=a@mit");
        context.evaluate(expr1);
        assertEquals("a=a@mit", context.save());
        ListExpression expr2 = ListExpression.parse("b=b@mit");
        context.evaluate(expr2);
        String saved = context.save();
        assert(saved.contains(new StringBuilder("a=a@mit")));
        assert(saved.contains(new StringBuilder("b=b@mit")));
    }
    
    // Covers: load()
    //              load a string that can be parsed and evaluated
    @Test
    public void testLoadParsableAndEvaluatable() throws EvaluationException, UnableToLoadException {
        Context context = new Context();
        context.load("a=a@mit; b=b@mit");
        assertEquals(ListExpression.parse("a@mit"), context.getRelevantExpression(new ListName("a")));
        assertEquals(ListExpression.parse("b@mit"), context.getRelevantExpression(new ListName("b")));
    }
    
    // Covers: load()
    //              load a string that can be parsed and evaluated
    @Test
    public void testLoadParsableButNotEvaluatable() throws EvaluationException, UnableToLoadException {
        Context context = new Context();
        assertThrows(UnableToLoadException.class, () -> context.load("a=b; b=a"));
    }
    
    // Covers: load()
    //              load a string that can't be parsed
    @Test
    public void testLoadNotParsable() throws EvaluationException, UnableToLoadException {
        Context context = new Context();
        assertThrows(UnableToLoadException.class, () -> context.load("a=a@"));
    }
    
}
