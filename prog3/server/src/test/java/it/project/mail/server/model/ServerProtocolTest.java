package it.project.mail.server.model;

import it.project.mail.common.Email;
import it.project.mail.common.RequestType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerProtocolTest {
    @TempDir
    Path directory;

    @Test
    void handlesLoginDeliveryRefreshDeletionAndShutdown() throws Exception {
        List<String> logs = new CopyOnWriteArrayList<>();
        Path storage = directory.resolve("runtime");
        try (Server server = new Server(0, storage, logs::add)) {
            int port = server.getPort();
            assertEquals(List.of(), request(port, "alice@example.it", RequestType.LOGIN, null));

            Email email = new Email("alice@example.it", List.of("bob@example.it"), "Hello", "Message");
            email.setDate();
            assertEquals(Boolean.FALSE, request(port, "alice@example.it", RequestType.SEND, email));

            assertEquals(List.of(), request(port, "bob@example.it", RequestType.LOGIN, null));
            assertEquals(Boolean.TRUE, request(port, "alice@example.it", RequestType.SEND, email));
            List<?> inbox = (List<?>) request(port, "bob@example.it", RequestType.RECEIVE, null);
            assertEquals(1, inbox.size());
            Email received = (Email) inbox.getFirst();
            assertEquals(email.getId(), received.getId());

            assertEquals(Boolean.TRUE, request(port, "bob@example.it", RequestType.DELETE, received));
            assertEquals(Boolean.FALSE, request(port, "bob@example.it", RequestType.DELETE, received));
            assertEquals(List.of(), request(port, "bob@example.it", RequestType.RECEIVE, null));
            assertEquals(Boolean.FALSE, request(port, "../server.log", RequestType.LOGIN, null));
            assertEquals(Boolean.TRUE, request(port, "alice@example.it", RequestType.DISCONNECT, null));
        }
        assertTrue(Files.isRegularFile(storage.resolve("server.log")));
        assertTrue(logs.stream().anyMatch(entry -> entry.contains("Message delivered")));
        assertFalse(Files.exists(directory.resolve("server.log")));
    }

    private static Object request(int port, String address, int type, Email email) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket("127.0.0.1", port);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            output.writeObject(new RequestType(address, type));
            if (email != null) {
                output.writeObject(email);
            }
            output.flush();
            return input.readObject();
        }
    }
}
