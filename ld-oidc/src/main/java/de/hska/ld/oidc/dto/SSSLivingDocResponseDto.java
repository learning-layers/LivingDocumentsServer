package de.hska.ld.oidc.dto;

public class SSSLivingDocResponseDto {
    String op;
    SSSLivingdoc livingDoc;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public SSSLivingdoc getLivingDoc() {
        return livingDoc;
    }

    public void setLivingDoc(SSSLivingdoc livingDoc) {
        this.livingDoc = livingDoc;
    }
}
