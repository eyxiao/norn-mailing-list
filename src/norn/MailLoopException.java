package norn;

public class MailLoopException extends EvaluationException {
    /**
     * MailLoopException constructor. This exception is thrown when an expression being evaluated
     * contains a mailing loop, where there are mutually-recursive list definitions (e.g. a=b;b=a). 
     * A mutually-recursive list definition is when two listnames depend directly or indirectly on
     * each other's definitions. 
     * 
     * However, an edit to a listname that uses the listname itself (e.g. a=a)
     * is not a mutually-recursive definition, and a MailLoopException should not be thrown if this 
     * occurs in a list expression being evaluated.
     *
     * @param errorMessage error message
     */
    public MailLoopException(String errorMessage) {
        super(errorMessage);
    }
}
