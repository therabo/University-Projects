package it.project.mail.server.model;

import it.project.mail.common.Email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

final class Mailbox {
    private final Path file;
    private final List<Email> messages = new ArrayList<>();

    Mailbox(Path directory, String address) throws IOException {
        file = directory.resolve(address + ".mail");
        if (Files.notExists(file)) {
            Files.createFile(file);
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {
                messages.add(decode(line));
            }
        }
    }

    static boolean exists(Path directory, String address) {
        return Files.isRegularFile(directory.resolve(address + ".mail"));
    }

    synchronized List<Email> getMessages() {
        return new ArrayList<>(messages);
    }

    synchronized void addMessage(Email email) throws IOException {
        messages.add(email);
        try {
            save();
        } catch (IOException exception) {
            messages.remove(messages.size() - 1);
            throw exception;
        }
    }

    synchronized boolean removeMessage(Email email) throws IOException {
        int index = messages.indexOf(email);
        if (index < 0) {
            return false;
        }
        Email removed = messages.remove(index);
        try {
            save();
            return true;
        } catch (IOException exception) {
            messages.add(index, removed);
            throw exception;
        }
    }

    private void save() throws IOException {
        Path temporary = Files.createTempFile(file.getParent(), "mailbox-", ".tmp");
        try {
            List<String> lines = messages.stream().map(Mailbox::encode).toList();
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static String encode(Email email) {
        return String.join("|", "v2", email.getId().toString(), field(email.getSender()),
                field(String.join("\u001f", email.getRecipients())), field(email.getSubject()),
                field(email.getText()), field(email.getDate()));
    }

    private static Email decode(String line) throws IOException {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 7 || !"v2".equals(fields[0])) {
            throw new IOException("Invalid mailbox record");
        }
        try {
            String recipients = value(fields[3]);
            return Email.restore(UUID.fromString(fields[1]), value(fields[2]),
                    recipients.isEmpty() ? List.of() : List.of(recipients.split("\u001f")),
                    value(fields[4]), value(fields[5]), value(fields[6]));
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid mailbox record", exception);
        }
    }

    private static String field(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String value(String encoded) {
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }
}
