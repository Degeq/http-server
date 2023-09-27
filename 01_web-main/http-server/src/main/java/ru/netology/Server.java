package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static ExecutorService threadPool = Executors.newFixedThreadPool(16);
    private int port;
    private List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
//    private HashMap<String, HashMap<String, Handler>> handlerList = new HashMap<>();

    public Server() throws IOException {

        System.out.println("Начнем работу сервера. Введите номер порта: ");
//Запись порта в файл вынесена в отдельный метод
        bindingPort();
    }

    private void bindingPort() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String number = scanner.nextLine();

        this.port = Integer.parseInt(number);
    }

    public void runServer() throws IOException {
        //В бесконечном цикле сервер ожидает подключение новых пользователей
        //Каждый вызов создает новый экземпляр ThreadProcessing и запускает его метод run в новом потоке
        while (true) {
            try (ServerSocket server = new ServerSocket(port)) {
                Socket clientSocket = server.accept();
                threadPool.execute(new ThreadProcessing(clientSocket, validPaths));
                System.out.println("Новое подключение");
            } catch (IOException ex) {
                //Если порт оказывается занятым, происходит его переназначение и перезапись в файл settings
                ex.printStackTrace();
                System.out.println("Выбранный порт занят. Введите новый: ");
                bindingPort();
            }
        }
    }
}
