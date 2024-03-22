import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.jetbrains.skia.Bitmap

val client = HttpClient()

@Composable
fun UrlImage(url: String) {
    var painter by remember { mutableStateOf<BitmapPainter?>(null) }

    LaunchedEffect(Unit) {
        val response: ByteArray = client.get(url)
            .body()

        val image = org.jetbrains.skia.Image.makeFromEncoded(response)
        val bitmap = Bitmap.Companion
            .makeFromImage(image)
            .asComposeImageBitmap()
        painter = BitmapPainter(bitmap)
    }

    val imagePainter = painter ?: return
    Image(
        painter = imagePainter,
        contentDescription = url
    )
}

@Composable
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                UrlImage("/kodee.png")
            }
        }
    }
}