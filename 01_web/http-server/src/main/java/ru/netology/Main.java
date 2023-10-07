package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static final String ENCTYPE_URL = "application/x-www-form-urlencoded";
    public static final String ENCTYPE_MULTIPART = "multipart/form-data";

    public static void main(String[] args) throws IOException {
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        final var server = new Server();

        //Добавляем обработчик только для файлов, которые обрабатываются "не дефолтно"
        server.addHandler("GET", "/classic.html", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream out) {
                try {
                    final var filePath = Path.of(".", "http-server/public", request.getPath());
                    final var mimeType = Files.probeContentType(filePath);

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
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        server.addHandler("GET", "/message", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream out) {
                try {
                    // выводим на экран результат парсинг QueryString для метода GET
                    System.out.println(request.getQueryParams());
                    System.out.println(request.getSpecificParam("value"));
                    System.out.println(request.getPath());

                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        server.addHandler("POST", "/message", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream out) {
                System.out.println(request.getHeaderByName("Content-Type"));
                if(request.getHeaderByName("Content-Type").equals(ENCTYPE_URL)) {
                    request.parseUrlBody();
                } else { if(request.getHeaderByName("Content-Type").equals(ENCTYPE_MULTIPART)) {

                }}
                try {
                    // выводим на экран результат парсинг QueryString для метода GET
                    System.out.println(request.getPostParamByName("title"));
                    System.out.println(request.getPostParams());

                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        server.runServer();
    }
}


