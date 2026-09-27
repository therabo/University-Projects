package it.project.mail.common;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Email implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final UUID id;
    private String sender;
    private List<String> recipients = List.of();
    private String subject;
    private String text;
    private String date;

    public Email() {
        id = UUID.randomUUID();
    }

    public Email(String sender, List<String> recipients, String subject, String text) {
        this();
        this.sender = sender;
        this.recipients = List.copyOf(recipients);
        this.subject = subject;
        this.text = text;
    }

    private Email(UUID id, String sender, List<String> recipients, String subject, String text, String date) {
        this.id = id;
        this.sender = sender;
        this.recipients = List.copyOf(recipients);
        this.subject = subject;
        this.text = text;
        this.date = date;
    }

    public static Email restore(UUID id, String sender, List<String> recipients, String subject, String text, String date) {
        return new Email(id, sender, recipients, subject, text, date);
    }

    public UUID getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public LocalDateTime getSentAt() {
        return date == null ? LocalDateTime.MIN : LocalDateTime.parse(date, DATE_FORMAT);
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = List.copyOf(recipients);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate() {
        date = LocalDateTime.now().format(DATE_FORMAT);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Email email && id.equals(email.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
