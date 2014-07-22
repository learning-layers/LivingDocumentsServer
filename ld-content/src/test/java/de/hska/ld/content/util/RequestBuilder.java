package de.hska.ld.content.util;

public class RequestBuilder {

    public static String buildCombinedRequestParams(String... requestParams) {
        String combinedString = "?" + requestParams[0];
        for (int i=1;i<requestParams.length;i++) {
            combinedString = combinedString + "&" + requestParams[i];
        }
        return combinedString;
    }
}
