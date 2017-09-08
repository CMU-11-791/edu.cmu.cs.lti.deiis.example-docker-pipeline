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

package edu.cmu.cs.lti.deiis.example

import edu.cmu.cs.lti.deiis.core.annotator.AbstractService

import edu.cmu.cs.lti.deiis.core.model.Types
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.Uri
import org.lappsgrid.metadata.ServiceMetadataBuilder

class Tokenizer extends AbstractService {

    public Tokenizer() {
        super(Tokenizer.class)
    }

    protected ServiceMetadataBuilder configure(ServiceMetadataBuilder builder) {
        return builder.name(this.class.name)
                .produce(Uri.TOKEN)
    }

    String execute(String input) {
        Data data = Serializer.parse(input, Data)
        if (Uri.ERROR == data.discriminator) {
            return input
        }
        if (Uri.LIF != data.discriminator) {
            return unsupported(data.discriminator)
        }

        long startTime = timestamp()
        Container container = new Container((Map) data.payload)
        View view = container.newView("tokens")
        Map contains = [:]
        contains[Uri.TOKEN] = [ producer: this.class.name ]
        view.metadata.contains = contains
        int start = 0
        int id = 0
        container.text.eachLine { String line ->
            int n = line.length() - 1
            String[] tokens = line.substring(0,n).split(' ')
            tokens.each { String token ->
                Annotation a = view.newAnnotation("tok-${id++}", Uri.TOKEN)
                a.start = start
                a.end = start + token.length()
                start = a.end + 1
                Map f = a.features
                f.string = token
                f.componentId = this.class.name
                f.confidence = 1.0f
            }
            Annotation period = view.newAnnotation("tok-${id++}", Types.PUNCTUATION)
            period.start = start - 1
            period.end = start
            period.features.string = '.'
            period.features.componentId = this.class.name
            period.features.confidence = 1.0f
            ++start
        }
        container.metadata.tokenizer = [
                duration: timestamp() - startTime,
                size: view.annotations.size()
        ]
        data.payload = container
        return data.asPrettyJson()
    }
}
