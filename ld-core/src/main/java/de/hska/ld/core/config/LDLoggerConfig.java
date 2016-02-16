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

package de.hska.ld.core.config;

import org.pmw.tinylog.Configurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.*;

@Configuration
public class LDLoggerConfig {

    @Autowired
    private Environment env;

    @Autowired
    private void init() throws IOException {
        File f = File.createTempFile("tinylog_conf", ".txt");
        FileOutputStream fos = new FileOutputStream(f);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        try {
            String writer = env.getProperty("tinylog.writer");
            bw.write("tinylog.writer = " + writer);
            bw.newLine();
            /*String fileName = env.getProperty("tinylog.writer.filename");
            bw.write("tinylog.writer.filename = " + fileName);
            bw.newLine();
            String buffered = env.getProperty("tinylog.writer.buffered");
            bw.write("tinylog.writer.buffered = " + buffered);
            bw.newLine();
            String append = env.getProperty("tinylog.writer.append");
            bw.write("tinylog.writer.append = " + append);
            bw.newLine();*/
            String level = env.getProperty("tinylog.level");
            bw.write("tinylog.level = " + level);
            bw.newLine();
            String writerUrl = env.getProperty("tinylog.writer.url");
            bw.write("tinylog.writer.url = " + writerUrl);
            bw.newLine();
            String writerTable = env.getProperty("tinylog.writer.table");
            bw.write("tinylog.writer.table = " + writerTable);
            bw.newLine();
            String writerColumns = env.getProperty("tinylog.writer.columns");
            bw.write("tinylog.writer.columns = " + writerColumns);
            bw.newLine();
            String writerValues = env.getProperty("tinylog.writer.values");
            bw.write("tinylog.writer.values = " + writerValues);
            bw.newLine();
            String writerBatch = env.getProperty("tinylog.writer.batch");
            bw.write("tinylog.writer.batch = " + writerBatch);
            bw.newLine();
            String writerUsername = env.getProperty("tinylog.writer.username");
            bw.write("tinylog.writer.username = " + writerUsername);
            bw.newLine();
            String writerPassword = env.getProperty("tinylog.writer.password");
            bw.write("tinylog.writer.password = " + writerPassword);
            bw.newLine();
            String writingThread = env.getProperty("tinylog.writingthread");
            bw.write("tinylog.writingthread = " + writingThread);
            bw.newLine();
            String wTObserve = env.getProperty("tinylog.writingthread.observe");
            bw.write("tinylog.writingthread.observe = " + wTObserve);
            bw.newLine();
            String wTPriority = env.getProperty("tinylog.writingthread.priority");
            bw.write("writingthread.priority = " + wTPriority);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bw.close();
        }
        Configurator.fromFile(f).activate();
    }
}
