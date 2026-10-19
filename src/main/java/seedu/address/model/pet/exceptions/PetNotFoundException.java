package seedu.address.model.pet.exceptions;

/**
 * Signals that the client has no pet with the given name.
 */
public class PetNotFoundException extends RuntimeException {
    public PetNotFoundException() {
        super("The client has no pet with that name");
    }
}
