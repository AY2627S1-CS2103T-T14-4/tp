package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Address;
import seedu.address.model.client.Email;
import seedu.address.model.client.Name;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "12-34";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "12345678";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_CLIENT, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_CLIENT, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseAppointmentTarget_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAppointmentTarget(null));
    }

    @Test
    public void parseAppointmentTarget_validInput_acceptsAnyOrderAndPastOrFutureDates() throws Exception {
        for (String date : List.of("29-02-2024", "31-12-9999")) {
            ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" t/00:00   d/" + date + "   c/1 ",
                    PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME);
            ParserUtil.AppointmentTarget target = ParserUtil.parseAppointmentTarget(arguments);
            assertEquals(INDEX_FIRST_CLIENT, target.clientIndex());
            assertEquals(new AppointmentDate(date), target.date());
            assertEquals(new AppointmentTime("00:00"), target.time());
        }
    }

    @Test
    public void parseAppointmentTarget_additionalPrefixes_remainAvailableToCaller() throws Exception {
        Prefix notesPrefix = new Prefix("nt/");
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" c/1 d/12-12-2026 t/14:00 nt/Teddy cut",
                PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME, notesPrefix);
        ParserUtil.AppointmentTarget expected = new ParserUtil.AppointmentTarget(INDEX_FIRST_CLIENT,
                new AppointmentDate("12-12-2026"), new AppointmentTime("14:00"));
        assertEquals(expected, ParserUtil.parseAppointmentTarget(arguments));
        assertEquals("Teddy cut", arguments.getValue(notesPrefix).orElseThrow());
    }

    @Test
    public void parseAppointmentTarget_invalidValues_preservesValidationOrderAndMessages() {
        String[] inputs = {" c/0 d/invalid t/invalid", " c/1 d/31-02-2026 t/invalid",
            " c/1 d/12-12-2026 t/24:00"};
        String[] messages = {MESSAGE_INVALID_INDEX, AppointmentDate.MESSAGE_CONSTRAINTS,
            AppointmentTime.MESSAGE_CONSTRAINTS};
        for (int i = 0; i < inputs.length; i++) {
            ArgumentMultimap arguments = ArgumentTokenizer.tokenize(inputs[i], PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME);
            assertThrows(ParseException.class, messages[i], () -> ParserUtil.parseAppointmentTarget(arguments));
        }
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parseName_repeatedSpaces_returnsNormalizedName() throws Exception {
        assertEquals(new Name("Alice Tan"), ParserUtil.parseName("  Alice   Tan  "));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }

    @Test
    public void parsePetName_repeatedSpaces_returnsNormalizedName() throws Exception {
        String input = "  Mi" + " ".repeat(45) + "lo  ";

        PetName expected = new PetName("Mi lo");

        assertEquals(expected, ParserUtil.parsePetName(input));
    }

    @Test
    public void parseSpecies_repeatedSpaces_returnsNormalizedSpecies() throws Exception {
        String input = "  Golden" + " ".repeat(45) + "Retriever  ";

        Species expected = new Species("Golden Retriever");

        assertEquals(expected, ParserUtil.parseSpecies(input));
    }

    @Test
    public void parseBreed_repeatedSpaces_returnsNormalizedBreed() throws Exception {
        String input = "  Golden" + " ".repeat(45) + "Retriever  ";

        Breed expected = new Breed("Golden Retriever");

        assertEquals(expected, ParserUtil.parseBreed(input));
    }

    @Test
    public void parseNoteType_validKeyword_returnsNoteType() throws Exception {
        assertEquals(NoteType.PREF, ParserUtil.parseNoteType("pref"));
        assertEquals(NoteType.TEMP, ParserUtil.parseNoteType(" TEMP "));
    }

    @Test
    public void parseNoteType_invalidKeyword_throwsParseException() {
        assertThrows(ParseException.class, NoteType.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseNoteType("mood"));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseNoteType(null));
    }
}
