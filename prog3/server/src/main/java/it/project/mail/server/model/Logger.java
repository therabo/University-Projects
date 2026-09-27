package it.project.mail.server.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class Logger implements AutoCloseable {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private final BufferedWriter writer;

    Logger(Path file) throws IOException {
        writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        append("New server execution started");
    }

    synchronized String append(String message) throws IOException {
        String entry = LocalDateTime.now().format(FORMAT) + " | " + message;
        writer.write(entry);
        writer.newLine();
        writer.flush();
        return entry;
    }

    @Override
    public synchronized void close() throws IOException {
        writer.close();
    }
}
