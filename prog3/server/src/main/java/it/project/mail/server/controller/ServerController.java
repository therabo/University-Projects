package it.project.mail.server.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public final class ServerController {
    @FXML
    private ListView<String> listserver_view;

    public void addLog(String entry) {
        listserver_view.getItems().add(entry);
        listserver_view.scrollTo(listserver_view.getItems().size() - 1);
    }
}
