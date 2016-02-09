/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2016, Karlsruhe University of Applied Sciences.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.hska.ld.etherpad.dto;

import java.util.Map;

public class PadValueDto {

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
