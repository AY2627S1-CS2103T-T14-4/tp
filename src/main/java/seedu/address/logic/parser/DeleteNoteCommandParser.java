package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PET_NAME;

import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.PetName;

/**
 * Parses arguments for the {@code delnote} command.
 */
public class DeleteNoteCommandParser implements Parser<DeleteNoteCommand> {

    /**
     * Parses exactly one client index, pet name and note type, in any order, with no preamble.
     *
     * @throws ParseException if a prefix is missing or repeated, the preamble is not empty,
     *     or a value is invalid.
     */
    @Override
    public DeleteNoteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_CLIENT, PREFIX_PET_NAME, PREFIX_NOTE_TYPE);
        if (!arguments.getPreamble().isEmpty() || Stream.of(PREFIX_CLIENT, PREFIX_PET_NAME, PREFIX_NOTE_TYPE)
                .anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteNoteCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_PET_NAME, PREFIX_NOTE_TYPE);

        Index clientIndex = ParserUtil.parseIndex(arguments.getValue(PREFIX_CLIENT).get());
        PetName petName = ParserUtil.parsePetName(arguments.getValue(PREFIX_PET_NAME).get());
        NoteType noteType = ParserUtil.parseNoteType(arguments.getValue(PREFIX_NOTE_TYPE).get());
        return new DeleteNoteCommand(clientIndex, petName, noteType);
    }
}
