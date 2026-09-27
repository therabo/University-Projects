package it.project.mail.client.controller;

import it.project.mail.client.ApplicationClient;
import it.project.mail.client.model.Client;
import it.project.mail.common.Email;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class LoginController {
    @FXML
    private Button btn_login;
    @FXML
    private TextField email_field;
    @FXML
    private Label lbl_error;
    private Client client;

    @FXML
    private void initialize() {
        btn_login.setOnAction(event -> login());
        email_field.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                login();
            }
        });
    }

    public void setModel(Client client) {
        this.client = client;
    }

    private void login() {
        String address = email_field.getText().trim();
        if (!Client.isValidEmail(address)) {
            lbl_error.setText("Inserisci un indirizzo email valido.");
            return;
        }
        btn_login.setDisable(true);
        lbl_error.setText("");
        CompletableFuture.supplyAsync(() -> client.sendLogin(address))
                .whenComplete((emails, failure) -> Platform.runLater(() -> finishLogin(address, emails, failure)));
    }

    private void finishLogin(String address, List<Email> emails, Throwable failure) {
        btn_login.setDisable(false);
        if (failure != null) {
            lbl_error.setText("Impossibile connettersi al server.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(ApplicationClient.class.getResource("listemail-view.fxml"));
            ListEmailController controller = new ListEmailController(client, address);
            loader.setController(controller);
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btn_login.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(address);
            controller.fillReceivedEmail(emails);
        } catch (IOException exception) {
            lbl_error.setText("Impossibile aprire la casella di posta.");
        }
    }
}
