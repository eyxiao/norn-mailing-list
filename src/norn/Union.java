package norn;

import java.util.Set;

/**
 * Immutable Union class.
 */
public class Union implements ListExpression {
    private final ListExpression expr1;
    private final ListExpression expr2;
    
    // AF(expr1, expr2) = the set union operation applied on expr1 and expr2
    //
    // RI: true
    //
    // SRE:
    //  - all fields are private, final, and immutable
    //  - none of the methods return references to any fields
    //
    // TSA:
    //  - all fields are final and immutable

    /**
     * Public constructor for Union.
     * @param expr1 first expression in union operation
     * @param expr2 second expression in union operation
     */
    public Union(ListExpression expr1, ListExpression expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    @Override
    public Set<String> evaluate(Context context) throws EvaluationException  {
        Set<String> s1 = expr1.evaluate(context);
        Set<String> s2 = expr2.evaluate(context);
        s1.addAll(s2);
        return s1;
    }
    
    @Override
    public ListExpression getUpdatedExpression(Context context, ListName listname) {
        return new Union(expr1.getUpdatedExpression(context, listname), expr2.getUpdatedExpression(context, listname));
    }
    
    @Override
    public Set<ListName> getDefinedListNames() {
        Set<ListName> s1 = expr1.getDefinedListNames();
        Set<ListName> s2 = expr2.getDefinedListNames();
        s1.addAll(s2);
        return s1;
    }
    
    @Override
    public Set<ListName> getAllListNames() {
        Set<ListName> s1 = expr1.getAllListNames();
        Set<ListName> s2 = expr2.getAllListNames();
        s1.addAll(s2);
        return s1;
    }
    
    @Override
    public Set<ListName> getListNamesNotDefined() {
        Set<ListName> s1 = expr1.getListNamesNotDefined();
        Set<ListName> s2 = expr2.getListNamesNotDefined();
        s1.addAll(s2);
        return s1;
    }

    @Override
    public VisualizerTree getVisualizerTree() {
        final VisualizerTree tree = new VisualizerTree("∪");
        tree.addChild(expr1.getVisualizerTree());
        tree.addChild(expr2.getVisualizerTree());
        return tree;
    }

    ///////////////////// Equality and toString() methods //////////////////////
    
    @Override
    public boolean equals(Object that) {
        return that instanceof Union && sameValue((Union) that);
    }
    
    private boolean sameValue(Union that) {
        return this.expr1.equals(that.expr1) && this.expr2.equals(that.expr2);
    }
    
    @Override
    public int hashCode() {
        return expr1.hashCode() + expr2.hashCode();
    }
    
    @Override
    public String toString() {
        return "(" + expr1.toString() + "," + expr2.toString() + ")";
    }
}
