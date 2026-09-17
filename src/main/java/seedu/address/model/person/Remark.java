package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a person's remark in the address book.
 * Guarantees: immutable; is always non-null. An empty remark represents no remark.
 */
public class Remark {
    public final String value;

    /**
     * Constructs a {@code Remark}. Remarks have no content constraints.
     *
     * @param remark A non-null remark.
     */
    public Remark(String remark) {
        requireNonNull(remark);
        value = remark;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        // instanceof handles nulls
        return other instanceof Remark otherRemark && value.equals(otherRemark.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
