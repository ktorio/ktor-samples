package io.ktor.samples.structuredlogging

import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.AttributeKey
import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers.appendEntries
import org.slf4j.Logger
import org.slf4j.Marker

private val StructuredLoggerAttr = AttributeKey<StructuredLogger>("StructuredLogger")

/**
 * Obtains a logger for this [ApplicationCall] that allows to temporarily [StructuredLogger.attach] objects
 * to be associated to each log.
 */
val PipelineContext<Unit, ApplicationCall>.logger get() = this.call.attributes.computeIfAbsent(StructuredLoggerAttr) { StructuredLogger(this.application.log) }

/**
 * Non-static [StructuredLogger] that allows to temporarily attach named context objects to it, so calls to logging methods
 * delegate their logging to the [logger] but with attaching the named context objects available when the call is performed.
 */
class StructuredLogger(val logger: Logger) : Logger {

    @PublishedApi
    internal val attributes = LinkedHashMap<String, String>()

    /**
     * Attaches a [key] named context object [value] temporally while the [callback] is executed.
     * This function is inline so can be used in suspend functions respecting the.
     */
    inline fun attach(key: String, value: String, callback: () -> Unit) {
        val oldValue = attributes[key]
        attributes[key] = value
        try {
            callback()
        } finally {
            attributes.putOrRemove(key, oldValue)
        }
    }

