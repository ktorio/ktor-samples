package io.ktor.samples.structuredlogging

import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.pipeline.PipelineContext
import io.ktor.util.AttributeKey
import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers.appendEntries
import org.slf4j.Logger
import org.slf4j.Marker

val StructuredLoggerAttr = AttributeKey<StructuredLogger>("StructuredLogger")
val PipelineContext<Unit, ApplicationCall>.log get() = this.call.attributes.computeIfAbsent(StructuredLoggerAttr) { StructuredLogger(this.application.log) }

class StructuredLogger(val logger: Logger) : Logger {

    val attributes = LinkedHashMap<String, String>()

    inline fun attach(key: String, value: String, callback: () -> Unit) {
        attributes[key] = value
        try {
            callback()
        } finally {
            attributes.remove(key)
        }
    }

    override fun info(msg: String?, t: Throwable?) {
        logger.info(appendEntries(attributes), msg, t)
    }

    override fun info(marker: Marker?, msg: String?) {
        logger.info(appendEntries(attributes).and<LogstashMarker>(marker), msg)
    }

    override fun info(marker: Marker?, format: String?, arg: Any?) {
        logger.info(appendEntries(attributes).and<LogstashMarker>(marker), format, arg)
    }

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        logger.info(appendEntries(attributes).and<LogstashMarker>(marker), format, arg1, arg2)
    }

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        logger.info(appendEntries(attributes).and<LogstashMarker>(marker), format, arguments)
    }

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {
        logger.info(appendEntries(attributes).and<LogstashMarker>(marker), msg, t)
    }

    override fun warn(msg: String?) {
        logger.warn(appendEntries(attributes), msg)
    }

    override fun warn(format: String?, arg: Any?) {
        logger.warn(appendEntries(attributes), format, arg)
    }

    override fun warn(format: String?, vararg arguments: Any?) {
        logger.warn(appendEntries(attributes), format, arguments)
    }

    override fun warn(format: String?, arg1: Any?, arg2: Any?) {
        logger.warn(appendEntries(attributes), format, arg1, arg2)
    }

    override fun warn(msg: String?, t: Throwable?) {
        logger.warn(appendEntries(attributes), msg, t)
    }

    override fun warn(marker: Marker?, msg: String?) {
        logger.warn(appendEntries(attributes).and<LogstashMarker>(marker), msg)
    }

    override fun warn(marker: Marker?, format: String?, arg: Any?) {
        logger.warn(appendEntries(attributes).and<LogstashMarker>(marker), format, arg)
    }

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        logger.warn(appendEntries(attributes).and<LogstashMarker>(marker), format, arg1, arg2)
    }

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
        logger.warn(appendEntries(attributes).and<LogstashMarker>(marker), format, arguments)
    }

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {
        logger.warn(appendEntries(attributes).and<LogstashMarker>(marker), msg, t)
    }

    override fun getName(): String {
        return logger.name
    }

    override fun isErrorEnabled(): Boolean {
        return logger.isErrorEnabled
    }

    override fun isErrorEnabled(marker: Marker?): Boolean {
        return logger.isErrorEnabled(marker)
    }

    override fun error(msg: String?) {
        logger.error(appendEntries(attributes), msg)
    }

    override fun error(format: String?, arg: Any?) {
        logger.error(appendEntries(attributes), format, arg)
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        logger.error(appendEntries(attributes), format, arg1, arg2)
    }

    override fun error(format: String?, vararg arguments: Any?) {
        logger.error(appendEntries(attributes), format, arguments)
    }

    override fun error(msg: String?, t: Throwable?) {
        logger.error(appendEntries(attributes), msg, t)
    }

    override fun error(marker: Marker?, msg: String?) {
        logger.error(appendEntries(attributes), msg)
    }

    override fun error(marker: Marker?, format: String?, arg: Any?) {
        logger.error(appendEntries(attributes).and<LogstashMarker>(marker), format, arg)
    }

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        logger.error(appendEntries(attributes).and<LogstashMarker>(marker), format, arg1, arg2)
    }

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        logger.error(appendEntries(attributes).and<LogstashMarker>(marker), format, *arguments)
    }

    override fun error(marker: Marker?, msg: String?, t: Throwable?) {
        logger.error(appendEntries(attributes).and<LogstashMarker>(marker), msg, t)
    }

    override fun isDebugEnabled(): Boolean {
        return logger.isDebugEnabled
    }

    override fun isDebugEnabled(marker: Marker?): Boolean {
        return isDebugEnabled(marker)
    }

    override fun debug(msg: String?) {
        logger.debug(appendEntries(attributes), msg)
    }

    override fun debug(format: String?, arg: Any?) {
        logger.debug(appendEntries(attributes), format, arg)
    }

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {
        logger.debug(appendEntries(attributes), format, arg1, arg2)
    }

    override fun debug(format: String?, vararg arguments: Any?) {
        logger.debug(appendEntries(attributes), format, *arguments)
    }

    override fun debug(msg: String?, t: Throwable?) {
        logger.debug(appendEntries(attributes), msg, t)
    }

    override fun debug(marker: Marker?, msg: String?) {
        logger.debug(appendEntries(attributes).and<LogstashMarker>(marker), msg)
    }

    override fun debug(marker: Marker?, format: String?, arg: Any?) {
        logger.debug(appendEntries(attributes).and<LogstashMarker>(marker), format, arg)
    }

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        logger.debug(appendEntries(attributes).and<LogstashMarker>(marker), format, arg1, arg2)
    }

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        logger.debug(appendEntries(attributes).and<LogstashMarker>(marker), format, *arguments)
    }

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        logger.debug(appendEntries(attributes).and<LogstashMarker>(marker), msg, t)
    }

    override fun isInfoEnabled(): Boolean {
        return logger.isInfoEnabled
    }

    override fun isInfoEnabled(marker: Marker?): Boolean {
        return logger.isInfoEnabled(marker)
    }

    override fun trace(msg: String?) {
        logger.trace(appendEntries(attributes), msg)
    }

    override fun trace(format: String?, arg: Any?) {
        logger.trace(appendEntries(attributes), format, arg)
    }

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {
        logger.trace(appendEntries(attributes), format, arg1, arg2)
    }

    override fun trace(format: String?, vararg arguments: Any?) {
        logger.trace(appendEntries(attributes), format, *arguments)
    }

    override fun trace(msg: String?, t: Throwable?) {
        logger.trace(appendEntries(attributes), msg, t)
    }

    override fun trace(marker: Marker?, msg: String?) {
        logger.trace(appendEntries(attributes), msg)
    }

    override fun trace(marker: Marker?, format: String?, arg: Any?) {
        logger.trace(appendEntries(attributes), format, arg)
    }

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        logger.trace(appendEntries(attributes).and<LogstashMarker>(marker), format, arg1, arg2)
    }

    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) {
        logger.trace(appendEntries(attributes).and<LogstashMarker>(marker), format, *argArray)
    }

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        logger.trace(appendEntries(attributes).and<LogstashMarker>(marker), msg, t)
    }

    override fun isWarnEnabled(): Boolean {
        return isWarnEnabled
    }

    override fun isWarnEnabled(marker: Marker?): Boolean {
        return isWarnEnabled(marker)
    }

    override fun isTraceEnabled(): Boolean {
        return isTraceEnabled
    }

    override fun isTraceEnabled(marker: Marker?): Boolean {
        return isTraceEnabled(marker)
    }

    override fun info(format: String?, vararg arguments: Any?) {
        logger.info(appendEntries(attributes), format, arguments)
    }

    override fun info(format: String?, arg1: Any?, arg2: Any?) {
        logger.info(appendEntries(attributes), format, arg1, arg2)
    }

    override fun info(format: String?, arg: Any?) {
        logger.info(appendEntries(attributes), format, arg)
    }

    override fun info(text: String) {
        logger.info(appendEntries(attributes), text)
    }
}