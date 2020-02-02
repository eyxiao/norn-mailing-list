package norn;

import java.util.HashSet;
import java.util.Set;


public class ListName implements ListExpression {
    private final String listName;
    
    // AF(listName) = the case-insensitive name of a mailing list, where
    //                  listName is the name of the mailing list in all lowercase
    //
    // RI: 
    //  - listName has to follow this grammar: [A-Za-z0-9_.-]+
    //  - listName is lowercase
    //
    // SRE:
    //  - all fields are private, final, and immutable
    //  - none of the methods return references to any fields
    //
    // TSA:
    //  - all fields are final and immutable
    
    /**
     * Public constructor for ListName.
     * @param listName name of list
     */
    public ListName(String listName) {
        this.listName = listName.toLowerCase();
        checkRep();
    }
    
    // check rep invariant
    private void checkRep() {
        assert listName.matches("[A-Za-z0-9_.-]+");
        assert listName.toLowerCase().equals(listName);
    }

    @Override
    public Set<String> evaluate(Context context) throws EvaluationException  {
        ListExpression expression = context.getRelevantExpression(this);
        return expression.evaluate(context);
    }
    
    @Override
    public ListExpression getUpdatedExpression(Context context, ListName listname) {
        if (this.equals(listname)) {
            return context.getRelevantExpression(listname);
        } else {
            return this;
        }
    }
    
    @Override
    public Set<ListName> getDefinedListNames() {
        return new HashSet<>();
    }
    
    @Override
    public Set<ListName> getAllListNames() {
        return new HashSet<>(Set.of(this));
    }
    
    @Override
    public Set<ListName> getListNamesNotDefined() {
        return new HashSet<>(Set.of(this));
    }

    @Override
    public VisualizerTree getVisualizerTree() {
        return new VisualizerTree(listName);
    }

    ////////////////////// Equality and toString methods ///////////////////////
    
    @Override
    public boolean equals(Object that) {
        return that instanceof ListName && sameValue((ListName)that);
    }
    
    private boolean sameValue(ListName that) {
        return this.listName.equals(that.listName);
    }
    
    @Override
    public int hashCode() {
        return this.listName.hashCode();
    }
    
    @Override
    public String toString() {
        return listName;
    }
    
}
