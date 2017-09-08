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

import org.junit.*
import org.lappsgrid.api.WebService
import org.lappsgrid.metadata.IOSpecification
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.DataContainer
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.*

/**
 * @author Keith Suderman
 */
class TokenizerTest {

    @Test
    void testMetadata() {
        WebService service = new Tokenizer()
        String json = service.getMetadata()
        Data<ServiceMetadata> data = Serializer.parse(json)
        ServiceMetadata md = data.payload
        assert 'https://www.lti.cs.cmu.edu' == md.vendor
        assert '1.0.0' == md.version
        assert 0 == md.requires.annotations.size()
        assert 1 == md.requires.format.size()
        assert Uri.LIF == md.requires.format[0]

        IOSpecification produces = md.produces
        assert 1 == produces.annotations.size()
        assert Uri.TOKEN == produces.annotations[0]
        assert 1 == produces.format.size()
        assert Uri.LIF == produces.format[0]
    }

    @Test
    void testService() {
        InputStream stream = this.class.getResourceAsStream("/prepared.json")
        assert null != stream

        WebService service = new Tokenizer()
        String json = service.execute(stream.text)
        DataContainer dc = Serializer.parse(json, DataContainer)
        assert Uri.LIF == dc.discriminator

        Container container = dc.payload
        View view = container.findViewById("tokens")
        assert null != view
        assert 8 == view.annotations.size()
        Iterator<Annotation> actual = view.annotations.iterator()
        'question . Answer one . Answer two .'.tokenize().each { String expected ->
            assert actual.hasNext()
            assert expected == actual.next().features.string
        }
        assert false == actual.hasNext()
    }
}
