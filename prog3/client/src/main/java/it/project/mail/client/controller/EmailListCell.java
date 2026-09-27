package it.project.mail.client.controller;

import it.project.mail.common.Email;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

class EmailListCell extends ListCell<Email> {
    private final Label avatar = new Label();
    private final Label sender = new Label();
    private final Label subject = new Label();
    private final Label date = new Label();
    private final HBox row = new HBox(12);

    EmailListCell() {
        avatar.getStyleClass().add("mail-avatar");
        sender.getStyleClass().add("mail-sender");
        subject.getStyleClass().add("mail-subject");
        date.getStyleClass().add("mail-date");

        sender.setTextOverrun(OverrunStyle.ELLIPSIS);
        subject.setTextOverrun(OverrunStyle.ELLIPSIS);
        sender.setMaxWidth(Double.MAX_VALUE);
        subject.setMaxWidth(Double.MAX_VALUE);

        VBox description = new VBox(3, sender, subject);
        HBox.setHgrow(description, Priority.ALWAYS);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(avatar, description, date);
    }

    @Override
    protected void updateItem(Email email, boolean empty) {
        super.updateItem(email, empty);
        if (empty || email == null) {
            setGraphic(null);
            return;
        }

        String address = email.getSender();
        avatar.setText(address == null || address.isBlank() ? "?" : address.substring(0, 1).toUpperCase());
        sender.setText(address);
        subject.setText(email.getSubject());
        date.setText(email.getDate());
        setGraphic(row);
    }
}
