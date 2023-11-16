package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("StaticFieldLeak")
object ODKPrinter {

    private var mWebView: WebView? = null

    @JvmStatic
    fun doWebViewPrint(context: Context) {
        // Create a WebView object specifically for printing
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false

            override fun onPageFinished(view: WebView, url: String) {
                createWebPrintJob(context, view)
                mWebView = null
            }
        }

        // Generate an HTML document on the fly:
        val htmlDocument =

                        "      <img id='qr_code'" +
                        "            src=\"file:///android_asset/qr_code.png\"/>" +
                        "      <br>" +
                        "      <img id='barcode'" +
                        "            src=\"file:///android_asset/barcode.png\"/>" +
                        "      <br>" +
                        "      <h1>ODK is awesome</h1>"

        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView
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
