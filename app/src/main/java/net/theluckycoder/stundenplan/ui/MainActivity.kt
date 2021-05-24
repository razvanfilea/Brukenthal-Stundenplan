package net.theluckycoder.stundenplan.ui

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import net.theluckycoder.stundenplan.BuildConfig
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.databinding.MainActivityBinding
import net.theluckycoder.stundenplan.extensions.browseUrl
import net.theluckycoder.stundenplan.model.TimetableType
import net.theluckycoder.stundenplan.utils.Analytics
import net.theluckycoder.stundenplan.utils.NetworkResult
import net.theluckycoder.stundenplan.utils.UpdateChecker
import net.theluckycoder.stundenplan.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainActivityBinding

    private var isToolbarVisible = true
    private var useDarkTheme = true

    init {
        lifecycleScope.launchWhenStarted {
            viewModel.networkState.collect { result ->
                ensureActive()

                when (result) {
                    is NetworkResult.Success -> {
                        hideProgressBar()
                        displayPdf(result)
                    }
                    is NetworkResult.Loading -> {
                        with(binding.progressBar) {
                            isIndeterminate = result.indeterminate
                            progress = result.progress
                        }
                        showProgressBar()
                    }
                    is NetworkResult.Failed -> {
                        hideProgressBar()

                        val reasonStringRes = when (result.reason) {
                            NetworkResult.FailReason.MissingNetworkConnection -> R.string.error_network_connection
                            NetworkResult.FailReason.DownloadFailed -> R.string.error_download_failed
                        }

                        makeErrorSnackbar(reasonStringRes)
                            .setAction(R.string.action_retry) { viewModel.refresh(force = true) }
                            .show()
                    }
                }
            }
        }

        // Ensure that the proper timetable is selected in the BottomNavigationView
        lifecycleScope.launchWhenStarted {
            binding.bottomBar.selectedItemId = when (viewModel.timetableType()) {
                TimetableType.HIGH_SCHOOL -> R.id.nav_high_school
                TimetableType.MIDDLE_SCHOOL -> R.id.nav_middle_school
            }
        }

        lifecycleScope.launchWhenResumed {
            if (!viewModel.hasSeenUpdateDialog) {
                showUpdateDialog()
                viewModel.hasSeenUpdateDialog = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.pdfView.maxZoom = 6f
        binding.pdfView.setOnClickListener {
            isToolbarVisible = !isToolbarVisible

            supportActionBar?.let {
                TransitionManager.beginDelayedTransition(binding.toolbar, Slide(Gravity.TOP))

                if (isToolbarVisible) it.show() else it.hide()
            }

            TransitionManager.beginDelayedTransition(binding.bottomBar, Slide(Gravity.BOTTOM))
            binding.bottomBar.isVisible = isToolbarVisible
        }

        viewModel.darkTheme.observe(this, { darkTheme ->
            useDarkTheme = darkTheme

            with(binding.pdfView) {
                setNightMode(darkTheme)
                loadPages()
            }
        })

        binding.bottomBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_high_school -> viewModel.switchTimetableType(TimetableType.HIGH_SCHOOL)
                R.id.nav_middle_school -> viewModel.switchTimetableType(TimetableType.MIDDLE_SCHOOL)
            }
            true
        }

        if (intent.getBooleanExtra(ARG_OPENED_FROM_NOTIFICATION, false))
            Analytics.openNotificationEvent()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_switch_theme -> viewModel.switchTheme(!useDarkTheme)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun makeErrorSnackbar(stringRes: Int) =
        Snackbar.make(binding.root, stringRes, Snackbar.LENGTH_LONG)
            .setAnchorView(binding.bottomBar)
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .setActionTextColor(ContextCompat.getColor(this, R.color.white))
            .setBackgroundTint(ContextCompat.getColor(this, R.color.red_800))

    private fun displayPdf(result: NetworkResult.Success) {
        binding.pdfView.fromUri(result.fileUri)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .enableAntialiasing(true)
            .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
            .nightMode(useDarkTheme)
            .load()
    }

    private fun showProgressBar() {
        with(binding) {
            TransitionManager.beginDelayedTransition(root)
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        with(binding) {
            TransitionManager.beginDelayedTransition(root)
            progressBar.visibility = View.GONE
        }
    }

    private fun showUpdateDialog() {
        UpdateChecker {
            AlertDialog.Builder(this)
                .setTitle(R.string.update_available)
                .setMessage(R.string.update_available_desc)
                .setPositiveButton(R.string.action_update) { _, _ ->
                    browseUrl(APP_STORE_URL)
                }
                .setNegativeButton(R.string.action_ignore, null)
                .show()
        }
    }

    companion object {
        const val ARG_OPENED_FROM_NOTIFICATION = "opened_from_notification"
        private const val APP_STORE_URL =
            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
    }
}
