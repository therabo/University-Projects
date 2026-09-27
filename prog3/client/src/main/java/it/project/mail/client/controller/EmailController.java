package it.project.mail.client.controller;

import it.project.mail.client.model.Client;
import it.project.mail.common.Email;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class EmailController {
    @FXML
    private Button btn_send;
    @FXML
    private TextField field_email;
    @FXML
    private TextField field_subject;
    @FXML
    private TextArea field_text;
    private Client client;

    @FXML
    private void initialize() {
        btn_send.setOnAction(event -> sendEmail());
    }

    public void setModel(Client client) {
        this.client = client;
    }

    public void setEmailField(String address) {
        field_email.setText(address);
    }

    public void setSubjectField(String subject) {
        field_subject.setText(subject);
    }

    public void setTextField(String text) {
        field_text.setText(text);
    }

    private void sendEmail() {
        String addresses = field_email.getText().trim();
        String subject = field_subject.getText().trim();
        String body = field_text.getText().trim();
        if (addresses.isEmpty() || subject.isEmpty() || body.isEmpty()) {
            showAlert("Completa tutti i campi prima di inviare il messaggio.", Alert.AlertType.ERROR);
            return;
        }
        List<String> recipients = Arrays.stream(addresses.split("[;,]"))
                .map(String::trim)
                .toList();
        if (recipients.stream().anyMatch(address -> !Client.isValidEmail(address))) {
            showAlert("Uno o più indirizzi email non sono validi.", Alert.AlertType.ERROR);
            return;
        }
        Email email = new Email(null, recipients, subject, body);
        btn_send.setDisable(true);
        CompletableFuture.supplyAsync(() -> client.sendEmail(email))
                .whenComplete((sent, failure) -> Platform.runLater(() -> {
                    btn_send.setDisable(false);
                    if (failure == null && Boolean.TRUE.equals(sent)) {
                        showAlert("Messaggio consegnato.", Alert.AlertType.INFORMATION);
                        ((Stage) field_email.getScene().getWindow()).close();
                    } else {
                        showAlert("Invio non riuscito. Verifica la connessione e che i destinatari abbiano una casella registrata.", Alert.AlertType.ERROR);
                    }
                }));
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
