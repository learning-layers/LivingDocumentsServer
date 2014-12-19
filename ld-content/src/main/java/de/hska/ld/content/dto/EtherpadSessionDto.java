package de.hska.ld.content.dto;

public class EtherpadSessionDto {

    //{"code":0,"message":"ok","data":{"sessionID":"s.844a12b28c7298174fe9949d9cf96483"}}

    long code;
    String message;
    Data data;

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {
        String sessionID;

        public String getSessionID() {
            return sessionID;
        }

        public void setSessionID(String sessionID) {
            this.sessionID = sessionID;
        }
    }
}
