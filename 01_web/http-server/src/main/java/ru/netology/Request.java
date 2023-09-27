package ru.netology;

public class Request {
    private String method;
    private String path;
    private String body;

    public Request(String method, String path, String body) {
        this.method = method;
        this.path = path;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }
}
