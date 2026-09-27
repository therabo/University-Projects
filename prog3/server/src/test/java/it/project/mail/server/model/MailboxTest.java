package it.project.mail.server.model;

import it.project.mail.common.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailboxTest {
    @TempDir
    Path directory;

    @Test
    void preservesMultilineTextAndDeletesOnlyTheSelectedMessage() throws IOException {
        Mailbox mailbox = new Mailbox(directory, "student@example.it");
        Email first = new Email("sender@example.it", List.of("student@example.it"), "A_B | C", "Line one\nLine two");
        Email second = new Email("sender@example.it", List.of("student@example.it"), "A_B | C", "Line one\nLine two");
        first.setDate();
        second.setDate();
        mailbox.addMessage(first);
        mailbox.addMessage(second);

        Mailbox reloaded = new Mailbox(directory, "student@example.it");
        assertEquals(2, reloaded.getMessages().size());
        assertEquals("Line one\nLine two", reloaded.getMessages().getFirst().getText());
        assertTrue(reloaded.removeMessage(first));
        assertFalse(reloaded.removeMessage(first));
        assertEquals(second, new Mailbox(directory, "student@example.it").getMessages().getFirst());
    }
}
