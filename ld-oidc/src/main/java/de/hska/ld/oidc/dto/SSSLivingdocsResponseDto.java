package de.hska.ld.oidc.dto;

public class SSSLivingdocsResponseDto {
    String op;
    String livingDoc;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getLivingDoc() {
        return livingDoc;
    }

    public void setLivingDoc(String livingDoc) {
        this.livingDoc = livingDoc;
    }

    @Override
    public String toString() {
        return "SSSLivingdocsResponseDto{" +
                "op='" + op + '\'' +
                ", livingDoc='" + livingDoc + '\'' +
                '}';
    }
}
