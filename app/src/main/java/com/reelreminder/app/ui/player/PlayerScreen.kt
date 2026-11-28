package com.reelreminder.app.ui.player

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.reelreminder.app.data.Reel
import com.reelreminder.app.ui.theme.LightB1
import com.reelreminder.app.ui.theme.LightInk
import com.reelreminder.app.ui.theme.LightMute
import com.reelreminder.app.ui.theme.TealDark
import com.reelreminder.app.util.ShortcodeParser

private val ALLOWED_EMBED_HOSTS = setOf(
    "instagram.com",
    "www.instagram.com",
    "static.cdninstagram.com",
    "scontent.cdninstagram.com"
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(
    reel: Reel?,
    onBack: () -> Unit,
    onOpenInInstagram: () -> Unit,
    onOpenInBrowser: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parsed = remember(reel?.url) { ShortcodeParser.parse(reel?.url) }
    val shortcode = parsed?.shortcode
    val embedUrl = shortcode?.let { "https://www.instagram.com/reel/$it/embed/" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightB1)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LightInk
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (shortcode != null) "Reel ID: $shortcode" else "Reel ID: Not saved",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = LightInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!reel?.note.isNullOrBlank()) {
                    Text(
                        text = reel?.note.orEmpty(),
                        fontSize = 13.sp,
                        color = LightMute,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenInInstagram,
                enabled = shortcode != null,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealDark)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Open in Instagram", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onOpenInBrowser,
                enabled = shortcode != null,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text("Browser", fontWeight = FontWeight.SemiBold, color = LightInk)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            if (embedUrl != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url ?: return true
                                    return !isAllowedEmbedUri(url)
                                }
                            }
                            loadUrl(embedUrl)
                        }
                    },
                    update = { webView ->
                        if (webView.url != embedUrl) {
                            webView.loadUrl(embedUrl)
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Not saved: this reel has no link",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LightMute
                    )
                }
            }
        }
    }
}

private fun isAllowedEmbedUri(uri: Uri): Boolean {
    if (uri.scheme?.lowercase() != "https") return false
    val host = uri.host?.lowercase() ?: return false
    return host in ALLOWED_EMBED_HOSTS || host.endsWith(".cdninstagram.com") || host.endsWith(".fbcdn.net")
}
