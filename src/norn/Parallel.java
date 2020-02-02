package norn;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Parallel implements ListExpression {
    private ListExpression expr1;
    private ListExpression expr2;
    
    // AF(expr1, expr2) = a parallel operation, where expr1 and expr2 are list expressions 
    //                      evaluated in parallel, and the parallel operation itself evaluates
    //                      to the empty set, but evaluations of expr1 and expr2 can be used
    //                      in later parts of the list expression.
    //
    // RI: 
    //  - true
    //
    // SRE:
    //  - all fields are private, final, and immutable
    //  - none of the methods return references to any fields
    //
    // TSA:
    //  - all fields are final and immutable
    
    
    /**
     * Public constructor for Parallel.
     * @param expr1 first subexpression in the parallel operation
     * @param expr2 second subexpression in the parallel operation
     */
    public Parallel(ListExpression expr1, ListExpression expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    
    @Override
    public Set<String> evaluate(Context context) throws EvaluationException {
        // check for parallel condition
        // check for expr1:
        Set<ListName> allListNames1 = new HashSet<>();
        Set<ListName> allDirectListNames1 = expr1.getAllListNames();
        for (ListName listname : allDirectListNames1) {
            allListNames1.add(listname);
            allListNames1.addAll(context.getDependencies(listname));
        }
        Set<ListName> definedListNames2 = expr2.getDefinedListNames();
        if (!Collections.disjoint(allListNames1, definedListNames2)) {
            throw new ParallelDefinitionException("Parallel subexpressions must not define any "
                    + "list names that also appear directly or indirectly in the other subexpression.");
        }
        // check for expr2:
        Set<ListName> allListNames2 = new HashSet<>();
        Set<ListName> allDirectListNames2 = expr2.getAllListNames();
        for (ListName listname : allDirectListNames2) {
            allListNames2.add(listname);
            allListNames2.addAll(context.getDependencies(listname));
        }
        Set<ListName> definedListNames1 = expr1.getDefinedListNames();
        if (!Collections.disjoint(allListNames2, definedListNames1)) {
            throw new ParallelDefinitionException("Parallel subexpressions must not define any "
                    + "list names that also appear directly or indirectly in the other subexpression.");
        }


        ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
        final Future<Set<String>> result = threadExecutor.submit(() -> expr2.evaluate(context));

        expr1.evaluate(context);

        try {
            result.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            assert e.getCause() instanceof EvaluationException;
            throw (EvaluationException) e.getCause();
        }
        
        return new HashSet<>();
    }
    
    @Override
    public ListExpression getUpdatedExpression(Context context, ListName listname) {
        return new Parallel(expr1.getUpdatedExpression(context, listname), expr2.getUpdatedExpression(context, listname));
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
        final VisualizerTree tree = new VisualizerTree("parallel");
        tree.addChild(expr1.getVisualizerTree());
        tree.addChild(expr2.getVisualizerTree());
        return tree;
    }

    ////////////////////Equality and toString methods ///////////////////
        
    @Override
    public boolean equals(Object that) {
        return that instanceof Parallel && sameValue((Parallel) that);
    }
    
    private boolean sameValue(Parallel that) {
        return this.expr1.equals(that.expr1) && this.expr2.equals(that.expr2);
    }
    
    @Override
    public int hashCode() {
        return expr1.hashCode() + expr2.hashCode();
    }
    
    @Override
    public String toString() {
        return "(" + expr1.toString() + " | " + expr2.toString() + ")";
    }

}