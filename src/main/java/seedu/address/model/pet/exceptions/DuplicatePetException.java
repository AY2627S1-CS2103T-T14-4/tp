package seedu.address.model.pet.exceptions;

/**
 * Signals that an operation would add a pet whose identity already exists for the client.
 */
public class DuplicatePetException extends RuntimeException {
    public DuplicatePetException() {
        super("Operation would result in a duplicate pet for the client");
    }
}
