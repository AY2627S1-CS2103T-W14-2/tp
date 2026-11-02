package seedu.address.storage;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Writes contact data without truncating the previous file. Atomic replacement is required;
 * an unsupported or blocked replacement is reported as a save failure.
 */
class AtomicFileWriter {
    private static final Logger logger = LogsCenter.getLogger(AtomicFileWriter.class);

    /**
     * Writes complete content to a sibling temporary file before replacing the destination.
     * Existing symbolic links are followed, and existing access permissions are retained where supported.
     * A failed first save does not create an empty destination. Concurrent external edits are not supported.
     */
    void write(Path file, String content) throws IOException {
        Path target = resolveTarget(file);
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "linkup-", ".tmp");
        boolean isCommitted = false;
        try {
            writeTemporaryFile(temporary, content);
            copyPermissions(target, temporary);
            replaceFile(temporary, target);
            isCommitted = true;
        } finally {
            // A successful move consumes the temporary file. Nothing fallible may follow the commit.
            if (!isCommitted) {
                try {
                    deleteTemporaryFile(temporary);
                } catch (IOException e) {
                    logger.warning("Could not remove temporary contact file " + temporary + ": " + e);
                }
            }
        }
    }

    private Path resolveTarget(Path file) throws IOException {
        Path target;
        try {
            target = file.toRealPath();
        } catch (NoSuchFileException e) {
            // Do not replace a dangling link with a regular file.
            if (Files.isSymbolicLink(file)) {
                throw e;
            }
            return file.toAbsolutePath();
        }
        if (!Files.isRegularFile(target)) {
            throw new IOException("The contact data path must refer to a regular file: " + file);
        }
        // Rename permission alone must not bypass a read-only contact file.
        if (!Files.isWritable(target)) {
            throw new AccessDeniedException(file.toString());
        }
        return target;
    }

    private void copyPermissions(Path target, Path temporary) throws IOException {
        if (!Files.exists(target)) {
            return;
        }
        PosixFileAttributeView posix = Files.getFileAttributeView(target, PosixFileAttributeView.class);
        if (posix != null) {
            Files.setPosixFilePermissions(temporary, posix.readAttributes().permissions());
        }
        AclFileAttributeView acl = Files.getFileAttributeView(target, AclFileAttributeView.class);
        if (acl != null) {
            Files.getFileAttributeView(temporary, AclFileAttributeView.class).setAcl(acl.getAcl());
        }
    }

    void writeTemporaryFile(Path temporary, String content) throws IOException {
        Files.writeString(temporary, content);
    }

    void replaceFile(Path temporary, Path target) throws IOException {
        Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
    }

    void deleteTemporaryFile(Path temporary) throws IOException {
        Files.deleteIfExists(temporary);
    }
}
