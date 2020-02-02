package norn;

import java.util.Set;

import edu.mit.eecs.parserlib.UnableToParseException;

public interface ListExpression {
    //  Datatype definition:
    //  ListExpression = Recipient(recipient: String)
    //                   + ListName(listName: String)
    //                   + Empty()
    //                   + Union(expr1: ListExpression, expr2: ListExpression)
    //                   + Intersection(expr1: ListExpression, expr2: ListExpression)
    //                   + Difference(expr1: ListExpression, expr2: ListExpression)
    //                   + Parallel(expr1: ListExpression, expr2: ListExpression)
    //                   + Sequence(expr1: ListExpression, expr2: ListExpression)
    //                   + Definition(listName: ListName, expr: ListExpression)

    /**
     * Parse a list expression.
     *
     * @param input expression to parse, as defined in the Norn handout
     * @return expression AST for the input
     * @throws IllegalArgumentException if the expression is syntactically invalid.
     */
    static ListExpression parse(String input)  {
        try {
            return ListExpressionParser.parse(input);
        } catch (UnableToParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * @return a parsable representation of this expression, such that
     * for all e:ListExpression, e.equals(ListExpression.parse(e.toString())).
     * Furthermore, the repeated application of parse and toString is idempotent:
     * e.toString().equals(ListExpression.parse(e.toString()).toString())
     */
    @Override
    String toString();

    /**
     * @param that any object
     * @return true if and only if this and that are structurally-equal
     * ListExpressions, where structural equality is defined as followed:
     *     1. the expressions contain the same recipients and operators
     *     2. the recipients and operators are in the same order, read left-to-right
     *     3. they are grouped in the same way
     */
    @Override
    boolean equals(Object that);

    /**
     * @return hash code value consistent with the equals() definition of structural
     * equality, such that for all e1,e2:Expression,
     * e1.equals(e2) implies e1.hashCode() == e2.hashCode()
     */
    @Override
    int hashCode();

    /**
     * Evaluate a list expression according to the Norn spec in the project handout.
     * 
     * Valid operators and what evaluate() should return for each of them are listed below 
     * from greatest to least precedence:
     *  1. Set intersection (e*f): returns recipients in both e and f 
     *  2. Set difference (e!f): returns recipients in e but not in f
     *  3. Set union (e,f): returns recipients in either e or f
     *  4. List definition (listname=e): returns the set of recipients of e, and the 
     *      definition is saved in the context if it is used again in the future
     *  5. Sequence of list expressions (e;f): returns the recipients produced by f
     *      after substituting the expressions of all named list definitions found in e
     *  6. Parallel (e|f): evaluates e and f so that their list definitions may be used
     *      elsewhere and returns the empty set of recipients
     *
     *  If a definition exists within this ListExpression, the context will be modified.
     *  To ensure that if errors occur during the evaluation of this exception that the
     *  context will remain unmodified, context.evaluate should be called instead with this
     *  expression.
     *      
     * @param context context with previously defined list names
     * @return the set of all recipients (in lowercase) which this mailing list would email.
     * @throws EvaluationException if there is an error while evaluating (e.g. mailing loops,
     *          error in defining parallel when a subexpression defines a listname that appears
     *          directly or indirectly in the other subexpression)
     */
    Set<String> evaluate(Context context) throws EvaluationException;
    
    /**
     * Get a new updated ListExpression object, which is a copy of the original ListExpression
     * but with the most recent definition of listname (pulled from the context) plugged into
     * wherever listname appears in the original ListExpression. If listname does not appear in
     * the original ListExpression, then returns a copy of the same original ListExpression.
     * @param context context storing already evaluated and stored list definitions
     * @param listname listname to replace in the ListExpression
     * @return ListExpression object with most recent definition of listname plugged in
     */
    ListExpression getUpdatedExpression(Context context, ListName listname);
    
    /**
     * Get the list names defined in the list expression (does not include list names
     * that are not defined in but are used in the list expression)
     * @return the set of listnames that are defined in the list expression
     */
    Set<ListName> getDefinedListNames();
    
    /**
     * Get all list names that are defined or used in the list expression.
     * @return the set of listnames that are defined or used in the list expression
     */
    Set<ListName> getAllListNames();
    
    /**
     * Get the list names that are in the right hand side of a definition (used but not
     * defined in a list expression)
     * @return the set of listnames that are used but not defined in a list expression
     */
    Set<ListName> getListNamesNotDefined();

    /**
     * Create a VisualizerTree of the given Expression
     * @return a VisualizerTree of the given Expression
     */
    VisualizerTree getVisualizerTree();
}
