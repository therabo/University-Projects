package it.project.mail.server.model;

import it.project.mail.common.Email;
import it.project.mail.common.MailAddress;
import it.project.mail.common.RequestType;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;

    ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (socket;
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            output.flush();
            input.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                    "maxdepth=20;maxrefs=10000;maxbytes=1048576;it.project.mail.common.*;java.base/*;!*"));
            if (!(input.readObject() instanceof RequestType request) || !MailAddress.isValid(request.getEmail())) {
                output.writeObject(Boolean.FALSE);
                return;
            }
            Object response = handle(request, input);
            output.writeObject(response);
            output.flush();
        } catch (IOException | ClassNotFoundException exception) {
            server.logMessage("Client request failed: " + exception.getMessage());
        }
    }

    private Object handle(RequestType request, ObjectInputStream input) throws IOException, ClassNotFoundException {
        return switch (request.getType()) {
            case RequestType.LOGIN -> {
                server.logMessage("Client connected: " + request.getEmail());
                yield new ArrayList<>(server.getBox(request.getEmail()).getMessages());
            }
            case RequestType.RECEIVE -> new ArrayList<>(server.getBox(request.getEmail()).getMessages());
            case RequestType.SEND -> send(request, input);
            case RequestType.DELETE -> delete(request, input);
            case RequestType.DISCONNECT -> {
                server.logMessage("Client disconnected: " + request.getEmail());
                yield Boolean.TRUE;
            }
            default -> Boolean.FALSE;
        };
    }

    private boolean send(RequestType request, ObjectInputStream input) throws IOException, ClassNotFoundException {
        if (!(input.readObject() instanceof Email email) || !Objects.equals(email.getSender(), request.getEmail())
                || email.getDate() == null || email.getSubject() == null || email.getText() == null) {
            return false;
        }
        List<String> recipients = email.getRecipients();
        if (recipients == null || recipients.isEmpty() || recipients.stream().anyMatch(address -> !server.hasBox(address))) {
            return false;
        }
        for (String address : recipients.stream().distinct().toList()) {
            server.getBox(address).addMessage(email);
            server.logMessage("Message delivered to " + address);
        }
        return true;
    }

    private boolean delete(RequestType request, ObjectInputStream input) throws IOException, ClassNotFoundException {
        if (!(input.readObject() instanceof Email email)) {
            return false;
        }
        boolean deleted = server.getBox(request.getEmail()).removeMessage(email);
        if (deleted) {
            server.logMessage("Message deleted from " + request.getEmail());
        }
        return deleted;
    }
}
