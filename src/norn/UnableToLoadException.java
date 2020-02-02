package norn;

public class UnableToLoadException extends Exception {
    /**
     * UnableToLoadException constructor.
     * @param e exception e
     */
    public UnableToLoadException(Exception e) {
        super(e);
    }
}
