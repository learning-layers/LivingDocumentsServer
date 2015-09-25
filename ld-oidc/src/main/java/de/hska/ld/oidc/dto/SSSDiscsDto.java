package de.hska.ld.oidc.dto;

import java.util.List;

public class SSSDiscsDto {
    String op;
    List<SSSDiscDto> discs;

    public List<SSSDiscDto> getDiscs() {
        return discs;
    }

    public void setDiscs(List<SSSDiscDto> discs) {
        this.discs = discs;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }
}
