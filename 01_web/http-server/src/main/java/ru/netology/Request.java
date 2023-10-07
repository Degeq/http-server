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
    private char DefaultPairDelimeter = '=';
    private String method;
    private String path;
    private String body;
    private HashMap<String, List<String>> queryParams = new HashMap<>();
    private HashMap<String, String> headers = new HashMap<>();

    private HashMap<String, List<String>> postParams = new HashMap<>();

    private String delimeter;

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

    public void setHeaders(List<String> headersList) {
        for (String i : headersList) {
            String[] nameAndValue = i.split(": ");
            headers.put(nameAndValue[0], nameAndValue[1]);
        }
    }

    public String getHeaderByName(String headerName) {
        return headers.get(headerName);
    }

    public HashMap<String, String> getAllHeaders() {
        return headers;
    }

    public void setBody(String body) {
        this.body = body;
        encodedParsing(body, postParams);
        System.out.println(postParams.get("image"));
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
        encodedParsing(path, queryParams);
//        List<NameValuePair> queryList = URLEncodedUtils.parse(path, StandardCharsets.UTF_8, PathDefaultDelimeter, QueryDefaultDelimeter);
//        for (int i = 0; i < queryList.size(); i++) {
//            if (i == 0) {
//                this.path = queryList.get(i).getName();
//            } else {
//                if (queryParams.containsKey(queryList.get(i).getName())) {
//                    queryParams.get(queryList.get(i).getName()).add(queryList.get(i).getValue());
//                } else {
//                    queryParams.put(queryList.get(i).getName(), new ArrayList<>());
//                    queryParams.get(queryList.get(i).getName()).add(queryList.get(i).getValue());
//                }
//            }
//        }
    }

    public HashMap<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public List<String> getSpecificParam(String paramName) {
        return queryParams.get(paramName);
    }

    public void setPartDelimeter(String delimeter) {
        this.delimeter = delimeter;
    }

    public void parseUrlBody() {
        String[] pairsBody = body.split("&");
        for (String i : pairsBody) {
            String[] nameAndValue = i.split("=");
            addingInHashMap(nameAndValue[0], nameAndValue[1], queryParams);
        }
    }

    public List<String> getPostParamByName(String name) {
        return postParams.get(name);
    }

    public HashMap<String, List<String>> getPostParams() {
        return postParams;
    }

    private void encodedParsing(String toParse, HashMap<String, List<String>> targetSet) {
        List<NameValuePair> queryList = URLEncodedUtils.parse(toParse, StandardCharsets.UTF_8, QueryDefaultDelimeter, PathDefaultDelimeter, DefaultPairDelimeter);
        for (int i = 0; i < queryList.size(); i++) {
            if (queryList.get(i).getName().startsWith("/")) {
                this.path = queryList.get(i).getName();
            } else {
                addingInHashMap(queryList.get(i).getName(), queryList.get(i).getValue(), targetSet);
            }
        }
    }

    private void addingInHashMap(String name, String value, HashMap<String, List<String>> hashMap) {
        if (hashMap.containsKey(name)) {
            hashMap.get(name).add(value);
        } else {
            hashMap.put(name, new ArrayList<>());
            hashMap.get(name).add(value);
        }
    }
}
