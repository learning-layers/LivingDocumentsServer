package de.hska.ld.etherpad.dto;

public class EtherpadAuthorDto {
    // {"code":0,"message":"ok","data":{"authorID":"a.unuYg2A8XHecmx7K"}}

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
        String authorID;

        public String getAuthorID() {
            return authorID;
        }

        public void setAuthorID(String authorID) {
            this.authorID = authorID;
        }
    }
}
