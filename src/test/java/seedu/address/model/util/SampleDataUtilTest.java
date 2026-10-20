package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.client.Client;
import seedu.address.model.pet.NoteType;
import seedu.address.model.tag.Tag;

public class SampleDataUtilTest {

    private final List<Client> sampleClients = Arrays.asList(SampleDataUtil.getSampleClients());

    @Test
    public void getSamplePawPals_containsAllSampleClients() {
        assertEquals(sampleClients, SampleDataUtil.getSamplePawPals().getClientList());
    }

    @Test
    public void getSampleClients_linkedPetsBelongToTheirClient() {
        for (Client client : sampleClients) {
            client.getAppointments().forEach(appointment ->
                    assertEquals(appointment.getPetNames().size(), client.getPetsFor(appointment).size()));
        }
    }

    @Test
    public void getSampleClients_showcaseFeatures() {
        assertTrue(sampleClients.stream().flatMap(client -> client.getPets().stream())
                .anyMatch(pet -> pet.hasNote(NoteType.SENS)));
        assertTrue(sampleClients.stream().flatMap(client -> client.getPets().stream())
                .anyMatch(pet -> pet.hasNote(NoteType.TEMP)));
        assertTrue(sampleClients.stream().flatMap(client -> client.getPets().stream())
                .anyMatch(pet -> !pet.hasSafetyNote()));
        assertTrue(sampleClients.stream().flatMap(client -> client.getAppointments().stream())
                .anyMatch(appointment -> appointment.getEndTime().isPresent()));
        assertTrue(sampleClients.stream().flatMap(client -> client.getAppointments().stream())
                .anyMatch(appointment -> appointment.getEndTime().isEmpty()));
        assertTrue(sampleClients.stream().anyMatch(client -> client.getPets().isEmpty()));
    }

    @Test
    public void getTagSet_returnsTags() {
        assertEquals(Set.of(new Tag("vip"), new Tag("monthly")), SampleDataUtil.getTagSet("vip", "monthly"));
    }
}
