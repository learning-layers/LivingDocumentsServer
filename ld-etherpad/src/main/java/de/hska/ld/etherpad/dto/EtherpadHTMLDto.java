package de.hska.ld.etherpad.dto;

public class EtherpadHTMLDto {
    //{code: 0, message:"ok", data: {text:"Welcome Text"}}
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
        String html;

        public String getHTML() {
            return html;
        }

        public void setHTML(String html) {
            this.html = html;
        }
    }
}
