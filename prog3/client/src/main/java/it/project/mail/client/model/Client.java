package it.project.mail.client.model;

import it.project.mail.common.Email;
import it.project.mail.common.MailAddress;
import it.project.mail.common.RequestType;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public final class Client {
    private final String host;
    private final int port;
    private volatile String address;

    public Client() {
        this("127.0.0.1", 4040);
    }

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public List<Email> sendLogin(String address) {
        if (!isValidEmail(address)) {
            throw new IllegalArgumentException("Invalid email address");
        }
        List<Email> inbox = emails(exchange(new RequestType(address, RequestType.LOGIN), null));
        this.address = address;
        return inbox;
    }

    public boolean sendEmail(Email email) {
        String activeAddress = address;
        if (activeAddress == null || email.getRecipients() == null || email.getRecipients().isEmpty()
                || email.getRecipients().stream().anyMatch(recipient -> !isValidEmail(recipient))) {
            return false;
        }
        email.setSender(activeAddress);
        email.setDate();
        try {
            return Boolean.TRUE.equals(exchange(new RequestType(activeAddress, RequestType.SEND), email));
        } catch (IllegalStateException exception) {
            return false;
        }
    }

    public boolean cancelEmail(Email email) {
        String activeAddress = address;
        return activeAddress != null
                && Boolean.TRUE.equals(exchange(new RequestType(activeAddress, RequestType.DELETE), email));
    }

    public List<Email> receivedEmail(String address) {
        if (!address.equals(this.address)) {
            throw new IllegalArgumentException("Mailbox does not match the active account");
        }
        return emails(exchange(new RequestType(address, RequestType.RECEIVE), null));
    }

    public void sendDisconnect() {
        String activeAddress = address;
        if (activeAddress != null) {
            try {
                exchange(new RequestType(activeAddress, RequestType.DISCONNECT), null);
            } catch (IllegalStateException ignored) {
            } finally {
                address = null;
            }
        }
    }

    public static boolean isValidEmail(String address) {
        return MailAddress.isValid(address);
    }

    private Object exchange(RequestType request, Email payload) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
            socket.setSoTimeout(5000);
            try (ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {
                output.flush();
                try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                    input.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                            "maxdepth=20;maxrefs=10000;maxbytes=1048576;it.project.mail.common.*;java.base/*;!*"));
                    output.writeObject(request);
                    if (payload != null) {
                        output.writeObject(payload);
                    }
                    output.flush();
                    return input.readObject();
                }
            }
        } catch (IOException | ClassNotFoundException exception) {
            throw new IllegalStateException("Unable to communicate with mail server", exception);
        }
    }

    private static List<Email> emails(Object response) {
        if (!(response instanceof List<?> list) || list.stream().anyMatch(item -> !(item instanceof Email))) {
            throw new IllegalStateException("Unexpected mail server response");
        }
        List<Email> result = new ArrayList<>();
        for (Object item : list) {
            result.add((Email) item);
        }
        return result;
    }
}
