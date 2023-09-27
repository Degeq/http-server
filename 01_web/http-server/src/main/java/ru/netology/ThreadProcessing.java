package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class ThreadProcessing implements Runnable {

    private Socket clientSocket;
    private List<String> validPaths;
    private HashMap<String, HashMap<String, Handler>> handlersList;

    public ThreadProcessing(Socket clientSocket, List<String> validPaths, HashMap<String, HashMap<String, Handler>> handlerList) {
        this.clientSocket = clientSocket;
        this.validPaths = validPaths;
        this.handlersList = handlerList;
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

            if (handlersList.get(request.getMethod()).containsKey(request.getPath())) {
                synchronized (handlersList) {
                    handlersList.get(request.getMethod()).get(request.getPath()).handle(request, out);
                }
            } else {
                defaultHandler(request, out);
            }


        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void defaultHandler(Request request, BufferedOutputStream out) {
        try {
            final var filePath = Path.of(".", "http-server/public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
