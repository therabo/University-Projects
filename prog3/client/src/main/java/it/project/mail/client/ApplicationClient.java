package it.project.mail.client;

import it.project.mail.client.controller.LoginController;
import it.project.mail.client.model.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class ApplicationClient extends Application {
    private final Client client = new Client();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(ApplicationClient.class.getResource("login-view.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Login");
        ((LoginController) loader.getController()).setModel(client);
        stage.show();
    }

    @Override
    public void stop() {
        client.sendDisconnect();
    }

    public static void main(String[] args) {
        launch();
    }
}
