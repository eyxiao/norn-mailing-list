package norn;

public class ParallelDefinitionException extends EvaluationException {
    /**
     * ParallelDefinitionException constructor. This exception is thrown when a 
     * subexpression in a parallel list expression depends on a listname that appears
     * directly or indirectly in the other subexpression (e.g. x = a@mit.edu | y = x,b@mit.edu). 
     * A listname appears indirectly in a subexpression when a different listname that depends 
     * on the listname is explicitly used in the subexpression -- this should also throw a 
     * ParallelDefinitionException (e.g. c=a;(a=a@mit | b=c,b@mit)).
     * 
     * @param errorMessage error message
     */
    public ParallelDefinitionException(String errorMessage) {
        super(errorMessage);
    }
}
