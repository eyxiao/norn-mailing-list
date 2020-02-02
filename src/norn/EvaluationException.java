package norn;

public abstract class EvaluationException extends Exception {
    /**
     * EvaluationException constructor.
     * @param errorMessage error message
     */
    public EvaluationException(String errorMessage) {
        super(errorMessage);
    }
}
