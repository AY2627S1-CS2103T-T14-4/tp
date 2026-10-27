package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLIENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_END_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AppointmentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentTime;
import seedu.address.model.pet.PetName;

/**
 * Parses appointment times and optional repeated pet names for a displayed client.
 */
public class AppointmentCommandParser implements Parser<AppointmentCommand> {
    /**
     * Parses exactly one client index, date and start time, and at most one end time,
     * and any number of pet names, in any order, with no preamble.
     * Validates date/time format here; past, current and future starts are accepted.
     *
     * @param args The arguments following the {@code appt} command word.
     * @return A command targeting the supplied displayed client index.
     * @throws NullPointerException if {@code args} is null.
     * @throws ParseException if a required prefix is missing or repeated, the preamble is non-empty,
     *     or the client index, date, start time, end time or a pet name is invalid.
     */
    @Override
    public AppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args,
                PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME, PREFIX_END_TIME, PREFIX_PET_NAME);
        if (!arguments.getPreamble().isEmpty() || Stream.of(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME)
                .anyMatch(prefix -> arguments.getValue(prefix).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AppointmentCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLIENT, PREFIX_DATE, PREFIX_TIME, PREFIX_END_TIME);
        ParserUtil.AppointmentTarget target = ParserUtil.parseAppointmentTarget(arguments);
        try {
            Optional<AppointmentTime> endTime = arguments.getValue(PREFIX_END_TIME).map(AppointmentTime::new);
            Set<PetName> petNames = new LinkedHashSet<>();
            for (String name : arguments.getAllValues(PREFIX_PET_NAME)) {
                petNames.add(ParserUtil.parsePetName(name));
            }
            Appointment appointment = new Appointment(target.date(), target.time(), endTime, "", petNames);
            return new AppointmentCommand(target.clientIndex(), appointment);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
