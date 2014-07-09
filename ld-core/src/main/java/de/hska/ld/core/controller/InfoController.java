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

package de.hska.ld.core.controller;

import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Properties;

/**
 * <p><b>Resource:</b> {@value Core#RESOURCE_INFO}
 */
@RestController
@RequestMapping(Core.RESOURCE_INFO)
public class InfoController {

    private Info info;

    @Autowired
    public void init(Environment env) {
        try {
            Properties infoProp = PropertiesLoaderUtils.loadAllProperties("info.properties");
            String sandbox = Boolean.parseBoolean(env.getProperty("module.sandbox.enabled")) ? " Sandbox" : "";
            info = new Info(infoProp.getProperty("info.title") + sandbox, infoProp.getProperty("info.version"),
                    infoProp.getProperty("info.organization"));
        } catch (IOException exception) {
        }
    }

    /**
     * <pre>
     * Gets information about the current application version etc.
     *
     * <b>Path:</b> GET {@value Core#RESOURCE_INFO}
     * </pre>
     *
     * @return the application information
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Info> getInfo() {
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    static class Info {

        Info(String title, String version, String organization) {
            this.title = title;
            this.version = version;
            this.organization = organization;
        }

        String title;
        String version;
        String organization;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }
    }
}
