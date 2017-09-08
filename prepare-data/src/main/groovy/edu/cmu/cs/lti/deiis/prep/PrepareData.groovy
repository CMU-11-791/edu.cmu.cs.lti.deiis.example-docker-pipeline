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

package edu.cmu.cs.lti.deiis.prep

import edu.cmu.cs.lti.deiis.core.annotator.AbstractService
import edu.cmu.cs.lti.deiis.core.model.Types
import groovy.util.logging.Slf4j
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.*
import org.lappsgrid.metadata.ServiceMetadataBuilder
import org.lappsgrid.serialization.Data

@Slf4j("logger")
class PrepareData extends AbstractService {

    public PrepareData() {
        super(PrepareData.class)
    }

    protected ServiceMetadataBuilder configure(ServiceMetadataBuilder builder) {
        return builder.name(this.class.name)
                .requireFormat("text/plain")
                .produce(Types.QUESTION)
                .produce(Types.ANSWER)
    }

    String execute(String input) {
        long startTime = timestamp()
        Container container = new Container();
        View view = container.newView("questions")
        view.metadata.contains = [:]
        view.metadata.contains[Types.QUESTION] = [
                producer: this.class.name,
                type: 'question'
        ]
        view.metadata.contains[Types.ANSWER] = [
                producer: this.class.name,
                type: 'answer'
        ]
        StringWriter buffer = new StringWriter()
        PrintWriter writer = new PrintWriter(buffer)
        Iterator<String> it = input.readLines().iterator()
        String questionText = it.next()
        if (questionText.startsWith('Q ')) {
            questionText = questionText.substring(2)
        }
        else {
            logger.warn("Malformed question does not begin with 'Q'")
        }

        writer.println(questionText)
        Annotation a = view.newAnnotation("q-0", Types.QUESTION)
        a.start = 0
        a.end = questionText.length()
        a.features.componentId = this.class.name
        a.features.confidence = 1.0f
        a.features.text = questionText

        int start = a.end + 1
        int id = 0
        while (it.hasNext()) {
            String line = it.next()
            logger.debug("Line: {}", line)
            if (line == null || line.length() == 0) continue

            boolean isAnswer = line.substring(2, 3) == '1'
            String answerText = line.substring(4)
            writer.println(answerText)
            a = view.newAnnotation("a-${id++}", Types.ANSWER)
            a.start = start
            a.end = start + answerText.length()
            Map f = a.features
            f.componentId = this.class.name
            f.confidence = 1.0f
            f.text = answerText
            f.isAnswer = isAnswer
            logger.debug("A {}: {}", a.id, a.features.text)
            start = a.end + 1
        }
        logger.debug("Packaging return data")
        container.text = buffer.toString()
        container.metadata.prepare = [
                duration: timestamp() - startTime,
                size: view.annotations.size()
        ]
        Data data = new Data(Uri.LIF, container)
        return data.asPrettyJson()
    }
}
