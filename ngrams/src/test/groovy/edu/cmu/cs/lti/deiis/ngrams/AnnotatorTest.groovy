package edu.cmu.cs.lti.deiis.ngrams

import org.junit.Test
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.DataContainer
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

/**
 * @author Keith Suderman
 */
class AnnotatorTest {

    @Test
    void bigramTest() {

        InputStream stream = this.class.getResourceAsStream("/tokenized.json")
        assert null != stream
        String json = stream.text

        Data d = Serializer.parse(json)
        if (d.parameters == null) {
            d.parameters = [:]
        }
        d.parameters['type'] = 'BIGRAM'
        json = new Annotator().execute(d.asJson())
        d = Serializer.parse(json, DataContainer)
        Container container = d.payload
        assert 3 == container.views.size()
        View ngrams = container.findViewById("ngrams")
        assert null != ngrams
        assert 26 == ngrams.annotations.size()
    }

    @Test
    void unigramTest() {

        InputStream stream = this.class.getResourceAsStream("/tokenized.json")
        assert null != stream
        String json = stream.text

        Data d = Serializer.parse(json)
        if (d.parameters == null) {
            d.parameters = [:]
        }
        d.parameters['type'] = 'UNIGRAM'
        json = new Annotator().execute(d.asJson())
        d = Serializer.parse(json, DataContainer)
        Container container = d.payload
        assert 3 == container.views.size()
        View ngrams = container.findViewById("ngrams")
        assert null != ngrams
        assert 35 == ngrams.annotations.size()
    }

    @Test
    void trigramTest() {

        InputStream stream = this.class.getResourceAsStream("/tokenized.json")
        assert null != stream
        String json = stream.text

        Data d = Serializer.parse(json)
        if (d.parameters == null) {
            d.parameters = [:]
        }
        d.parameters['type'] = 'TRIGRAM'
        json = new Annotator().execute(d.asJson())
        d = Serializer.parse(json, DataContainer)
        Container container = d.payload
        assert 3 == container.views.size()
        View ngrams = container.findViewById("ngrams")
        assert null != ngrams
        assert 17 == ngrams.annotations.size()
    }

}
