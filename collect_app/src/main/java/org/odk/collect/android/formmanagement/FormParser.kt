package org.odk.collect.android.formmanagement

import org.kxml2.io.KXmlParser
import org.kxml2.kdom.Document
import org.kxml2.kdom.Element
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

object FormParser {
    @JvmStatic
    @Throws(FileNotFoundException::class, XmlPullParserException::class)
    fun parseXml(formFile: File): Document {
        return parseXml(formFile.inputStream())
    }

    @JvmStatic
    @Throws(XmlPullParserException::class)
    fun parseXml(formInputStream: InputStream): Document {
        val parser = KXmlParser().apply {
            setInput(formInputStream, "UTF-8")
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        }
        return Document().also { it.parse(parser) }
    }
}

fun Document.getHead(): Element {
    return getRootElement().getElement(null, "h:head")
}

fun Document.getTitle(): String {
    return getHead().getElement(null, "h:title").getChild(0).toString()
}

fun Document.getModel(): Element {
    return getHead().getElement(null, "model")
}
