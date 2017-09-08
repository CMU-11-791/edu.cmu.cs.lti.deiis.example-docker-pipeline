/*
 * Copyright (c) 2017. Carnegie Mellon University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.cmu.cs.lti.deiis.example.summary

import edu.cmu.cs.lti.deiis.core.annotator.AbstractService
import edu.cmu.cs.lti.deiis.core.model.Types
import groovy.util.logging.Slf4j
import org.lappsgrid.metadata.ServiceMetadataBuilder
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container

import static org.lappsgrid.discriminator.Discriminators.Uri

@Slf4j("logger")
class RankingSummary extends AbstractService {
    RankingSummary() {
        super(RankingSummary.class)
    }

    @Override
    protected ServiceMetadataBuilder configure(ServiceMetadataBuilder builder) {
        return builder.name(this.class.name)
                .require(Types.RANK)
                .produceFormat('text/plain')

    }

    @Override
    String execute(String input) {
        Data data = Serializer.parse(input)
        if (Uri.ERROR == data.discriminator) {
            logger.warn("Received an ERROR data package.")
            return input
        }
        if (Uri.LIF != data.discriminator) {
            logger.warn("Received unsupported discriminator {}", data.discriminator)
            return unsupported(data.discriminator)
        }
        Container container = new Container((Map) data.payload)
        if (container.metadata == null || container.metadata.ranking == null) {
            logger.error("No ranking data found")
            return 'No ranking data found.'
        }
        return container.metadata.ranking.text
//        return Serializer.toPrettyJson(container.metadata)
    }
}
