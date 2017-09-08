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

package edu.cmu.cs.lti.deiis.example.evaluator

import edu.cmu.cs.lti.deiis.core.annotator.AbstractService
import edu.cmu.cs.lti.deiis.core.model.Types
import groovy.util.logging.Slf4j
import org.lappsgrid.metadata.ServiceMetadataBuilder
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

@Slf4j("logger")
class Evaluator extends AbstractService {

    int n

    public Evaluator() {
        super(Evaluator.class)
        n = -1
        logger.debug("Default constructor")
    }

    public Evaluator(int n) {
        super(Evaluator.class)
        this.n = n
        logger.info("Constructor set N: {}", n)
    }

    protected ServiceMetadataBuilder configure(ServiceMetadataBuilder builder) {
        return builder.name(this.class.name)
                .produce(Types.PRF)
    }

    String execute(String input) {
        Data data = Serializer.parse(input, Data)
        if (Types.ERROR == data.discriminator) {
            return input
        }
        if (Types.LIF != data.discriminator ) {
            return unsupported(data.discriminator)
        }
        if (data.parameters?.n) {
            n = data.parameters.n as int
            logger.info("Getting N from data.parameters: {}", n)
        }

        Container container = new Container((Map) data.payload)
        List<View> views = container.findViewsThatContain(Types.ANSWER)
        if (views.size() == 0) {
            String message = "The input document does not contain any answers"
            logger.error(message)
            return error(message)
        }

        long start = timestamp()
        if (views.size() > 1) {
            logger.warn("The input document contains more than one view with Answer annotations. Using the last one found.")
        }
        List<Annotation> answers = views[-1].findByAtType(Types.ANSWER)
                .sort { it.features.rank }

        if (n < 1) {
            logger.debug("Counting answers")
            n = answers.count { it.features.isAnswer }.toInteger()
        }
        logger.info("Ranking N={}", n)
        int correct = 0
//        int i = 0
//        while (i < n) {
//            if (answers[i++].features.isAnswer) {
//                ++correct
//            }
//        }
        answers[0..(n-1)].each { Annotation a ->
            if (a.features.isAnswer) {
                ++correct
            }
        }
        int missed = 0
        answers[n..-1].each { Annotation a ->
            if (a.features.isAnswer) {
                ++missed
            }
        }
        logger.info("Correct: {} Missed: {}", correct, missed)
        float p = correct / n
        float r = correct / (correct + missed)
        float f
        if (p+r == 0) {
            f = 0.0f
        }
        else {
            f = 2 * (p * r) / (p + r)
        }
        long duration = timestamp() - start
        if (container.metadata == null) {
            container.metadata = [:]
        }
        Map evaluator = [
                n: n,

        ]
        container.metadata.evaluator = [
                n: n,
                precision: p,
                recall: r,
                fscore: f,
                duration: duration,
                size: answers.size()
        ]
//        container.metadata.n = n
//        container.metadata.precision = p
//        container.metadata.recall = r
//        container.metadata.fscore = f
        data.payload = container
        return data.asPrettyJson()
    }
}
