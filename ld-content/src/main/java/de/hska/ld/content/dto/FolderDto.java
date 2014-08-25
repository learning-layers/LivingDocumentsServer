/**
 * Code contributed to the Learning Layers project
 * http://www.learning-layers.eu
 * Development is partly funded by the FP7 Programme of the European
 * Commission under Grant Agreement FP7-ICT-318209.
 * Copyright (c) 2014, Karlsruhe University of Applied Sciences.
 * For a list of contributors see the AUTHORS file at the top-level directory
 * of this distribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hska.ld.content.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hska.ld.content.persistence.domain.Folder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FolderDto extends Folder {
    private List<Long> jsonParentIdList = new ArrayList<>();

    @JsonProperty("jsonParentId")
    public List<Long> getJsonParentIdList() {
        if (this.getParentFolderList() != null && this.getParentFolderList().size() > 0) {
            for (Folder parentFolder : this.getParentFolderList()) {
                jsonParentIdList.add(parentFolder.getId());
            }
        }
        return jsonParentIdList;
    }

    @JsonProperty("jsonParentId")
    public void setJsonParentIdList(List<Long> idList) {
        if (this.getParentFolderList() != null && this.getParentFolderList().size() > 0) {
            for (Folder parentFolder : this.getParentFolderList()) {
                jsonParentIdList.add(parentFolder.getId());
            }
        } else {
            jsonParentIdList = idList;
        }
    }

    public FolderDto() {

    }

    public FolderDto(Folder folder) {
        try {
            // extract and set values per reflection
            for (Field folderfield : folder.getClass().getDeclaredFields()) {
                folderfield.setAccessible(true);
                Object obj = folderfield.get(folder);
                folderfield.set(this, obj);
            }
            this.setId(folder.getId());
        } catch (IllegalAccessException e) {
            //
        }
        this.getJsonParentIdList();
    }
}
