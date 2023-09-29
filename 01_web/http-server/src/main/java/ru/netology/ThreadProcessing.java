package ru.netology;

import java.io.IOException;

import java.util.Arrays;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ThreadProcessing implements Runnable {

    private Socket clientSocket;
    private List<String> validPaths;
    private HashMap<String, HashMap<String, Handler>> handlersList;
    private List<String> allowedMethods;
    private Request request = new Request();

    public ThreadProcessing(Socket clientSocket, List<String> validPaths, HashMap<String, HashMap<String, Handler>> handlerList, List<String> allowedMethods) {
        this.clientSocket = clientSocket;
        this.validPaths = validPaths;
        this.handlersList = handlerList;
        this.allowedMethods = allowedMethods;
    }

    @Override
    public void run() {
        try (
                final BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());
                final var in = new BufferedInputStream(clientSocket.getInputStream());
        ) {

            final var limit = 4096;

            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);

            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                clientSocket.close();
            }

            // читаем request line
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                clientSocket.close();
            }

            request.setMethod(requestLine[0]);
            if (!allowedMethods.contains(request.getMethod())) {
                badRequest(out);
                clientSocket.close();
            }
            System.out.println(request.getMethod());

            request.setPath(requestLine[1]);
            if (!request.getPath().startsWith("/")) {
                badRequest(out);
                clientSocket.close();
            }
            System.out.println(request.getPath());


            // ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                clientSocket.close();
            }

            // отматываем на начало буфера
            in.reset();
            // пропускаем requestLine
            in.skip(headersStart);


            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
            System.out.println(headers);

            if (handlersList.get(request.getMethod()).containsKey(request.getPath())) {
                synchronized (handlersList) {
                    handlersList.get(request.getMethod()).get(request.getPath()).handle(request, out);
                }
            }
//
//            // для GET тела нет
//            if (!request.getMethod().equals(Server.GET)) {
//                in.skip(headersDelimiter.length);
//                // вычитываем Content-Length, чтобы прочитать body
//                final var contentLength = extractHeader(headers, "Content-Length");
//                if (contentLength.isPresent()) {
//                    final var length = Integer.parseInt(contentLength.get());
//                    final var bodyBytes = in.readNBytes(length);
//
//                    request.setBody(new String(bodyBytes));
//                    System.out.println(request.getQueryParams());
//                }
//            }

            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();


//            final var requestLine = in.readLine();
//            final var parts = requestLine.split(" ");
//
//            if (parts.length != 3) {
//                // just close socket
//                clientSocket.close();
//            }
//
//            Request request = new Request(parts[0], parts[1], parts[2]);
//
//            final var path = request.getPath();
//            if (!validPaths.contains(path)) {
//                out.write((
//                        "HTTP/1.1 404 Not Found\r\n" +
//                                "Content-Length: 0\r\n" +
//                                "Connection: close\r\n" +
//                                "\r\n"
//                ).getBytes());
//                out.flush();
//                clientSocket.close();
//            }
//
//            if (handlersList.get(request.getMethod()).containsKey(request.getPath())) {
//                synchronized (handlersList) {
//                    handlersList.get(request.getMethod()).get(request.getPath()).handle(request, out);
//                }
//            } else {
//                defaultHandler(request, out);
//            }


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

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

}
