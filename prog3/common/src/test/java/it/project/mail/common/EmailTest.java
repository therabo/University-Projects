package it.project.mail.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTest {
    @Test
    void validatesTheSameAddressesForClientAndServer() {
        assertTrue(MailAddress.isValid("student@example.it"));
        assertFalse(MailAddress.isValid("../../server.log"));
        assertFalse(MailAddress.isValid("student@example.org"));
        assertFalse(MailAddress.isValid(null));
    }

    @Test
    void distinguishesMessagesWithIdenticalContent() {
        Email first = new Email("a@example.it", List.of("b@example.it"), "Subject", "Body");
        Email second = new Email("a@example.it", List.of("b@example.it"), "Subject", "Body");
        first.setDate();
        second.setDate();
        assertNotEquals(first, second);
        assertEquals(first, Email.restore(first.getId(), first.getSender(), first.getRecipients(),
                first.getSubject(), first.getText(), first.getDate()));
    }
}
