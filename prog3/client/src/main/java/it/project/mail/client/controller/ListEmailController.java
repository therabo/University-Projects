package it.project.mail.client.controller;

import it.project.mail.client.ApplicationClient;
import it.project.mail.client.model.Client;
import it.project.mail.common.Email;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ListEmailController {
    @FXML
    private Button btn_newemail;
    @FXML
    private Button btn_newarrivals;
    @FXML
    private Button btn_disconnect;
    @FXML
    private ListView<Email> listview_email;
    private final Client client;
    private final String account;

    public ListEmailController(Client client, String account) {
        this.client = client;
        this.account = account;
    }

    @FXML
    private void initialize() {
        listview_email.setCellFactory(view -> new EmailListCell());
        listview_email.setOnMouseClicked(event -> openSelectedEmail());
        btn_newemail.setOnAction(event -> openComposer("", "", ""));
        btn_newarrivals.setOnAction(event -> refresh());
        btn_disconnect.setOnAction(event -> disconnect());
    }

    public void fillReceivedEmail(List<Email> emails) {
        List<Email> sorted = emails.stream()
                .sorted(Comparator.comparing(Email::getSentAt).reversed())
                .toList();
        listview_email.getItems().setAll(sorted);
    }

    public void openComposer(String recipients, String subject, String body) {
        try {
            FXMLLoader loader = new FXMLLoader(ApplicationClient.class.getResource("email-view.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Nuova email");
            EmailController controller = loader.getController();
            controller.setModel(client);
            controller.setEmailField(recipients);
            controller.setSubjectField(subject);
            controller.setTextField(body);
            stage.show();
        } catch (IOException exception) {
            showError("Impossibile aprire la composizione del messaggio.");
        }
    }

    private void openSelectedEmail() {
        Email selected = listview_email.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(ApplicationClient.class.getResource("email.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(selected.getSubject());
            ((OpenedEmailController) loader.getController()).show(selected, account, this);
            stage.show();
            listview_email.getSelectionModel().clearSelection();
        } catch (IOException exception) {
            showError("Impossibile aprire il messaggio.");
        }
    }

    public void refresh() {
        btn_newarrivals.setDisable(true);
        CompletableFuture.supplyAsync(() -> client.receivedEmail(account))
                .whenComplete((emails, failure) -> Platform.runLater(() -> {
                    btn_newarrivals.setDisable(false);
                    if (failure == null) {
                        fillReceivedEmail(emails);
                    } else {
                        showError("Impossibile aggiornare la casella di posta.");
                    }
                }));
    }

    public void deleteEmail(Email email) {
        CompletableFuture.supplyAsync(() -> client.cancelEmail(email))
                .whenComplete((deleted, failure) -> Platform.runLater(() -> {
                    if (failure == null && Boolean.TRUE.equals(deleted)) {
                        refresh();
                    } else {
                        showError("Impossibile eliminare il messaggio.");
                    }
                }));
    }

    private void disconnect() {
        btn_disconnect.setDisable(true);
        CompletableFuture.runAsync(client::sendDisconnect)
                .whenComplete((unused, failure) -> Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(ApplicationClient.class.getResource("login-view.fxml"));
                        Stage stage = (Stage) listview_email.getScene().getWindow();
                        stage.setScene(new Scene(loader.load()));
                        stage.setTitle("Login");
                        ((LoginController) loader.getController()).setModel(client);
                    } catch (IOException exception) {
                        btn_disconnect.setDisable(false);
                        showError("Impossibile tornare alla schermata di accesso.");
                    }
                }));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
