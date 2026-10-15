package seedu.address.testutil;

import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.List;
import java.util.Locale;

import seedu.address.model.person.ContactConflict;
import seedu.address.model.person.ContactConflict.Field;
import seedu.address.model.person.Person;

/**
 * Persons and helpers for testing duplicate-contact detection.
 */
public final class ConflictTestUtil {

    /** Has the same name as {@code ALICE}, but contact details not used by any typical person. */
    public static final Person ALICE_NAMESAKE = distinctFromTypicalPersons()
            .withName(ALICE.getName().fullName).withProjects("Orbital").build();

    private ConflictTestUtil() {} // prevents instantiation

    /**
     * A person that clashes with an existing person on exactly one contact detail.
     *
     * @param person The clashing person.
     * @param expectedConflict The conflict that adding or saving {@code person} should report.
     */
    public record Clash(Person person, ContactConflict expectedConflict) {

        public String expectedMessage() {
            return expectedConflict.getMessage();
        }
    }

    /**
     * Returns a builder for a person whose name and contact details are not used by any typical person.
     */
    public static PersonBuilder distinctFromTypicalPersons() {
        return new PersonBuilder().withName("Zoe Tan").withPhone("+6581112222")
                .withEmail("zoe.tan@example.com").withTelegramUsername("zoe_tan");
    }

    /**
     * Returns one clash per unique contact detail: copies of {@code base}, each with one detail taken from
     * {@code existing}. The email and Telegram username are typed in a different case, since case must not hide a
     * clash; the expected conflict reports the value as saved.
     */
    public static List<Clash> clashesWith(Person existing, Person base) {
        String phone = existing.getPhone().value;
        Person emailClash = new PersonBuilder(base).withEmail(existing.getEmail().value.toUpperCase(Locale.ROOT))
                .build();
        String telegramUsername = existing.getTelegramUsername().value;
        return List.of(
                new Clash(new PersonBuilder(base).withPhone(phone).build(),
                        new ContactConflict(Field.PHONE, phone, existing)),
                new Clash(emailClash, new ContactConflict(Field.EMAIL, emailClash.getEmail().value, existing)),
                new Clash(new PersonBuilder(base).withTelegramUsername("@" + telegramUsername.toUpperCase(Locale.ROOT))
                        .build(), new ContactConflict(Field.TELEGRAM_USERNAME, "@" + telegramUsername, existing)));
    }
}
