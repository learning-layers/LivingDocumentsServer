package de.hska.ld.etherpad.dto;

public class EtherpadGroupPadDto {
    //{"code":0,"message":"ok","data":{"padID":"g.6Afu7ex013SEocl5$Sandbox_Document"}}

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
        String padID;

        public String getPadID() {
            return padID;
        }

        public void setPadID(String padID) {
            this.padID = padID;
        }
    }
}
