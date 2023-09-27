package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
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
        server.runServer();
    }
}


