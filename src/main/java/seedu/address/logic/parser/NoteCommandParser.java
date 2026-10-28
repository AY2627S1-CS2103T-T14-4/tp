package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PET_NAME;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;

/**
 * Parses arguments for the {@code note} command.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    @Override
    public NoteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithTrailingText(
                args, PREFIX_NOTE, PREFIX_CLIENT, PREFIX_PET_NAME, PREFIX_NOTE_TYPE);

        if (!arguments.getPreamble().isEmpty()
                || !arguments.getValue(PREFIX_CLIENT).isPresent()
                || !arguments.getValue(PREFIX_PET_NAME).isPresent()
                || !arguments.getValue(PREFIX_NOTE_TYPE).isPresent()
                || !arguments.getValue(PREFIX_NOTE).isPresent()
                || hasDuplicatePrefixes(arguments, PREFIX_CLIENT, PREFIX_PET_NAME, PREFIX_NOTE_TYPE, PREFIX_NOTE)) {
            throw new ParseException(NoteCommand.MESSAGE_INVALID_FORMAT);
        }

        Index clientIndex;
        try {
            clientIndex = ParserUtil.parseIndex(arguments.getValue(PREFIX_CLIENT).get());
        } catch (ParseException e) {
            throw new ParseException(NoteCommand.MESSAGE_INVALID_CLIENT_INDEX, e);
        }

        PetName petName = ParserUtil.parsePetName(arguments.getValue(PREFIX_PET_NAME).get());
        NoteType noteType = ParserUtil.parseNoteType(arguments.getValue(PREFIX_NOTE_TYPE).get());
        PetNote note = parseNote(arguments.getValue(PREFIX_NOTE).get());

        return new NoteCommand(clientIndex, petName, noteType, note);
    }

    private static boolean hasDuplicatePrefixes(ArgumentMultimap arguments, Prefix... prefixes) {
        for (Prefix prefix : prefixes) {
            if (arguments.getAllValues(prefix).size() > 1) {
                return true;
            }
        }
        return false;
    }

    private static PetNote parseNote(String value) throws ParseException {
        try {
            return new PetNote(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(PetNote.MESSAGE_CONSTRAINTS, e);
        }
    }
}
