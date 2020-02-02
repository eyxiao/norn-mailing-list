package norn;

import java.util.HashSet;
import java.util.Set;

public class Definition implements ListExpression {
    private final ListName listName;
    private final ListExpression expr;
    
    // AF(listName, expr) = a definition operation, with listName being defined as the set
    //                          of recipients that the list expression expr evaluates to.
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
     * Public constructor for Definition.
     * @param listName name for the expression
     * @param expr expression
     */
    public Definition(ListName listName, ListExpression expr) {
        this.listName = listName;
        this.expr = expr;
    }

    @Override
    public Set<String> evaluate(Context context) throws EvaluationException {
        ListExpression newExpr = expr.getUpdatedExpression(context, listName);
        context.updateMap(listName, newExpr);
        return listName.evaluate(context);
    }
    
    @Override
    public ListExpression getUpdatedExpression(Context context, ListName listname) {
        return new Definition(listName, expr.getUpdatedExpression(context, listname));
    }
    
    @Override
    public Set<ListName> getDefinedListNames() {
        Set<ListName> s1 = new HashSet<>(Set.of(listName));
        Set<ListName> s2 = expr.getDefinedListNames();
        s1.addAll(s2);
        return s1;
    }
    
    @Override
    public Set<ListName> getAllListNames() {
        Set<ListName> s1 = new HashSet<>(Set.of(listName));
        Set<ListName> s2 = expr.getAllListNames();
        s1.addAll(s2);
        return s1;
    }
    
    @Override
    public Set<ListName> getListNamesNotDefined() {
        return expr.getListNamesNotDefined();
    }

    @Override
    public VisualizerTree getVisualizerTree() {
        final VisualizerTree tree = new VisualizerTree(this.listName + " = ");
        tree.addChild(expr.getVisualizerTree());
        return tree;
    }

    //////////////////// Equality and toString methods ///////////////////
    
    @Override
    public boolean equals(Object that) {
        return that instanceof Definition && sameValue((Definition) that);
    }
    
    private boolean sameValue(Definition that) {
        return this.expr.equals(that.expr) && this.listName.equals(that.listName);
    }
    
    @Override
    public int hashCode() {
        return listName.hashCode() + expr.hashCode();
    }
    
    @Override
    public String toString() {
        return "(" + listName.toString() + "=" + expr.toString() + ")";
    }
}
