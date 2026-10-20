package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;

public class NoteCommandParserTest {

    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_validArgs_returnsNoteCommand() {
        NoteCommand expected = new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.TEMP,
                Optional.of(new PetNote("Anxious around loud dryers")));

        assertParseSuccess(parser, " c/1 pn/Milo ty/temp nt/Anxious around loud dryers", expected);
    }

    @Test
    public void parse_prefixesInDifferentOrder_returnsNoteCommand() {
        NoteCommand expected = new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.SENS,
                Optional.of(new PetNote("Sensitive paws")));

        assertParseSuccess(parser, " nt/Sensitive paws ty/SENS pn/Milo c/1", expected);
    }

    @Test
    public void parse_petNameRepeatedSpaces_normalizesName() {
        NoteCommand expected = new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Mr Fluffy"), NoteType.PREF,
                Optional.empty());

        assertParseSuccess(parser, " c/1 pn/Mr    Fluffy ty/pref", expected);
    }

    @Test
    public void parse_missingOrEmptyNote_clearsNote() {
        NoteCommand expected = new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"), NoteType.PREF,
                Optional.empty());

        assertParseSuccess(parser, " c/1 pn/Milo ty/pref", expected);
        assertParseSuccess(parser, " c/1 pn/Milo ty/pref nt/", expected);
        assertParseSuccess(parser, " c/1 pn/Milo ty/pref nt/   ", expected);
    }

    @Test
    public void parse_missingRequiredPrefix_throwsParseException() {
        assertParseFailure(parser, " pn/Milo ty/pref nt/Note", NoteCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " c/1 ty/pref nt/Note", NoteCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " c/1 pn/Milo nt/Note", NoteCommand.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, " c/0 pn/Milo ty/pref", NoteCommand.MESSAGE_INVALID_CLIENT_INDEX);
        assertParseFailure(parser, " c/abc pn/Milo ty/pref", NoteCommand.MESSAGE_INVALID_CLIENT_INDEX);
    }

    @Test
    public void parse_invalidPetName_throwsParseException() {
        assertParseFailure(parser, " c/1 pn/Milo! ty/pref", PetName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidNoteType_throwsParseException() {
        assertParseFailure(parser, " c/1 pn/Milo ty/temperament", NoteType.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidNote_throwsParseException() {
        assertParseFailure(parser, " c/1 pn/Milo ty/pref nt/" + "a".repeat(PetNote.MAX_LENGTH + 1),
                PetNote.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicatePrefixOrPreamble_throwsParseException() {
        assertParseFailure(parser, " c/1 c/2 pn/Milo ty/pref", NoteCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " c/1 pn/Milo pn/Luna ty/pref", NoteCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " c/1 pn/Milo ty/pref ty/temp", NoteCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " c/1 pn/Milo ty/pref nt/One nt/Two", NoteCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " extra c/1 pn/Milo ty/pref", NoteCommand.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
