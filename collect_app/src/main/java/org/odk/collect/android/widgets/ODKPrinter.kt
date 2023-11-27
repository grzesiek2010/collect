package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.odk.collect.qrcode.QRCodeCreator
import java.io.ByteArrayOutputStream

@SuppressLint("StaticFieldLeak")
object ODKPrinter {

    private var mWebView: WebView? = null

    @JvmStatic
    fun doWebViewPrint(context: Context, htmlDocument: String) {
        val doc: Document = Jsoup.parse(htmlDocument)

        val qrcodeElements = doc.getElementsByTag("qrcode")
        for (qrcodeElement in qrcodeElements) {
            val newElement = doc.createElement("img").apply {
                attributes().addAll(qrcodeElement.attributes())
                val bitmap = bitmapToBase64(QRCodeCreator().generate(qrcodeElement.text()))
                attr("src", "data:image/png;base64,$bitmap")
            }
            qrcodeElement.replaceWith(newElement)
        }

        val parsedHtml = doc.html()

        // Create a WebView object specifically for printing
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false

            override fun onPageFinished(view: WebView, url: String) {
                createWebPrintJob(context, view)
                mWebView = null
            }
        }

        webView.loadDataWithBaseURL(null, parsedHtml, "text/HTML", "UTF-8", null)

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun createWebPrintJob(context: Context, webView: WebView) {
        // Get a PrintManager instance
        (context.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->

            val jobName = "Test document"

            // Get a print adapter instance
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            // Create a print job with name and adapter instance
            printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder().build()
            )
        }
    }
}
