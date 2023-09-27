package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class ThreadProcessing implements Runnable {

    private Socket clientSocket;
    private List<String> validPaths;

    public ThreadProcessing(Socket clientSocket, List<String> validPaths) {
        this.clientSocket = clientSocket;
        this.validPaths = validPaths;
    }

    @Override
    public void run() {
        try (
                final BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());
                final var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                clientSocket.close();
            }

            Request request = new Request(parts[0], parts[1], parts[2]);

            final var path = request.getPath();
            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                clientSocket.close();
            }

            final var filePath = Path.of(".", "http-server/public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);

            if (request.getPath().equals("/classic.html")) {
                // special case for classic
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
            } else {
                final var length = Files.size(filePath);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();
            }


        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
