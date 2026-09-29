package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents an optional, immutable remark about a person.
 */
public class Remark {

    public final String value;

    /**
     * Constructs a remark. An empty value represents no remark.
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
        return other == this || (other instanceof Remark otherRemark && value.equals(otherRemark.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
