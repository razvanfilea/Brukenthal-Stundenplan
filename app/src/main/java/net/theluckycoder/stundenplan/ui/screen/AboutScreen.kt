package net.theluckycoder.stundenplan.ui.screen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import cafe.adriel.voyager.core.screen.Screen
import net.theluckycoder.stundenplan.BuildConfig
import net.theluckycoder.stundenplan.R

@OptIn(ExperimentalMaterialApi::class)
class AboutScreen : Screen {

    private fun Context.openUrl(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun Context.openEmail(email: String, title: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, title)
        }

        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
    }

    @Composable
    override fun Content() {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppCard()
            AuthorCard()
        }
    }

    @Composable
    private fun Item(
        title: String, summary: String?, iconResId: Int, action: (() -> Unit)? = null
    ) {
        val mod = if (action != null) Modifier.clickable(
            role = Role.Button, onClick = action
        ) else Modifier
        ListItem(
            modifier = mod,
            text = { Text(title) },
            secondaryText = if (summary != null) {
                { Text(summary) }
            } else null,
            icon = {
                Icon(
                    painterResource(iconResId),
                    modifier = Modifier.fillMaxHeight(),
                    contentDescription = null
                )
            }
        )
    }

    @Composable
    private fun Item(titleId: Int, summary: String?, iconResId: Int, action: (() -> Unit)? = null) {
        Item(stringResource(titleId), summary, iconResId, action)
    }

    @Composable
    private fun ColumnCard(content: @Composable () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.fillMaxWidth()) {
                content()
            }
        }
    }

    @Composable
    private fun AppCard() = ColumnCard {
        val ctx = LocalContext.current

        ListItem(
            text = { Text(stringResource(R.string.app_name)) },
            icon = {
                ResourcesCompat.getDrawable(
                    LocalContext.current.resources, R.mipmap.ic_launcher, LocalContext.current.theme
                )?.let { drawable ->
                    val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888,
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)

                    Icon(
                        bitmap.asImageBitmap(), null, Modifier.size(48.dp), tint = Color.Unspecified
                    )
                }
            })

        Item(R.string.about_version, BuildConfig.VERSION_NAME, R.drawable.ic_about_version)
        Item(R.string.about_privacy_policy, PRIVACY_POLICY, R.drawable.ic_about_privacy_policy) {
            ctx.openUrl(PRIVACY_POLICY)
        }
        Item(R.string.about_app_feedback, APP_EMAIL, R.drawable.ic_about_email) {
            ctx.openEmail(APP_EMAIL, "BrukPlan App")
        }
    }

    @Composable
    private fun AuthorCard() = ColumnCard {
        val ctx = LocalContext.current

        Text(
            stringResource(R.string.about_developer),
            fontSize = 17.5.sp,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            fontWeight = FontWeight.SemiBold
        )

        Item("Filea Răzvan-Gheorghe", null, R.drawable.ic_about_author)

        Item(R.string.about_contact, DEVELOPER_EMAIL, R.drawable.ic_about_email) {
            ctx.openEmail(DEVELOPER_EMAIL, "BrukPlan App")
        }
        Item("GitHub", DEVELOPER_GITHUB, R.drawable.ic_about_github) {
            ctx.openUrl(DEVELOPER_GITHUB)
        }
    }

    companion object {
        private const val APP_EMAIL = "directoradjunct97@gmail.com"
        private const val PRIVACY_POLICY = "https://brukenthal.ro/wp-content/uploads/2022/09/stundenplan.html"

        private const val DEVELOPER_EMAIL = "razvan.filea@gmail.com"
        private const val DEVELOPER_GITHUB = "https://github.com/TheLuckyCoder"
    }
}