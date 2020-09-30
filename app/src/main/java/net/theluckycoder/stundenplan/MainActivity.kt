package net.theluckycoder.stundenplan

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.snackbar.Snackbar
import net.theluckycoder.stundenplan.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding: MainActivityBinding
    private val preferences by lazy { Preferences(applicationContext) }

    private var isToolbarVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.viewer.useBestQuality(true)
        binding.viewer.maxZoom = 6f

        binding.viewer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root, Slide(Gravity.TOP))

            if (isToolbarVisible) {
                supportActionBar?.hide()
            } else {
                supportActionBar?.show()
            }

            isToolbarVisible = !isToolbarVisible
        }

        viewModel.getStateLiveData().observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    binding.progressBar.hide()
                    configurePdfViewer(result.fileUri)
                }
                is Result.Loading -> {
                    with(binding.progressBar) {
                        show()
                        isIndeterminate = result.indeterminate
                        progress = result.progress
                    }
                }
                is Result.Failed -> {
                    binding.progressBar.hide()
                    Snackbar.make(binding.root, R.string.download_failed, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        // Cancel Active Notifications
        NotificationManagerCompat.from(this).cancel(FirebaseNotificationService.NOTIFICATION_ID)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_switch_theme -> {
                val darkTheme = !preferences.useDarkTheme
                preferences.useDarkTheme = darkTheme

                binding.viewer.setNightMode(darkTheme)
                binding.viewer.loadPages()
            }
            R.id.action_refresh -> viewModel.reload()
            R.id.action_switch_timetable_type -> viewModel.switchTimetableType()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun configurePdfViewer(uri: Uri) {
        binding.viewer.fromUri(uri)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onError { Snackbar.make(binding.root, R.string.error_displaying, Snackbar.LENGTH_LONG).show() }
            .enableAntialiasing(true)
            .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
            .nightMode(preferences.useDarkTheme)
            .load()
    }
}
