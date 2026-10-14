package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_USERNAME_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.ContactConflict.Field;
import seedu.address.model.project.Project;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    private static final String ALEX_NAME = "Alex Yeoh";
    private static final String ALEX_PHONE = "91234567";
    private static final String ALEX_EMAIL = "alex@example.com";
    private static final String ALEX_TELEGRAM_USERNAME = "alex_yeoh";
    private static final String ALEX_PROJECT = "CS2103T";
    private static final Person ALEX = new PersonBuilder().withName(ALEX_NAME).withPhone(ALEX_PHONE)
            .withEmail(ALEX_EMAIL).withTelegramUsername(ALEX_TELEGRAM_USERNAME).withProjects(ALEX_PROJECT).build();

    /** Each modifier makes exactly one contact detail the same as {@code ALEX}'s. */
    private static final List<UnaryOperator<PersonBuilder>> CLASHES_WITH_ALEX = List.of(
            builder -> builder.withPhone(ALEX_PHONE),
            builder -> builder.withEmail(ALEX_EMAIL),
            builder -> builder.withTelegramUsername(ALEX_TELEGRAM_USERNAME));

    @Test
    public void getProjects_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().withProjects("CS2103T").build();

        assertThrows(UnsupportedOperationException.class, () ->
                person.getProjects().add(new Project("Orbital")));
    }

    @Test
    public void constructor_duplicateProjectsIgnoringCase_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new PersonBuilder().withProjects("CS2103T", "cs2103t").build());
    }

    @Test
    public void conflictsWith_sameNameDifferentDetails_returnsFalse() {
        assertConflictBothWays(ALEX, distinctFromAlex().withName(ALEX_NAME).build(), false);
    }

    @Test
    public void conflictsWith_sharedContactDetail_returnsTrue() {
        for (UnaryOperator<PersonBuilder> clash : CLASHES_WITH_ALEX) {
            // same name
            assertConflictBothWays(ALEX, clash.apply(distinctFromAlex().withName(ALEX_NAME)).build(), true);
            // different name
            assertConflictBothWays(ALEX, clash.apply(distinctFromAlex()).build(), true);
        }
    }

    @Test
    public void conflictsWith_sameProjectDifferentDetails_returnsFalse() {
        assertConflictBothWays(ALEX, distinctFromAlex().withProjects(ALEX_PROJECT).build(), false);
        assertConflictBothWays(ALEX, distinctFromAlex().withName(ALEX_NAME).withProjects(ALEX_PROJECT).build(),
                false);
    }

    @Test
    public void conflictsWith_nameDiffersOnlyInCaseOrSpacing_returnsFalse() {
        assertConflictBothWays(ALEX, distinctFromAlex().withName("alex yeoh").build(), false);
        assertConflictBothWays(ALEX, distinctFromAlex().withName("Alex  Yeoh").build(), false);
    }

    @Test
    public void conflictsWith_emailDiffersOnlyInCase_returnsTrue() {
        assertConflictBothWays(ALEX, distinctFromAlex().withEmail("ALEX@Example.com").build(), true);
        assertConflictBothWays(ALEX, distinctFromAlex().withEmail("alex@EXAMPLE.COM").build(), true);
    }

    @Test
    public void conflictsWith_telegramUsernameWithAtSignAndCase_returnsTrue() {
        assertConflictBothWays(ALEX, distinctFromAlex().withTelegramUsername("@Alex_Yeoh").build(), true);
    }

    @Test
    public void conflictsWith_emptyTelegramUsername_returnsFalse() {
        Person withoutTelegram = distinctFromAlex().withoutTelegramUsername().build();
        Person otherWithoutTelegram = new PersonBuilder().withName("Charlotte Oliveiro").withPhone("93210283")
                .withEmail("charlotte@example.com").withoutTelegramUsername().build();

        assertConflictBothWays(ALEX, withoutTelegram, false);
        assertConflictBothWays(withoutTelegram, otherWithoutTelegram, false);
    }

    @Test
    public void conflictsWith_self_returnsTrue() {
        assertTrue(ALEX.conflictsWith(ALEX));
    }

    @Test
    public void conflictsWith_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ALEX.conflictsWith(null));
    }

    @Test
    public void findConflictWith_noSharedDetail_returnsEmpty() {
        assertEquals(Optional.empty(), distinctFromAlex().build().findConflictWith(ALEX));
    }

    @Test
    public void findConflictWith_sharedDetails_reportsPhoneThenEmailThenTelegram() {
        Person sharesAll = new PersonBuilder(ALEX).withName("Bernice Yu").withEmail("ALEX@example.com").build();
        Person sharesEmailAndTelegram = distinctFromAlex().withEmail("ALEX@example.com")
                .withTelegramUsername("alex_yeoh").build();
        Person sharesTelegram = distinctFromAlex().withTelegramUsername("@Alex_Yeoh").build();

        assertConflict(new ContactConflict(Field.PHONE, ALEX_PHONE, ALEX), sharesAll);
        assertConflict(new ContactConflict(Field.EMAIL, "alex@example.com", ALEX), sharesEmailAndTelegram);
        assertConflict(new ContactConflict(Field.TELEGRAM_USERNAME, "@alex_yeoh", ALEX), sharesTelegram);
    }

    /**
     * Returns a builder for a person whose name and contact details all differ from {@code ALEX}'s.
     */
    private static PersonBuilder distinctFromAlex() {
        return new PersonBuilder().withName("Bernice Yu").withPhone("82345678")
                .withEmail("bernice@example.com").withTelegramUsername("bernice_yu");
    }

    private static void assertConflictBothWays(Person first, Person second, boolean isConflictExpected) {
        assertEquals(isConflictExpected, first.conflictsWith(second));
        assertEquals(isConflictExpected, second.conflictsWith(first));
    }

    private static void assertConflict(ContactConflict expected, Person candidate) {
        assertEquals(Optional.of(expected), candidate.findConflictWith(ALEX));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different Telegram username -> returns false
        editedAlice = new PersonBuilder(ALICE).withTelegramUsername(VALID_TELEGRAM_USERNAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different projects -> returns false
        editedAlice = new PersonBuilder(ALICE).withProjects("CS2103T").build();
        assertFalse(ALICE.equals(editedAlice));

        // different project order -> returns false
        Person firstProjectOrder = new PersonBuilder(ALICE).withProjects("CS2103T", "Orbital").build();
        Person secondProjectOrder = new PersonBuilder(ALICE).withProjects("Orbital", "CS2103T").build();
        assertFalse(firstProjectOrder.equals(secondProjectOrder));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", telegramUsername=" + ALICE.getTelegramUsername()
                + ", projects=" + ALICE.getProjects() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
