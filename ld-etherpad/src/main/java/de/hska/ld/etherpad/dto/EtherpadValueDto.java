package de.hska.ld.etherpad.dto;

import java.util.Map;

public class EtherpadValueDto {
    AttributeText atext;
    Pool pool;

    public class AttributeText {
        private String text;
        private String attribs;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getAttribs() {
            return attribs;
        }

        public void setAttribs(String attribs) {
            this.attribs = attribs;
        }
    }

    public class Pool {
        Map<String, String[]> numToAttrib;

        public Map<String, String[]> getNumToAttrib() {
            return numToAttrib;
        }

        public void setNumToAttrib(Map<String, String[]> numToAttrib) {
            this.numToAttrib = numToAttrib;
        }
    }

    public AttributeText getAtext() {
        return atext;
    }

    public void setAtext(AttributeText atext) {
        this.atext = atext;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }
}
