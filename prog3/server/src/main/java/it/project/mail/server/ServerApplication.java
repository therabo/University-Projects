package it.project.mail.server;

import it.project.mail.server.controller.ServerController;
import it.project.mail.server.model.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

public final class ServerApplication extends Application {
    private Server server;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(ServerApplication.class.getResource("server-view.fxml"));
        ServerController controller = new ServerController();
        loader.setController(controller);
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Mail server");
        server = new Server(4040, Path.of(System.getProperty("mail.data.dir", "mail-data")),
                entry -> Platform.runLater(() -> controller.addLog(entry)));
        stage.show();
    }

    @Override
    public void stop() throws IOException {
        if (server != null) {
            server.close();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
