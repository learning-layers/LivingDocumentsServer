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
package de.hska.ld.ldToSSS.config;

import de.hska.ld.ldToSSS.client.RecommClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;


@Configuration
public class RecommConfig {

    /**
     *
     * @return returns new Recommendation Client Object
     */
    @Bean
    public RecommClient recommClient() {
        //This access token was provided to Martin Bachl
        String token_sss = "886be164d6049c40b2e3d6ce7c22b0d0";

        //CHANGE Flag whenever you test module within the Hochschule
        Boolean requiresProxy = true;

        //PROXY personal credentials to access within the Hochschule
        String proxy_username = "vigr1011";
        String proxy_password = "Stellate91";
        try {
            if(requiresProxy) {
                return new RecommClient(token_sss, proxy_username, proxy_password);
            }else{
                return new RecommClient(token_sss);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("There was an error creating client with connection to Social Semantic Server, please contact the IT administrators");
        }
        return null;
    }
}



