package catering.businesslogic;

/**
 * Exception thrown when there is a logic error in use case processing.
 * <p>
 * This exception is used to indicate issues related to the logic of use case operations
 * within the application, such as invalid state or logic errors that prevent proper
 * execution of business processes.
 * </p>
 */
public class UseCaseLogicException extends Exception {
    /**
     * Constructs a new {@code UseCaseLogicException} with the specified detail message.
     * <p>
     * The detail message is saved for later retrieval by the {@link #getMessage()} method.
     * </p>
     *
     * @param e The detail message.
     */
    public UseCaseLogicException(String e) {
        super(e);
    }

    /**
     * Constructs a new {@code UseCaseLogicException} with no detail message.
     * <p>
     * The detail message is set to {@code null}.
     * </p>
     */
    public UseCaseLogicException() {
    }
}
