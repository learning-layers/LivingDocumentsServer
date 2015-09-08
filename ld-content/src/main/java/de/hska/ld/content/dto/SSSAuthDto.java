package de.hska.ld.content.dto;

public class SSSAuthDto {
    /*{
        "op": "authCheckCred",
            "user": "http://sss.eu/<id>",
            "key": ""
    }*/
    String op;
    String user;
    String key;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "SSSAuthDto{" +
                "op='" + op + '\'' +
                ", user='" + user + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
