package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_USERNAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_USERNAME_BOB;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramUsername;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    private static final TelegramUsername TELEGRAM_AMY = new TelegramUsername(VALID_TELEGRAM_USERNAME_AMY);
    private static final TelegramUsername TELEGRAM_BOB = new TelegramUsername(VALID_TELEGRAM_USERNAME_BOB);

    @Test
    public void equals() {
        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(DESC_AMY.equals(DESC_AMY));

        // null -> returns false
        assertFalse(DESC_AMY.equals(null));

        // different types -> returns false
        assertFalse(DESC_AMY.equals(5));

        // different values -> returns false
        assertFalse(DESC_AMY.equals(DESC_BOB));

        // different name -> returns false
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withName(VALID_NAME_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different phone -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withPhone(VALID_PHONE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different email -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // Telegram username set on only one -> returns false
        editedAmy = new EditPersonDescriptor(DESC_AMY);
        editedAmy.setTelegramUsername(TELEGRAM_AMY);
        assertFalse(DESC_AMY.equals(editedAmy));

        // different Telegram username only -> returns false
        EditPersonDescriptor otherTelegram = new EditPersonDescriptor(DESC_AMY);
        otherTelegram.setTelegramUsername(TELEGRAM_BOB);
        assertFalse(editedAmy.equals(otherTelegram));

        // same Telegram username -> returns true
        EditPersonDescriptor sameTelegram = new EditPersonDescriptor(DESC_AMY);
        sameTelegram.setTelegramUsername(new TelegramUsername("@AMY_Bee"));
        assertTrue(editedAmy.equals(sameTelegram));
    }

    @Test
    public void telegramUsername_presentAndAbsent() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertEquals(Optional.empty(), descriptor.getTelegramUsername());

        descriptor.setTelegramUsername(TELEGRAM_AMY);
        assertEquals(Optional.of(TELEGRAM_AMY), descriptor.getTelegramUsername());

        // null means "not supplied", so the existing value is kept
        descriptor.setTelegramUsername(null);
        assertEquals(Optional.empty(), descriptor.getTelegramUsername());
    }

    @Test
    public void setTelegramUsername_empty_throwsIllegalArgumentException() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        assertThrows(IllegalArgumentException.class, EditPersonDescriptor.MESSAGE_TELEGRAM_NOT_CLEARABLE, () ->
                descriptor.setTelegramUsername(TelegramUsername.EMPTY));
        assertEquals(Optional.empty(), descriptor.getTelegramUsername());
    }

    @Test
    public void isAnyFieldEdited() {
        assertFalse(new EditPersonDescriptor().isAnyFieldEdited());

        EditPersonDescriptor telegramOnly = new EditPersonDescriptor();
        telegramOnly.setTelegramUsername(TELEGRAM_AMY);
        assertTrue(telegramOnly.isAnyFieldEdited());
    }

    @Test
    public void copyConstructor_copiesAllFields() {
        EditPersonDescriptor original = new EditPersonDescriptor(DESC_AMY);
        original.setTelegramUsername(TELEGRAM_AMY);

        EditPersonDescriptor copy = new EditPersonDescriptor(original);

        assertEquals(original, copy);
        assertEquals(Optional.of(TELEGRAM_AMY), copy.getTelegramUsername());
        // an absent Telegram username is copied as absent
        assertEquals(Optional.empty(), new EditPersonDescriptor(DESC_AMY).getTelegramUsername());
    }

    @Test
    public void copyConstructor_changingCopy_leavesOriginalUnchanged() {
        EditPersonDescriptor original = new EditPersonDescriptor(DESC_AMY);
        original.setTelegramUsername(TELEGRAM_AMY);

        EditPersonDescriptor copy = new EditPersonDescriptor(original);
        copy.setName(new Name(VALID_NAME_BOB));
        copy.setPhone(new Phone(VALID_PHONE_BOB));
        copy.setEmail(new Email(VALID_EMAIL_BOB));
        copy.setTelegramUsername(TELEGRAM_BOB);

        assertEquals(Optional.of(new Name(VALID_NAME_AMY)), original.getName());
        assertEquals(Optional.of(new Phone(VALID_PHONE_AMY)), original.getPhone());
        assertEquals(Optional.of(new Email(VALID_EMAIL_AMY)), original.getEmail());
        assertEquals(Optional.of(TELEGRAM_AMY), original.getTelegramUsername());
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{name="
                + editPersonDescriptor.getName().orElse(null) + ", phone="
                + editPersonDescriptor.getPhone().orElse(null) + ", email="
                + editPersonDescriptor.getEmail().orElse(null) + ", telegramUsername="
                + editPersonDescriptor.getTelegramUsername().orElse(null) + "}";
        assertEquals(expected, editPersonDescriptor.toString());

        editPersonDescriptor.setTelegramUsername(TELEGRAM_AMY);
        assertTrue(editPersonDescriptor.toString().endsWith(", telegramUsername=@amy_bee}"));
    }
}
