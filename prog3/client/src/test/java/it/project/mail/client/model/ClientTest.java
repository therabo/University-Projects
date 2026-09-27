package it.project.mail.client.model;

import it.project.mail.common.Email;
import it.project.mail.common.RequestType;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ClientTest {
    @Test
    void waitsForDeliveryConfirmation() throws Exception {
        try (ServerSocket listener = new ServerSocket(0)) {
            CompletableFuture<Void> server = CompletableFuture.runAsync(() -> {
                try {
                    try (Socket socket = listener.accept();
                         ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                         ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                        RequestType request = assertInstanceOf(RequestType.class, input.readObject());
                        assertEquals(RequestType.LOGIN, request.getType());
                        output.writeObject(new ArrayList<Email>());
                        output.flush();
                    }
                    try (Socket socket = listener.accept();
                         ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                         ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                        RequestType request = assertInstanceOf(RequestType.class, input.readObject());
                        assertEquals(RequestType.SEND, request.getType());
                        assertInstanceOf(Email.class, input.readObject());
                        output.writeObject(Boolean.FALSE);
                        output.flush();
                    }
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
            });

            Client client = new Client("127.0.0.1", listener.getLocalPort());
            assertEquals(List.of(), client.sendLogin("alice@example.it"));
            assertFalse(client.sendEmail(new Email(null, List.of("bob@example.it"), "Hello", "Body")));
            server.get(5, TimeUnit.SECONDS);
        }
    }
}
