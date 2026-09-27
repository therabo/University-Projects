module it.project.mail.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires it.project.mail.common;

    opens it.project.mail.client to javafx.fxml;
    exports it.project.mail.client;
    exports it.project.mail.client.controller;
    opens it.project.mail.client.controller to javafx.fxml;
}