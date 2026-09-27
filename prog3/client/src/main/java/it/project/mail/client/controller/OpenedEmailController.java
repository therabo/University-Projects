package it.project.mail.client.controller;

import it.project.mail.common.Email;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.LinkedHashSet;
import java.util.Set;

public final class OpenedEmailController {
    @FXML
    private Button btn_reply_all;
    @FXML
    private Label sender_text;
    @FXML
    private Label receiver_text;
    @FXML
    private Label subject_text;
    @FXML
    private TextArea text_area_field;
    @FXML
    private Button btn_cancel;
    @FXML
    private Button btn_forward;
    @FXML
    private Button btn_reply;
    @FXML
    private Label dateTime_text;
    private Email email;
    private String account;
    private ListEmailController inbox;

    @FXML
    private void initialize() {
        text_area_field.setEditable(false);
        btn_cancel.setOnAction(event -> deleteEmail());
        btn_forward.setOnAction(event -> forwardEmail());
        btn_reply.setOnAction(event -> replyEmail());
        btn_reply_all.setOnAction(event -> replyAllEmail());
    }

    public void show(Email email, String account, ListEmailController inbox) {
        this.email = email;
        this.account = account;
        this.inbox = inbox;
        sender_text.setText(email.getSender());
        receiver_text.setText(String.join(" | ", email.getRecipients()));
        subject_text.setText(email.getSubject());
        text_area_field.setText(email.getText());
        dateTime_text.setText(email.getDate());
    }

    private void deleteEmail() {
        inbox.deleteEmail(email);
        close();
    }

    private void forwardEmail() {
        String body = email.getText().startsWith("Forwarded - ")
                ? email.getText() : "Forwarded - " + email.getText();
        inbox.openComposer("", email.getSubject(), body);
    }

    private void replyEmail() {
        inbox.openComposer(email.getSender(), prefix("Reply - "), "");
    }

    private void replyAllEmail() {
        Set<String> recipients = new LinkedHashSet<>(email.getRecipients());
        recipients.add(email.getSender());
        recipients.remove(account);
        inbox.openComposer(String.join(", ", recipients), prefix("Reply All - "), "");
    }

    private String prefix(String value) {
        String subject = email.getSubject();
        if (subject.startsWith("Reply All - ")) {
            subject = subject.substring(12);
        } else if (subject.startsWith("Reply - ")) {
            subject = subject.substring(8);
        }
        return value + subject;
    }

    private void close() {
        ((Stage) btn_cancel.getScene().getWindow()).close();
    }
}
