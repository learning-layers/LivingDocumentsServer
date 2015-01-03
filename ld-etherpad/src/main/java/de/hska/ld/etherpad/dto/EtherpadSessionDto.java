package de.hska.ld.etherpad.dto;

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

        //{code: 0, message:"ok", data: {authorID: "a.s8oes9dhwrvt0zif", groupID: g.s8oes9dhwrvt0zif,
        // validUntil: 1312201246}} {code: 1, message:"sessionID does not exist", data: null}
        String authorID;
        String groupID;
        Long validUntil;

        //{code: 0, message:"ok", data: {readOnlyID: "r.s8oes9dhwrvt0zif"}}
        String readOnlyID;

        public String getReadOnlyID() {
            return readOnlyID;
        }

        public void setReadOnlyID(String readOnlyID) {
            this.readOnlyID = readOnlyID;
        }

        public String getAuthorID() {
            return authorID;
        }

        public void setAuthorID(String authorID) {
            this.authorID = authorID;
        }

        public String getGroupID() {
            return groupID;
        }

        public void setGroupID(String groupID) {
            this.groupID = groupID;
        }

        public Long getValidUntil() {
            return validUntil;
        }

        public void setValidUntil(Long validUntil) {
            this.validUntil = validUntil;
        }

        public String getSessionID() {
            return sessionID;
        }

        public void setSessionID(String sessionID) {
            this.sessionID = sessionID;
        }
    }
}
