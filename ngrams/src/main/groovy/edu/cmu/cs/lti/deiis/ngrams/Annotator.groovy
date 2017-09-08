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

package edu.cmu.cs.lti.deiis.ngrams

import edu.cmu.cs.lti.deiis.core.annotator.AbstractService
import edu.cmu.cs.lti.deiis.core.model.NGram
import edu.cmu.cs.lti.deiis.core.model.Types
import groovy.util.logging.Slf4j
import org.lappsgrid.metadata.ServiceMetadataBuilder
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.Uri

@Slf4j("logger")
class Annotator extends AbstractService {

    NGram.Type type

    private int id

    public Annotator() {
        super(Annotator.class)
    }

    public Annotator(NGram.Type type) {
        super(Annotator.class)
        this.type = type
    }

    public Annotator(int size) {
        super(Annotator.class)
        type = NGram.Type.valueOf(size)
    }

    protected ServiceMetadataBuilder configure(ServiceMetadataBuilder builder) {
        return builder.name(this.class.name)
                .require(Types.QUESTION)
                .require(Types.ANSWER)
                .produce(Types.NGRAM)
    }

    String execute(String input) {
        Data data = Serializer.parse(input,Data)
        if (Uri.ERROR == data.discriminator) {
            return input
        }
        if (Uri.LIF != data.discriminator) {
            return unsupported(data.discriminator)
        }
        if (data.parameters?.type) {
            logger.info("type set in data.parameters: {}", data.parameters.type )
            type = NGram.Type.valueOf(data.parameters.type)
        }
        if (type == null) {
            logger.error("NGram type has not been specified")
            return error("NGram type has not been specified")
        }
        logger.info("Creating NGrams of type: {}", type)
        Container container = new Container((Map) data.payload)

        long start = timestamp()
        List<Annotation> answers = find(container, Types.ANSWER)
        View view = container.newView("ngrams")
        view.addContains(Types.NGRAM, this.class.name, type.toString())

        id = 0
        answers.each { Annotation answer ->
            createNGrams(container, view, answer)
        }
        find(container, Types.QUESTION).each { Annotation question ->
            createNGrams(container, view, question)
        }
//        if (container.metadata == null) {
//            container.metadata = [:]
//        }
        container.metadata[type] = [
                duration: timestamp() - start,
                answers: answers.size(),
                created: view.annotations.size()
        ]
        container.setMetadata('ngram', type.n())
        data.payload = container
        return data.asPrettyJson()
    }

    protected boolean tokenInSpan(Annotation a , Annotation span) {
        return a.atType == Uri.TOKEN && span.start <= a.start && a.end <= span.end
    }

    protected void createNGrams(Container container, View scoreView, Annotation span) {
        List<View> views = container.findViewsThatContain(Uri.TOKEN)
        if (views.size() < 1) {
            return
        }
        View view = views[0]
        // Find all the annotation covered by the span.
        List<Annotation> tokens = view.annotations.findAll { tokenInSpan(it, span) }
        Iterator<Annotation> it = tokens.iterator()
        List<String> ngrams = []
        NGram ngram = new NGram(type)
        // Prime the ngram with the first n-1 tokens
        int n = type.n() - 1
        while (n > 0 && it.hasNext()) {
            ngram << it.next() //.features.string
            --n
        }

        while (it.hasNext()) {
            ngram << it.next() //.features.string
            ngrams << ngram.toString()
            Annotation a = ngram.annotate(type.toString() + "-" + (++id))
            scoreView.add(a)
        }
//        logger.debug("Setting feature {} on span {}", type, span.id)
//        span.features[type] = [score:0, rank: 0, ngrams: ngrams]
    }

    protected List<Annotation> find(Container container, String type) {
        List<Annotation> result = null
        List<View> views = container.findViewsThatContain(type)
        if (views.size() == 0) {
            return result
        }
        return views[0].annotations.findAll{ it.atType == type }
    }
}
