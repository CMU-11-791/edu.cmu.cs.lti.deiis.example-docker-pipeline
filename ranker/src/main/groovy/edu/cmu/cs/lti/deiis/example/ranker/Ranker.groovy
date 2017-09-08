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

package edu.cmu.cs.lti.deiis.example.ranker

import edu.cmu.cs.lti.deiis.core.annotator.AbstractService
import edu.cmu.cs.lti.deiis.core.model.Features
import edu.cmu.cs.lti.deiis.core.model.Types
import groovy.util.logging.Slf4j
import org.lappsgrid.metadata.ServiceMetadataBuilder
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.*

/**
 * <p>Ranks (sorts) a set of candidate Answer annotations based on a given feature
 * name. The feature name can be provided in the constructor or passed in the
 * data parameters. A feature name provided in the data parameters takes
 * precedence over a feature name provided in the constructor.</p>
 * <code>
 *     Ranker
 * </code>
 *
 */
@Slf4j('logger')
class Ranker extends AbstractService {

    /**
     * The feature to sort on.
     */
    String feature

    public Ranker() {
        this(Features.SCORE)
    }

    public Ranker(String feature) {
        super(Ranker.class)
        this.feature = feature
    }

    protected ServiceMetadataBuilder configure(ServiceMetadataBuilder builder) {
        return builder.name(this.class.name)
                .require(Types.SCORE)
                .produce(Types.RANK)
    }

    String execute(String input) {
        Data data = Serializer.parse(input, Data)
        if (Uri.ERROR == data.discriminator) {
            logger.warn("Received an ERROR data package.")
            return input
        }
        if (Uri.LIF != data.discriminator) {
            logger.warn("Received unsupported discriminator {}", data.discriminator)
            return unsupported(data.discriminator)
        }
        if (data.parameters?.feature) {
            this.feature = data.parameters.feature
            logger.info("Feature found in data.parameters: {}", this.feature)
        }

        if (feature == null) {
            logger.error("No feature name has been provided.")
            return error("No feature name has been provided.")
        }

        long start = timestamp()
        Container container = new Container((Map)data.payload)
        List<View> views = container.findViewsThatContain(Types.ANSWER)
        if (views.size() == 0) {
            return error("No views contain Answer annotations")
        }
        View view = views[0]
        List<Annotation> answers = view.annotations.findAll { it.atType == Types.ANSWER }
        if (answers.size() == 0) {
            return error("No answers found.")
        }
        StringWriter stringWriter = new StringWriter()
        PrintWriter writer = new PrintWriter(stringWriter)
        logger.debug("Sorting by feature {}", feature)
        answers.sort { it.features[feature] }.reverse().eachWithIndex { answer, i ->
            logger.info("Ranked: {} is answer {}", answer.id, answer.features.isAnswer)
            answer.features.rank = i + 1
            if (answer.features.isAnswer) {
                writer.print '1 '
            }
            else {
                writer.print '0 '
            }

            writer.print answer.features[feature]
            writer.print ' '
            writer.println answer.features.text
        }

        if (container.metadata == null) {
            container.metadata = [:]
        }
        container.metadata.ranking = [
                duration: timestamp() - start,
                size: answers.size(),
                text: stringWriter.toString()
        ]
        data.payload = container
        return data.asPrettyJson()
    }
}
