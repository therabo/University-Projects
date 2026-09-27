module it.project.mail.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires it.project.mail.common;

    opens it.project.mail.server.controller to javafx.fxml;
    exports it.project.mail.server;
}
