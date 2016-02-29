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

package de.hska.ld.oidc.dto;

import java.util.List;

public class SSSCircleInfoWrapper {
    private String op;
    private List<SSSCircleItem> circles;
    private List<SSSCircleItem> orphans;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public List<SSSCircleItem> getCircles() {
        return circles;
    }

    public void setCircles(List<SSSCircleItem> circles) {
        this.circles = circles;
    }

    public List<SSSCircleItem> getOrphans() {
        return orphans;
    }

    public void setOrphans(List<SSSCircleItem> orphans) {
        this.orphans = orphans;
    }
}
