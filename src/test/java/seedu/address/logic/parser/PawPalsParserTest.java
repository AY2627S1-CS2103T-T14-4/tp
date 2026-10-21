package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddPetCommand;
import seedu.address.logic.commands.AppointmentCommand;
import seedu.address.logic.commands.CancelCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteClientCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditClientDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.client.Client;
import seedu.address.model.client.NameContainsKeywordsPredicate;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.NoteType;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetNote;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.ClientUtil;
import seedu.address.testutil.EditClientDescriptorBuilder;

public class PawPalsParserTest {

    private final PawPalsParser parser = new PawPalsParser();

    @Test
    public void parseCommand_appointment() throws Exception {
        assertEquals(new AppointmentCommand(INDEX_FIRST_CLIENT,
                new Appointment(new AppointmentDate("12-12-2026"), new AppointmentTime("12:34"))),
                parser.parseCommand("appt c/1 d/12-12-2026 t/12:34"));
        assertThrows(ParseException.class, () -> parser.parseCommand("appt c/1 d/31-02-2026 t/12:34"));
    }

    @Test
    public void parseCommand_cancel() throws Exception {
        assertEquals(new CancelCommand(INDEX_FIRST_CLIENT,
                new AppointmentDate("29-02-2024"), new AppointmentTime("14:00")),
                parser.parseCommand("cancel c/1 d/29-02-2024 t/14:00"));
        assertThrows(ParseException.class, () -> parser.parseCommand("cancel c/1 d/31-02-2026 t/14:00"));
        assertThrows(ParseException.class, () -> parser.parseCommand("cancel c/1 d/12-12-2026"));
    }

    @Test
    public void parseCommand_add() throws Exception {
        Client client = new ClientBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(ClientUtil.getAddCommand(client));
        assertEquals(new AddCommand(client), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_deleteClient() throws Exception {
        DeleteClientCommand command = (DeleteClientCommand) parser.parseCommand(
                DeleteClientCommand.COMMAND_WORD + " " + INDEX_FIRST_CLIENT.getOneBased());
        assertEquals(new DeleteClientCommand(INDEX_FIRST_CLIENT), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Client client = new ClientBuilder().build();
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder(client).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_CLIENT.getOneBased() + " " + ClientUtil.getEditClientDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_CLIENT, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void parseCommand_addPet() throws Exception {
        Pet pet = new Pet(new PetName("Milo"), new Species("Dog"), new Breed("Poodle"));

        AddPetCommand command = (AddPetCommand) parser.parseCommand("addp c/1 n/Milo s/Dog b/Poodle");

        assertEquals(new AddPetCommand(INDEX_FIRST_CLIENT, pet), command);
    }

    @Test
    public void parseCommand_note() throws Exception {
        NoteCommand command = (NoteCommand) parser.parseCommand(
                "note c/1 pn/Milo ty/temp nt/Anxious around loud dryers");

        assertEquals(new NoteCommand(INDEX_FIRST_CLIENT, new PetName("Milo"),
                NoteType.TEMP, new PetNote("Anxious around loud dryers")), command);
    }
}