    // Logger methods, delegating to the specified [logger] and attaching all the attributes available.
    override fun info(msg: String?, t: Throwable?) = run { logger.info(appendEntries(attributes), msg, t) }
    override fun info(marker: Marker?, msg: String?) = run { logger.info(appendEntries(attributes).and<LogstashMarker>(marker), msg) }
    override fun info(marker: Marker?, format: String?, arg: Any?) = run { logger.info(appendEntries(attributes).and<LogstashMarker>(marker), format, arg) }
    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) = run { logger.info(appendEntries(attributes).and<LogstashMarker>(marker), format, arg1, arg2) }
    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) = run { logger.info(appendEntries(attributes).and<LogstashMarker>(marker), format, arguments) }
    override fun info(marker: Marker?, msg: String?, t: Throwable?) = run { logger.info(appendEntries(attributes).and<LogstashMarker>(marker), msg, t) }
    override fun warn(msg: String?) = run { logger.warn(appendEntries(attributes), msg) }
    override fun warn(format: String?, arg: Any?) = run { logger.warn(appendEntries(attributes), format, arg) }
    override fun warn(format: String?, vararg arguments: Any?) = run { logger.warn(appendEntries(attributes), format, arguments) }
    override fun warn(format: String?, arg1: Any?, arg2: Any?) = run { logger.warn(appendEntries(attributes), format, arg1, arg2) }
    override fun warn(msg: String?, t: Throwable?) = run { logger.warn(appendEntries(attributes), msg, t) }
    override fun warn(marker: Marker?, msg: String?) = run { logger.warn(appendEntries(attributes).and<LogstashMarker>(marker), msg) }
    override fun warn(marker: Marker?, format: String?, arg: Any?) = run { logger.warn(appendEntries(attributes).and<LogstashMarker>(marker), format, arg) }
    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) = run { logger.warn(appendEntries(attributes).and<LogstashMarker>(marker), format, arg1, arg2) }
    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) = run { logger.warn(appendEntries(attributes).and<LogstashMarker>(marker), format, arguments) }
    override fun warn(marker: Marker?, msg: String?, t: Throwable?) = run { logger.warn(appendEntries(attributes).and<LogstashMarker>(marker), msg, t) }
    override fun getName(): String = logger.name
    override fun isErrorEnabled(): Boolean = logger.isErrorEnabled
    override fun isErrorEnabled(marker: Marker?): Boolean = logger.isErrorEnabled(marker)
    override fun error(msg: String?) = run { logger.error(appendEntries(attributes), msg) }
    override fun error(format: String?, arg: Any?) = run { logger.error(appendEntries(attributes), format, arg) }
    override fun error(format: String?, arg1: Any?, arg2: Any?) = run { logger.error(appendEntries(attributes), format, arg1, arg2) }
    override fun error(format: String?, vararg arguments: Any?) = run { logger.error(appendEntries(attributes), format, arguments) }
    override fun error(msg: String?, t: Throwable?) = run { logger.error(appendEntries(attributes), msg, t) }
    override fun error(marker: Marker?, msg: String?) = run { logger.error(appendEntries(attributes), msg) }
    override fun error(marker: Marker?, format: String?, arg: Any?) = run { logger.error(appendEntries(attributes).and<LogstashMarker>(marker), format, arg) }
    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) = run { logger.error(appendEntries(attributes).and<LogstashMarker>(marker), format, arg1, arg2) }
    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) = run { logger.error(appendEntries(attributes).and<LogstashMarker>(marker), format, *arguments) }
    override fun error(marker: Marker?, msg: String?, t: Throwable?) = run { logger.error(appendEntries(attributes).and<LogstashMarker>(marker), msg, t) }
    override fun isDebugEnabled(): Boolean = logger.isDebugEnabled
    override fun isDebugEnabled(marker: Marker?): Boolean = logger.isDebugEnabled(marker)
    override fun debug(msg: String?) = run { logger.debug(appendEntries(attributes), msg) }
    override fun debug(format: String?, arg: Any?) = run { logger.debug(appendEntries(attributes), format, arg) }
    override fun debug(format: String?, arg1: Any?, arg2: Any?) = run { logger.debug(appendEntries(attributes), format, arg1, arg2) }
    override fun debug(format: String?, vararg arguments: Any?) = run { logger.debug(appendEntries(attributes), format, *arguments) }
    override fun debug(msg: String?, t: Throwable?) = run { logger.debug(appendEntries(attributes), msg, t) }
    override fun debug(marker: Marker?, msg: String?) = run { logger.debug(appendEntries(attributes).and<LogstashMarker>(marker), msg) }
    override fun debug(marker: Marker?, format: String?, arg: Any?) = run { logger.debug(appendEntries(attributes).and<LogstashMarker>(marker), format, arg) }
    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) = run { logger.debug(appendEntries(attributes).and<LogstashMarker>(marker), format, arg1, arg2) }
    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) = run { logger.debug(appendEntries(attributes).and<LogstashMarker>(marker), format, *arguments) }
    override fun debug(marker: Marker?, msg: String?, t: Throwable?) = run { logger.debug(appendEntries(attributes).and<LogstashMarker>(marker), msg, t) }
    override fun isInfoEnabled(): Boolean = logger.isInfoEnabled
    override fun isInfoEnabled(marker: Marker?): Boolean = logger.isInfoEnabled(marker)
    override fun trace(msg: String?) = run { logger.trace(appendEntries(attributes), msg) }
    override fun trace(format: String?, arg: Any?) = run { logger.trace(appendEntries(attributes), format, arg) }
    override fun trace(format: String?, arg1: Any?, arg2: Any?) = run { logger.trace(appendEntries(attributes), format, arg1, arg2) }
    override fun trace(format: String?, vararg arguments: Any?) = run { logger.trace(appendEntries(attributes), format, *arguments) }
    override fun trace(msg: String?, t: Throwable?) = run { logger.trace(appendEntries(attributes), msg, t) }
    override fun trace(marker: Marker?, msg: String?) = run { logger.trace(appendEntries(attributes), msg) }
    override fun trace(marker: Marker?, format: String?, arg: Any?) = run { logger.trace(appendEntries(attributes), format, arg) }
    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) = run { logger.trace(appendEntries(attributes).and<LogstashMarker>(marker), format, arg1, arg2) }
    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) = run { logger.trace(appendEntries(attributes).and<LogstashMarker>(marker), format, *argArray) }
    override fun trace(marker: Marker?, msg: String?, t: Throwable?) = run { logger.trace(appendEntries(attributes).and<LogstashMarker>(marker), msg, t) }
    override fun isWarnEnabled(): Boolean = logger.isWarnEnabled
    override fun isWarnEnabled(marker: Marker?): Boolean = logger.isWarnEnabled(marker)
    override fun isTraceEnabled(): Boolean = logger.isTraceEnabled
    override fun isTraceEnabled(marker: Marker?): Boolean = logger.isTraceEnabled(marker)
    override fun info(format: String?, vararg arguments: Any?) = run { logger.info(appendEntries(attributes), format, arguments) }
    override fun info(format: String?, arg1: Any?, arg2: Any?) = run { logger.info(appendEntries(attributes), format, arg1, arg2) }
    override fun info(format: String?, arg: Any?) = run { logger.info(appendEntries(attributes), format, arg) }
    override fun info(text: String) = run { logger.info(appendEntries(attributes), text) }

}

/**
 * Utility function to either [MutableMap.remove] or [MutableMap.put] a [key]/[value] from a [MutableMap]
 * depending on the nullability of the value.
 */
@PublishedApi
internal fun <K, V> MutableMap<K, V>.putOrRemove(key: K, value: V?) {
    if (value == null) {
        this.remove(key)
    } else {
        this[key] = value
    }
}
