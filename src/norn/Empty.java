package norn;

import java.util.HashSet;
import java.util.Set;

/**
 * Immutable Recipient class.
 */
public class Empty implements ListExpression {

    // AF() = an empty list expression
    //
    // RI:
    //  - true
    //
    // SRE:
    //  - No fields
    //
    // TSA:
    //  - No fields


    /**
     * Public constructor for Empty.
     */
    public Empty() {
        
    }

    @Override
    public Set<String> evaluate(Context conext) {
        return new HashSet<>();
    }
    
    @Override
    public ListExpression getUpdatedExpression(Context context, ListName listname) {
        return this;
    }
    
    @Override
    public Set<ListName> getDefinedListNames() {
        return new HashSet<>();
    }
    
    @Override
    public Set<ListName> getAllListNames() {
        return new HashSet<>();
    }
    
    @Override
    public Set<ListName> getListNamesNotDefined() {
        return new HashSet<>();
    }

    @Override
    public VisualizerTree getVisualizerTree() {
        return new VisualizerTree("∅");
    }

    ///////////////////// Equality and toString() methods //////////////////////

    @Override
    public boolean equals(Object that) {
        return that instanceof Empty;
    }

    @Override
    public int hashCode() {
        return "".hashCode();
    }

    @Override
    public String toString() {
        return "( )";
    }
}
