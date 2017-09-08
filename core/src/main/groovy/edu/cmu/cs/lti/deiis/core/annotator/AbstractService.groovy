package edu.cmu.cs.lti.deiis.core.annotator

import org.lappsgrid.api.WebService
import org.lappsgrid.serialization.Data
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.lappsgrid.discriminator.Discriminators.*
import org.lappsgrid.metadata.ServiceMetadataBuilder


/**
 * The base class for services in the example pipeline with a few helper
 * methods common to all the services.  In particular much of the metadata
 * returned by services is the same.
 *
 */
abstract class AbstractService implements WebService {

    /**
     * Message returned when a service receives a data type it doesn't
     * understand.
     */
    static final String UNSUPPORTED = "Unsupported discriminator type: "

    /**
     * The JSON for the metadata is lazily initialized the first time
     * getMetadata is called.
     */
    private String metadata

    /**
     * Set by sub-classes and used when generating error messages.
     */
    private String name

    private static Logger statsLogger = LoggerFactory.getLogger("edu.cmu.cs.lti.deiss.stats")

    public AbstractService(Class child) {
        name = child.name
    }

    /**
     * The configure method is called to allow sub-classes to tailor the
     * metadata returned by the getMetadata() method.
     *
     * @param builder The ServiceMetadataBuilder to configure
     * @return the same builder that was passed as a parameter
     */
    protected abstract ServiceMetadataBuilder configure(ServiceMetadataBuilder builder)

    /**
     * The base class can generate most of the metadata for the service and
     * the configure method will be called so sub-classes have an opportunity
     * to contribute metadata as well.
     *
     * @return JSON representation of the metadata for this service
     */
    String getMetadata() {
        if (metadata == null) {
            ServiceMetadataBuilder builder = new ServiceMetadataBuilder()
                    .version("1.0.0")
                    .license(Uri.APACHE2)
                    .allow(Uri.ANY)
                    .vendor("https://www.lti.cs.cmu.edu")
                    .requireFormat(Uri.LIF)
                    .produceFormat(Uri.LIF)
            configure(builder)
            Data data = new Data(Uri.META, builder.build())
            metadata = data.asPrettyJson()
        }
        return metadata
    }

    protected long timestamp() { return System.currentTimeMillis() }
    protected void logStats(String message) {
        statsLogger.info(message)
    }

    protected void logStats(String message, Object... args) {
        statsLogger.info(message, args)
    }

    /**
     * Generates the JSON serialization of a Data object with the
     * discriminator set to ERROR and the payload set to the
     * UNSUPPORTED message above.
     *
     * @param type the discriminator that is not supported
     * @return the JSON representation of a Data object.
     */
    protected String unsupported(String type) {
        return error(UNSUPPORTED + type)
    }

    /**
     * Generates the JSON serialization of a Data object with the
     * discriminator set to ERROR and the payload set to the
     * supplied message.
     *
     * @param message
     * @return
     */
    protected String error(String message) {
        return new Data(Uri.ERROR, name + ': ' + message).asPrettyJson()
    }
}
