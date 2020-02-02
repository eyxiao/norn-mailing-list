package norn;

import java.util.Set;

public class Sequence implements ListExpression {
    private final ListExpression expr1;
    private final ListExpression expr2;
    
    // AF(expr1, expr2) = a sequence operation that represents recipients produced by
    //                      expr2 after substituting the expressions of all named list 
    //                      definitions found in expr1
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
     * Public constructor for Sequence.
     * @param expr1 first expression in sequence
     * @param expr2 second expression in sequence
     */
    public Sequence(ListExpression expr1, ListExpression expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    
    @Override
    public Set<String> evaluate(Context context) throws EvaluationException  {
        expr1.evaluate(context);
        return expr2.evaluate(context);
    }
    
    @Override
    public ListExpression getUpdatedExpression(Context context, ListName listname) {
        return new Sequence(expr1.getUpdatedExpression(context, listname), expr2.getUpdatedExpression(context, listname));
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
        final VisualizerTree tree = new VisualizerTree("sequence");
        tree.addChild(expr1.getVisualizerTree());
        tree.addChild(expr2.getVisualizerTree());
        return tree;
    }

    ///////////////////// Equality and toString methods ///////////////////////
            
    @Override
    public boolean equals(Object that) {
        return that instanceof Sequence && sameValue((Sequence) that);
    }
    
    private boolean sameValue(Sequence that) {
        return this.expr1.equals(that.expr1) && this.expr2.equals(that.expr2);
    }
    
    @Override
    public int hashCode() {
        return expr1.hashCode() + expr2.hashCode();
    }
    
    @Override
    public String toString() {
        return "(" + expr1.toString() + ";" + expr2.toString() + ")";
    }
}
