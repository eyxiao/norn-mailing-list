package norn;

import java.util.*;
import java.util.stream.Collectors;


public class Context {
    private final Map<ListName, ListExpression> expressionMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<ListName, Set<ListName>> childMap = Collections.synchronizedMap(new HashMap<>()); // maps a listname to all of the listnames it's dependent on

    // AF(expressionMap, childMap) = a context of already evaluated list definitions, where all of the
    //                              listnames that have been previously defined are the keys in expressionMap, 
    //                              and each value is the corresponding listname's most recent definition. The 
    //                              listnames' dependencies on one another are represented in childMap, where 
    //                              each key is a listname and the associated value is a set of all of the listnames
    //                              it is directly dependent on
    //
    // RI:
    //  - all listnames in expressionMap, including all those found in the expressions at expressionMap.get(key)
    //      for all keys in expressionMap, should be keys in childMap
    //  - all listnames found in childMap.get(key) for all keys in childMap should also be keys in childMap
    //
    // SRE:
    //  - all fields are private and final
    //  - none of the methods return references to any fields
    //
    // TSA:
    //  - expressionMap and childMap are both threadsafe datatypes
    //  - expressionMap and childMap are never exposed to clients, and childMap's mutable values
    //      are also never exposed to clients
    //  - Context implements the monitor pattern so no two threads are reading or writing to the same
    //      context object at the same time -- evaluate() is a wrapper for the ListExpression method evaluate()
    //  - no two threads modify (e.g. add or change definitions) list definitions that any other thread depends
    //      on, uses, or defines, so interleaving context operations are still threadsafe.

    /**
     * Public SequentialContext constructor.
     */
    public Context() {
        checkRep();
    }
    
    // check representation invariant
    private synchronized void checkRep() {
        for (ListName l : expressionMap.keySet()) {
            assert childMap.containsKey(l); 
        }
        for (ListName l : childMap.keySet()) {
            for (ListName child : childMap.get(l)) {
                assert childMap.containsKey(child);
            }
        }
    }

    /**
     * Get the list expression that listname is currently defined as.
     *
     * @param listname listname to get the expression for
     * @return expression corresponding to the listname
     */
    public synchronized ListExpression getRelevantExpression(ListName listname) {
        if (expressionMap.containsKey(listname)) {
            return expressionMap.get(listname);
        } else {
            return new Empty();
        }
    }

    /**
     * Update the list expression for an already defined listname, or add a definition for a listname
     * that has not been defined yet.
     *
     * @param listname to update or add the definition for
     * @param expr     the new list expression definition of listname
     * @throws MailLoopException if there is a mailing loop introduced by evaluating the expression
     */
    public synchronized void updateMap(ListName listname, ListExpression expr) throws MailLoopException {
        // add to childMap
        if (!childMap.containsKey(listname)) {
            childMap.put(listname, new HashSet<>());
        }

        Set<ListName> previousChildren = new HashSet<>(childMap.get(listname)); // copy and store previous children
        List<ListName> childAdded = new ArrayList<>();                          // in case we need to revert

        Set<ListName> allListNamesInExpression = expr.getAllListNames();
        if (allListNamesInExpression.contains(listname)) {
            childMap.get(listname).addAll(allListNamesInExpression);
        } else {
            childMap.put(listname, allListNamesInExpression);
        }

        // add nodes in childMap for list names in allListNamesInExpression that don't exist yet
        for (ListName l : allListNamesInExpression) {
            if (!childMap.containsKey(l)) {
                childMap.put(l, new HashSet<>());       // no children yet
                childAdded.add(l);
            }
        }

        // check for mail loop
        if (findLoop(listname)) {                       // revert back to previous context state
            childMap.put(listname, previousChildren);
            for (ListName child : childAdded) {
                childMap.remove(child);
            }
            throw new MailLoopException("Mailing list expression has a loop.");
        } else {
            expressionMap.put(listname, expr);
        }
        checkRep();
    }

    /**
     * Find a mailing loop if it exists in the currently evaluated expressions.
     *
     * @param listname listname to find loop for
     * @return true if there is a loop, false otherwise
     */
    private synchronized boolean findLoop(ListName listname) {
        Set<ListName> visited = new HashSet<>();
        for (ListName child : childMap.get(listname)) {
            if (child.equals(listname)) {
                continue;       // listname is redefined with itself, which is ok
            }
            if (findLoopHelper(listname, child, visited)) {
                return true;
            }

        }
        return false;
    }

    // Helper function for updateMap DFS
    private synchronized boolean findLoopHelper(ListName root, ListName listname, Set<ListName> visited) {
        visited.add(listname);
        for (ListName child : childMap.get(listname)) {
            if (child.equals(root)) {
                return true;
            } else {
                if (!visited.contains(child)) {
                    if (findLoopHelper(root, child, visited)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get the listnames that listname depends on.
     *
     * @param listname listname to get dependencies for
     * @return set of listnames that listname depends on
     */
    public synchronized Set<ListName> getDependencies(ListName listname) {
        Set<ListName> dependencies = new HashSet<>();
        if (!childMap.containsKey(listname)) {
            return dependencies;
        }
        for (ListName dependency : childMap.get(listname)) {
            getDependenciesHelper(dependency, dependencies);
        }
        return dependencies;
    }

    // Helper method for getDependencies
    private synchronized void getDependenciesHelper(ListName listname, Set<ListName> visited) {
        visited.add(listname);
        for (ListName child : childMap.get(listname)) {
            if (!visited.contains(child)) {
                getDependenciesHelper(child, visited);
            }
        }
    }

    /**
     * Returns a set of recipients that expr evaluates to as defined in the spec of the evaluate() method in
     * ListExpression.java. If an EvaluationException occurs while evaluating expr, this method will ensure
     * that the Context object associated with expr will still contain all past succesfully evaluated expressions,
     * and none of the expressions evaluated in the expression that caused the EvaluationException.
     * @param expr list expression to evaluate
     * @return set of recipients that expr evaluates to as defined in the spec of the evaluate()
     *      method in ListExpression.java
     * @throws EvaluationException if evaluating expr throws an EvaluationException (i.e. if there's a mail loop
     *      or something wrong with the parallel definition)
     */
    public Set<String> evaluate(ListExpression expr) throws EvaluationException {
        String prevState = save();
        try {
            return expr.evaluate(this);
        } catch (EvaluationException e) {
            // If EvaluationException occurs, we load our previous state
            expressionMap.clear();
            childMap.clear();
            try {
                load(prevState);
            } catch (UnableToLoadException ex) {
                throw new AssertionError("Load failed on saved state; load/save not consistent.");
            }
            throw e;
        }
    }

    /**
     * Saves defined listName definitions to a String
     *
     * @return a String containing all defined ListNames as a parseable expression
     */
    public synchronized String save() {
        return expressionMap.entrySet().stream().map(
                entry -> entry.getKey() + "=" + entry.getValue()
        ).collect(Collectors.joining(";"));
    }

    /**
     * Generates a new Context containing listName definitions to the console
     *
     * @param loader a string containing defined listNames
     * @throws UnableToLoadException if string is not parseable or cannot be evaluated properly.
     */
    public synchronized void load(String loader) throws UnableToLoadException {
        try {
            ListExpression expr = ListExpression.parse(loader);
            expr.evaluate(this);
        } catch (EvaluationException | IllegalArgumentException e) {
            throw new UnableToLoadException(e);
        }
    }
    
    @Override
    public synchronized String toString() {
        return "expressionMap: " + expressionMap.toString() + ", childMap: " + childMap.toString();
    }

}