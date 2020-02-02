package norn;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable Recipient class.
 */
public class Recipient implements ListExpression {
    private final String recipient;
    
    // AF(recipient) = a case-insensitive email address that follows username@domain,
    //                  where recipient is the email address in all lowercase characters
    //
    // RI:
    //  - recipient follow username@domain, where:
    //      - username ::= [A-Za-z0-9_.+-]+
    //      - domain ::= [A-Za-z0-9_.-]+
    //  - recipient is lowercase
    //
    // SRE:
    //  - all fields are private, final, and immutable
    //  - none of the methods return references to any fields
    //
    // TSA:
    //  - all fields are final and immutable
    
    // check rep invariant
    private void checkRep() {
        assert recipient.matches("([A-Za-z0-9_.+-]+)@([A-Za-z0-9_.-]+)");
        assert recipient.toLowerCase().equals(recipient);
    }
    
    /**
     * Public constructor for Recipient.
     * @param recipient name of recipient
     */
    public Recipient(String recipient) {
        this.recipient = recipient.toLowerCase();
        checkRep();
    }

    @Override
    public Set<String> evaluate(Context context) {
        return new HashSet<>(Collections.singleton(recipient));
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
        return new VisualizerTree(recipient);
    }

    ///////////////////// Equality and toString() methods //////////////////////
    
    @Override
    public boolean equals(Object that) {
        return that instanceof Recipient && sameValue((Recipient) that);
    }
    
    private boolean sameValue(Recipient that) {
        return this.recipient.equals(that.recipient);
    }
    
    @Override
    public int hashCode() {
        return recipient.hashCode();
    }
    
    @Override
    public String toString() {
        return recipient;
    }
}
