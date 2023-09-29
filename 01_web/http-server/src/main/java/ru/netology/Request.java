package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Request {
    private char QueryDefaultDelimeter = '&';
    private char PathDefaultDelimeter = '?';
    private String DefaultPairDelimeter = "=";
    private String method;
    private String path;
    private String body;
    private HashMap<String, List<String>> queryParams = new HashMap<>();

    public Request() {
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

    public void setBody(String body) {
        this.body = body;
        String[] nameValuePairs = body.split(String.valueOf(QueryDefaultDelimeter));

        for (int i = 0; i < nameValuePairs.length; i++) {
            String[] pair = nameValuePairs[i].split(DefaultPairDelimeter);
            if (queryParams.containsKey(pair[0])) {
                queryParams.get(pair[0]).add(pair[1]);
            } else {
                queryParams.put(pair[0], new ArrayList<>());
                queryParams.get(pair[0]).add(pair[1]);
            }
        }

    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
        List<NameValuePair> queryList = URLEncodedUtils.parse(String.valueOf(path.toCharArray()), StandardCharsets.UTF_8, PathDefaultDelimeter, QueryDefaultDelimeter);
        for (int i = 0; i < queryList.size(); i++) {
            if (i == 0) {
                this.path = queryList.get(i).getName();
            } else {
                if (queryParams.containsKey(queryList.get(i).getName())) {
                    queryParams.get(queryList.get(i).getName()).add(queryList.get(i).getValue());
                } else {
                    queryParams.put(queryList.get(i).getName(), new ArrayList<>());
                    queryParams.get(queryList.get(i).getName()).add(queryList.get(i).getValue());
                }
            }
        }
    }

    public HashMap<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public List<String> getSpecificParam(String paramName) {
        return queryParams.get(paramName);
    }
}
