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

import edu.cmu.cs.lti.deiis.core.model.Types
import org.junit.*
import org.lappsgrid.api.WebService
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.DataContainer
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import java.lang.annotation.Annotation

import static org.lappsgrid.discriminator.Discriminators.*


/**
 * @author Keith Suderman
 */
class PrepareDataTest {

    String data = '''Q question.
A 1 Answer one.
A 0 Answer two.
'''

    @Test
    void testMetadata() {
        WebService service = new PrepareData()
        String json = service.getMetadata()
        Data<ServiceMetadata> data = Serializer.parse(json)
        ServiceMetadata md = data.payload
        assert 'https://www.lti.cs.cmu.edu' == md.vendor
        assert '1.0.0' == md.version
    }

    @Test
    void testService() {
        WebService service = new PrepareData()
        String json =  service.execute(data)
//        println json

        DataContainer dc = Serializer.parse(json, DataContainer)
        assert Uri.LIF == dc.discriminator
        Container container = dc.payload
        assert 1 == container.views.size()
        View view = container.findViewById('questions')
        assert null != view
        assert 3 == view.annotations.size()

        List<View> list = container.findViewsThatContain(Types.QUESTION)
        assert 1 == list.size()
        View questionView = list[0]
        assert view.is(questionView)
        assert questionView.is(view)
        List<Annotation> questions = questionView.findByAtType(Types.QUESTION)
        assert 1 == questions.size()
        assert 'question.' == questions[0].features.text

        list = container.findViewsThatContain(Types.ANSWER)
        assert 1 == list.size()
        View answerView = list[0]
        assert view.is(answerView)
        assert answerView.is(view)
        List<Annotation> answers = answerView.findByAtType(Types.ANSWER)
        assert 2 == answers.size()
        assert 'Answer one.' == answers[0].features.text
        assert 'Answer two.' == answers[1].features.text

    }
}
