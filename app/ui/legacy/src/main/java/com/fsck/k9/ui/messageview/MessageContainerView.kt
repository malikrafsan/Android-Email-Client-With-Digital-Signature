package com.fsck.k9.ui.messageview

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebView.HitTestResult
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ShareCompat.IntentBuilder
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.fsck.k9.SAES2.SAES2

import com.fsck.k9.contact.ContactIntentHelper
import com.fsck.k9.custom_encrypt.ecc.EccMain
import com.fsck.k9.helper.ClipboardManager
import com.fsck.k9.helper.Utility
import com.fsck.k9.mail.Address
import com.fsck.k9.mailstore.AttachmentResolver
import com.fsck.k9.mailstore.AttachmentViewInfo
import com.fsck.k9.mailstore.MessageViewInfo
import com.fsck.k9.message.html.DisplayHtml
import com.fsck.k9.ui.R
import com.fsck.k9.view.MessageWebView
import com.fsck.k9.view.MessageWebView.OnPageFinishedListener
import com.fsck.k9.view.WebViewConfigProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class MessageContainerView(context: Context, attrs: AttributeSet?) :
    LinearLayout(context, attrs),
    OnCreateContextMenuListener,
    KoinComponent {

    private val displayHtml: DisplayHtml by inject(named("MessageView"))
    private val webViewConfigProvider: WebViewConfigProvider by inject()
    private val clipboardManager: ClipboardManager by inject()
    private val linkTextHandler: LinkTextHandler by inject()

    private lateinit var layoutInflater: LayoutInflater

    private lateinit var messageContentView: MessageWebView
    private lateinit var attachmentsContainer: ViewGroup
    private lateinit var unsignedTextContainer: View
    private lateinit var unsignedTextDivider: View
    private lateinit var unsignedText: TextView
    private lateinit var keyDekrip: EditText
    private lateinit var buttonDekrip: Button
    private lateinit var switchIsDekrip: androidx.appcompat.widget.SwitchCompat

    private lateinit var keySignature: EditText
    private lateinit var buttonValidateSignature: Button
    private lateinit var switchIsValidate: androidx.appcompat.widget.SwitchCompat

    private var isShowingPictures = false
    private var currentHtmlText: String? = null
    private var contentMessageHTML: String = ""
    private var templateMessageHTML: String = ""
    private var contentTanpaMark: String = ""
    private var mark: String = ""
    private val attachmentViewMap = mutableMapOf<AttachmentViewInfo, AttachmentView>()
    private val attachments = mutableMapOf<Uri, AttachmentViewInfo>()
    private var attachmentCallback: AttachmentViewCallback? = null
    private var currentAttachmentResolver: AttachmentResolver? = null

    @get:JvmName("hasHiddenExternalImages")
    var hasHiddenExternalImages = false
        private set

    public override fun onFinishInflate() {
        super.onFinishInflate()

        layoutInflater = LayoutInflater.from(context)

        messageContentView = findViewById<MessageWebView>(R.id.message_content).apply {
            if (!isInEditMode) {
                configure(webViewConfigProvider.createForMessageView())
            }

            setOnCreateContextMenuListener(this@MessageContainerView)
            visibility = VISIBLE
        }

        attachmentsContainer = findViewById(R.id.attachments_container)
        unsignedTextContainer = findViewById(R.id.message_unsigned_container)
        unsignedTextDivider = findViewById(R.id.message_unsigned_divider)
        unsignedText = findViewById(R.id.message_unsigned_text)

        keyDekrip = findViewById(R.id.key_dekrip);
        keyDekrip.visibility = View.INVISIBLE;
        buttonDekrip = findViewById(R.id.dekrip_email);
        buttonDekrip.visibility = View.INVISIBLE;

        switchIsDekrip = findViewById(R.id.switchdekrip);
        switchIsDekrip.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                keyDekrip.visibility = View.VISIBLE;
                buttonDekrip.visibility = View.VISIBLE;
            } else {
                keyDekrip.visibility = View.INVISIBLE;
                buttonDekrip.visibility = View.INVISIBLE;
            }
        }

        keySignature = findViewById(R.id.validate_signature);
        keySignature.visibility = View.INVISIBLE;
        buttonValidateSignature = findViewById(R.id.validate_email);
        buttonValidateSignature.visibility = View.INVISIBLE;

        switchIsValidate = findViewById(R.id.validate_receiver_signature);
        switchIsValidate.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                keySignature.visibility = View.VISIBLE;
                buttonValidateSignature.visibility = View.VISIBLE;
            } else {
                keySignature.visibility = View.INVISIBLE;
                buttonValidateSignature.visibility = View.INVISIBLE;
            }
        }

        buttonDekrip.setOnClickListener {
            try {
                var saes2 = SAES2("abcdefghij123456");
                ParserHTMLText()
                android.util.Log.v("Key apakah aman? ", contentMessageHTML);
                // contentMessageHTML = saes2.decrypt(contentMessageHTML!!);
                var temp = saes2.decrypt("QwfQAszOvG/xAAZNcCwbUA==");
                android.util.Log.v("Hasil dekrip aman? ", temp!!);
                android.util.Log.v("Hasil dekrip aman? ", contentMessageHTML!!);
                android.util.Log.v("Key apakah aman? ", templateMessageHTML!!);
                contentMessageHTML = temp;
                appendMessage();
                refreshDisplayedContent();
                android.util.Log.v("Key apakah aman? ", currentHtmlText!!);
            } catch (e: Exception) {
                android.util.Log.v("ERROR ", e.message!!);
                Toast.makeText(context, "Kunci dekrip anda salah!", Toast.LENGTH_LONG).show()
            }
        }

        buttonValidateSignature.setOnClickListener {
            try {
                if(contentMessageHTML == "") {
                    ParserHTMLText()
                }
                parserMark()
                var signature = keySignature.text
                android.util.Log.v("Signature apakah aman? ", signature.toString()!!);
                android.util.Log.v("Content apakah aman? ", contentTanpaMark.trim());
                android.util.Log.v("Mark apakah aman? ", mark!!);
                val valid = EccMain.INSTANCE.validate(signature.toString(), contentTanpaMark.trim(), mark)

                val str = "Valid? " + if (valid) "Yes" else "No"
                Toast.makeText(context, str, Toast.LENGTH_LONG).show()
                android.util.Log.v("Key apakah aman? ", currentHtmlText!!);
            } catch (e: Exception) {
                android.util.Log.v("ERROR ", e.message!!);
                Toast.makeText(context, "Signature salah! anda salah!", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun ParserHTMLText() {
        var text = currentHtmlText!!;
        var jarak = text.length;
        var template = ""
        var content = ""
        var isContent = false
        var menghitung = 6

        for (i in 0..jarak-1) {
            if(i >= jarak - 7) {
                template += text[i]
            } else {
                var temp = "" + text[i] + text[i+1] + text[i+2] + text[i+3] + text[i+4] + text[i+5]
                if(temp == "auto\">") {
                    print(temp)
                    isContent = true
                }
                if(temp == "</div>") {
                    isContent = false
                }
                if(isContent && menghitung == 0) {
                    content += text[i]
                } else if(isContent) {
                    menghitung -= 1
                    template += text[i]
                } else {
                    template += text[i]
                }
            }
        };
        contentMessageHTML = content;
        templateMessageHTML = template;
    }

    fun parserMark() {
        var content = contentMessageHTML
        var menghitung = 0
        var isMark = false
        var markfungsi = ""
        var contentTanpaMarkfungsi = ""
        android.util.Log.v("Fungsi mark apakah aman? ", contentMessageHTML);
        for (i in 0..content.length-1) {
            if(i >= content.length - 12) {

            } else {
                var temp = "" + content[i] + content[i+1] + content[i+2] + content[i+3] + content[i+4] + content[i+5]
                var temp1 = "" + content[i] + content[i+1] + content[i+2] + content[i+3]
                if(temp1 == "<br>") {
                    menghitung = 4
                }
                if(temp == "&lt;sp") {
                    menghitung = 12
                    isMark = true
                }
                if(temp == "&lt;/s") {
                    isMark = false
                    menghitung = 12
                }
                if(isMark && menghitung == 0) {
                    markfungsi += content[i]
                } else if(menghitung > 0) {
                    menghitung -= 1
                } else {
                    contentTanpaMarkfungsi += content[i]
                }
            }
        };
        android.util.Log.v("Fungsi mark apakah aman? ", markfungsi!!);
        android.util.Log.v("Fungsi content apakah aman? ", contentTanpaMarkfungsi!!);
        mark = markfungsi
        contentTanpaMark = contentTanpaMarkfungsi
    }

    fun appendMessage() {
        var template = templateMessageHTML
        var content = contentMessageHTML
        var isContent = false
        var menghitung = 6
        var tambahkanIsi = ""
        menghitung = 6
        for (i in 0..template.length-1) {
            if(i >= template.length - 7) {
                tambahkanIsi += template[i]
            } else {
                var temp = "" + template[i] + template[i+1] + template[i+2] + template[i+3] + template[i+4] + template[i+5]
                if(temp == "auto\">") {
                    isContent = true
                }
                if(isContent && menghitung == 0) {
                    for (j in 0..content.length-1) {
                        tambahkanIsi += content[j]
                    }
                    tambahkanIsi += template[i]
                    isContent = false
                } else if(isContent) {
                    menghitung -= 1
                    tambahkanIsi += template[i]
                } else {
                    tambahkanIsi += template[i]
                }
            }
        };
        currentHtmlText = tambahkanIsi;
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu)

        val webView = view as WebView
        val hitTestResult = webView.hitTestResult

        when (hitTestResult.type) {
            HitTestResult.SRC_ANCHOR_TYPE -> {
                createLinkMenu(menu, webView, linkUrl = hitTestResult.extra)
            }
            HitTestResult.IMAGE_TYPE, HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                createImageMenu(menu, imageUrl = hitTestResult.extra)
            }
            HitTestResult.PHONE_TYPE -> {
                createPhoneNumberMenu(menu, phoneNumber = hitTestResult.extra)
            }
            HitTestResult.EMAIL_TYPE -> {
                createEmailMenu(menu, email = hitTestResult.extra)
            }
        }
    }

    private fun createLinkMenu(
        menu: ContextMenu,
        webView: WebView,
        linkUrl: String?,
    ) {
        if (linkUrl == null) return

        val listener = MenuItem.OnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_ITEM_LINK_VIEW -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl))
                    startActivityIfAvailable(context, intent)
                }
                MENU_ITEM_LINK_SHARE -> {
                    IntentBuilder(context)
                        .setType("text/plain")
                        .setText(linkUrl)
                        .startChooser()
                }
                MENU_ITEM_LINK_COPY -> {
                    val label = context.getString(R.string.webview_contextmenu_link_clipboard_label)
                    clipboardManager.setText(label, linkUrl)
                }
                MENU_ITEM_LINK_TEXT_COPY -> {
                    val message = linkTextHandler.obtainMessage()
                    webView.requestFocusNodeHref(message)
                }
            }
            true
        }

        menu.setHeaderTitle(linkUrl)

        menu.add(
            Menu.NONE,
            MENU_ITEM_LINK_VIEW,
            0,
            context.getString(R.string.webview_contextmenu_link_view_action),
        ).setOnMenuItemClickListener(listener)

        menu.add(
            Menu.NONE,
            MENU_ITEM_LINK_SHARE,
            1,
            context.getString(R.string.webview_contextmenu_link_share_action),
        ).setOnMenuItemClickListener(listener)

        menu.add(
            Menu.NONE,
            MENU_ITEM_LINK_COPY,
            2,
            context.getString(R.string.webview_contextmenu_link_copy_action),
        ).setOnMenuItemClickListener(listener)

        menu.add(
            Menu.NONE,
            MENU_ITEM_LINK_TEXT_COPY,
            3,
            context.getString(R.string.webview_contextmenu_link_text_copy_action),
        ).setOnMenuItemClickListener(listener)
    }

    private fun createImageMenu(menu: ContextMenu, imageUrl: String?) {
        if (imageUrl == null) return

        val imageUri = Uri.parse(imageUrl)
        val attachmentViewInfo = getAttachmentViewInfoIfCidUri(imageUri)
        val inlineImage = attachmentViewInfo != null

        val listener = MenuItem.OnMenuItemClickListener { item ->
            val attachmentCallback = checkNotNull(attachmentCallback)

            when (item.itemId) {
                MENU_ITEM_IMAGE_VIEW -> {
                    if (inlineImage) {
                        attachmentCallback.onViewAttachment(attachmentViewInfo)
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW, imageUri)
                        startActivityIfAvailable(context, intent)
                    }
                }
                MENU_ITEM_IMAGE_SAVE -> {
                    if (inlineImage) {
                        attachmentCallback.onSaveAttachment(attachmentViewInfo)
                    } else {
                        downloadImage(imageUri)
                    }
                }
                MENU_ITEM_IMAGE_COPY -> {
                    val label = context.getString(R.string.webview_contextmenu_image_clipboard_label)
                    clipboardManager.setText(label, imageUri.toString())
                }
            }
            true
        }

        if (inlineImage) {
            menu.setHeaderTitle(R.string.webview_contextmenu_image_title)
        } else {
            menu.setHeaderTitle(imageUrl)
        }

        menu.add(
            Menu.NONE,
            MENU_ITEM_IMAGE_VIEW,
            0,
            context.getString(R.string.webview_contextmenu_image_view_action),
        ).setOnMenuItemClickListener(listener)

        if (inlineImage || imageUri.scheme?.lowercase() in supportedDownloadUriSchemes) {
            menu.add(
                Menu.NONE,
                MENU_ITEM_IMAGE_SAVE,
                1,
                if (inlineImage) {
                    context.getString(R.string.webview_contextmenu_image_save_action)
                } else {
                    context.getString(R.string.webview_contextmenu_image_download_action)
                },
            ).setOnMenuItemClickListener(listener)
        }

        if (!inlineImage) {
            menu.add(
                Menu.NONE,
                MENU_ITEM_IMAGE_COPY,
                2,
                context.getString(R.string.webview_contextmenu_image_copy_action),
            ).setOnMenuItemClickListener(listener)
        }
    }

    private fun createPhoneNumberMenu(menu: ContextMenu, phoneNumber: String?) {
        if (phoneNumber == null) return

        val listener = MenuItem.OnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_ITEM_PHONE_CALL -> {
                    val uri = Uri.parse(WebView.SCHEME_TEL + phoneNumber)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivityIfAvailable(context, intent)
                }
                MENU_ITEM_PHONE_SAVE -> {
                    val intent = ContactIntentHelper.getAddPhoneContactIntent(phoneNumber)
                    startActivityIfAvailable(context, intent)
                }
                MENU_ITEM_PHONE_COPY -> {
                    val label = context.getString(R.string.webview_contextmenu_phone_clipboard_label)
                    clipboardManager.setText(label, phoneNumber)
                }
            }
            true
        }

        menu.setHeaderTitle(phoneNumber)

        menu.add(
            Menu.NONE,
            MENU_ITEM_PHONE_CALL,
            0,
            context.getString(R.string.webview_contextmenu_phone_call_action),
        ).setOnMenuItemClickListener(listener)

        menu.add(
            Menu.NONE,
            MENU_ITEM_PHONE_SAVE,
            1,
            context.getString(R.string.webview_contextmenu_phone_save_action),
        ).setOnMenuItemClickListener(listener)

        menu.add(
            Menu.NONE,
            MENU_ITEM_PHONE_COPY,
            2,
            context.getString(R.string.webview_contextmenu_phone_copy_action),
        ).setOnMenuItemClickListener(listener)
    }

    private fun createEmailMenu(menu: ContextMenu, email: String?) {
        if (email == null) return

        val listener = MenuItem.OnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_ITEM_EMAIL_SEND -> {
                    val uri = Uri.parse(WebView.SCHEME_MAILTO + email)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivityIfAvailable(context, intent)
                }
                MENU_ITEM_EMAIL_SAVE -> {
                    val intent = ContactIntentHelper.getAddEmailContactIntent(Address(email))
                    startActivityIfAvailable(context, intent)
                }
                MENU_ITEM_EMAIL_COPY -> {
                    val label = context.getString(R.string.webview_contextmenu_email_clipboard_label)
                    clipboardManager.setText(label, email)
                }
            }
            true
        }

        menu.setHeaderTitle(email)

        menu.add(
            Menu.NONE,
            MENU_ITEM_EMAIL_SEND,
            0,
            context.getString(R.string.webview_contextmenu_email_send_action),
        ).setOnMenuItemClickListener(listener)

        menu.add(
            Menu.NONE,
            MENU_ITEM_EMAIL_SAVE,
            1,
            context.getString(R.string.webview_contextmenu_email_save_action),
        ).setOnMenuItemClickListener(listener)

        menu.add(
            Menu.NONE,
            MENU_ITEM_EMAIL_COPY,
            2,
            context.getString(R.string.webview_contextmenu_email_copy_action),
        ).setOnMenuItemClickListener(listener)
    }

    private fun downloadImage(uri: Uri) {
        val request = DownloadManager.Request(uri).apply {
            if (Build.VERSION.SDK_INT >= 29) {
                val filename = uri.lastPathSegment
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            }

            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private fun getAttachmentViewInfoIfCidUri(uri: Uri): AttachmentViewInfo? {
        if (uri.scheme != "cid") return null

        val attachmentResolver = checkNotNull(currentAttachmentResolver)

        val cid = uri.schemeSpecificPart
        val internalUri = attachmentResolver.getAttachmentUriForContentId(cid)

        return attachments[internalUri]
    }

    private fun startActivityIfAvailable(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_activity_not_found, Toast.LENGTH_LONG).show()
        }
    }

    private fun setLoadPictures(enable: Boolean) {
        messageContentView.blockNetworkData(!enable)
        isShowingPictures = enable
    }

    fun showPictures() {
        setLoadPictures(true)
        refreshDisplayedContent()
    }

    fun displayMessageViewContainer(
        messageViewInfo: MessageViewInfo,
        onRenderingFinishedListener: OnRenderingFinishedListener,
        loadPictures: Boolean,
        hideUnsignedTextDivider: Boolean,
        attachmentCallback: AttachmentViewCallback?,
    ) {
        this.attachmentCallback = attachmentCallback

        resetView()
        renderAttachments(messageViewInfo)

        val messageText = messageViewInfo.text
        if (messageText != null && !isShowingPictures) {
            if (Utility.hasExternalImages(messageText)) {
                if (loadPictures) {
                    setLoadPictures(true)
                } else {
                    hasHiddenExternalImages = true
                }
            }
        }

        val textToDisplay = messageText
            ?: displayHtml.wrapStatusMessage(context.getString(R.string.webview_empty_message))

        displayHtmlContentWithInlineAttachments(
            htmlText = textToDisplay,
            attachmentResolver = messageViewInfo.attachmentResolver,
            onPageFinishedListener = onRenderingFinishedListener::onLoadFinished,
        )

        if (!messageViewInfo.extraText.isNullOrEmpty()) {
            unsignedTextContainer.isVisible = true
            unsignedTextDivider.isGone = hideUnsignedTextDivider
            unsignedText.text = messageViewInfo.extraText
        }
    }

    private fun displayHtmlContentWithInlineAttachments(
        htmlText: String,
        attachmentResolver: AttachmentResolver,
        onPageFinishedListener: OnPageFinishedListener,
    ) {
        currentHtmlText = htmlText
        currentAttachmentResolver = attachmentResolver
        messageContentView.displayHtmlContentWithInlineAttachments(htmlText, attachmentResolver, onPageFinishedListener)
    }

    private fun refreshDisplayedContent() {
        val htmlText = checkNotNull(currentHtmlText)

        messageContentView.displayHtmlContentWithInlineAttachments(
            htmlText = htmlText,
            attachmentResolver = currentAttachmentResolver,
            onPageFinishedListener = null,
        )
    }

    private fun clearDisplayedContent() {
        messageContentView.displayHtmlContentWithInlineAttachments(
            htmlText = "",
            attachmentResolver = null,
            onPageFinishedListener = null,
        )

        unsignedTextContainer.isVisible = false
        unsignedText.text = ""
    }

    private fun renderAttachments(messageViewInfo: MessageViewInfo) {
        if (messageViewInfo.attachments != null) {
            for (attachment in messageViewInfo.attachments) {
                attachments[attachment.internalUri] = attachment
                if (attachment.inlineAttachment) {
                    continue
                }

                val attachmentView = layoutInflater.inflate(
                    R.layout.message_view_attachment,
                    attachmentsContainer,
                    false,
                ) as AttachmentView

                attachmentView.setCallback(attachmentCallback)
                attachmentView.setAttachment(attachment)

                attachmentViewMap[attachment] = attachmentView
                attachmentsContainer.addView(attachmentView)
            }
        }

        if (messageViewInfo.extraAttachments != null) {
            for (attachment in messageViewInfo.extraAttachments) {
                attachments[attachment.internalUri] = attachment
                if (attachment.inlineAttachment) {
                    continue
                }

                val lockedAttachmentView = layoutInflater.inflate(
                    R.layout.message_view_attachment_locked,
                    attachmentsContainer,
                    false,
                ) as LockedAttachmentView

                lockedAttachmentView.setCallback(attachmentCallback)
                lockedAttachmentView.setAttachment(attachment)

                attachmentsContainer.addView(lockedAttachmentView)
            }
        }
    }

    private fun resetView() {
        setLoadPictures(false)
        attachmentsContainer.removeAllViews()

        currentHtmlText = null
        currentAttachmentResolver = null

        /*
         * Clear the WebView content
         *
         * For some reason WebView.clearView() doesn't clear the contents when the WebView changes
         * its size because the button to download the complete message was previously shown and
         * is now hidden.
         */
        clearDisplayedContent()
    }

    fun refreshAttachmentThumbnail(attachment: AttachmentViewInfo) {
        getAttachmentView(attachment)?.refreshThumbnail()
    }

    private fun getAttachmentView(attachment: AttachmentViewInfo): AttachmentView? {
        return attachmentViewMap[attachment]
    }

    interface OnRenderingFinishedListener {
        fun onLoadFinished()
    }

    companion object {
        private const val MENU_ITEM_LINK_VIEW = Menu.FIRST
        private const val MENU_ITEM_LINK_SHARE = Menu.FIRST + 1
        private const val MENU_ITEM_LINK_COPY = Menu.FIRST + 2
        private const val MENU_ITEM_LINK_TEXT_COPY = Menu.FIRST + 3
        private const val MENU_ITEM_IMAGE_VIEW = Menu.FIRST
        private const val MENU_ITEM_IMAGE_SAVE = Menu.FIRST + 1
        private const val MENU_ITEM_IMAGE_COPY = Menu.FIRST + 2
        private const val MENU_ITEM_PHONE_CALL = Menu.FIRST
        private const val MENU_ITEM_PHONE_SAVE = Menu.FIRST + 1
        private const val MENU_ITEM_PHONE_COPY = Menu.FIRST + 2
        private const val MENU_ITEM_EMAIL_SEND = Menu.FIRST
        private const val MENU_ITEM_EMAIL_SAVE = Menu.FIRST + 1
        private const val MENU_ITEM_EMAIL_COPY = Menu.FIRST + 2

        // DownloadManager only supports http and https URIs
        private val supportedDownloadUriSchemes = setOf("http", "https")
    }
}
