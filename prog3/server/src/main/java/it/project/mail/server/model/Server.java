package it.project.mail.server.model;

import it.project.mail.common.MailAddress;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class Server implements AutoCloseable {
    private final ServerSocket serverSocket;
    private final ExecutorService clients = Executors.newFixedThreadPool(8);
    private final Map<String, Mailbox> mailboxes = new HashMap<>();
    private final Path dataDirectory;
    private final Logger logger;
    private final Consumer<String> logSink;
    private final Thread acceptThread;
    private volatile boolean running = true;

    public Server(int port, Path dataDirectory, Consumer<String> logSink) throws IOException {
        this.dataDirectory = dataDirectory.toAbsolutePath().normalize();
        this.logSink = logSink;
        Files.createDirectories(this.dataDirectory);
        logger = new Logger(this.dataDirectory.resolve("server.log"));
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"));
        } catch (IOException exception) {
            logger.close();
            throw exception;
        }
        acceptThread = new Thread(this::acceptClients, "mail-server-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
        logMessage("Server listening on port " + getPort());
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    private void acceptClients() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(5000);
                clients.execute(new ClientHandler(socket, this));
            } catch (SocketException exception) {
                if (running) {
                    logMessage("Connection error: " + exception.getMessage());
                }
            } catch (IOException exception) {
                if (running) {
                    logMessage("Connection error: " + exception.getMessage());
                }
            }
        }
    }

    synchronized Mailbox getBox(String address) throws IOException {
        if (!MailAddress.isValid(address)) {
            throw new IOException("Invalid mailbox address");
        }
        Mailbox mailbox = mailboxes.get(address);
        if (mailbox == null) {
            boolean created = !Mailbox.exists(dataDirectory, address);
            mailbox = new Mailbox(dataDirectory, address);
            mailboxes.put(address, mailbox);
            if (created) {
                logMessage("New mailbox created: " + address);
            }
        }
        return mailbox;
    }

    boolean hasBox(String address) {
        return MailAddress.isValid(address) && Mailbox.exists(dataDirectory, address);
    }

    void logMessage(String message) {
        try {
            logSink.accept(logger.append(message));
        } catch (IOException exception) {
            System.err.println("Unable to write server log: " + exception.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        serverSocket.close();
        clients.shutdownNow();
        try {
            clients.awaitTermination(5, TimeUnit.SECONDS);
            acceptThread.join(5000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        logger.close();
    }
}
