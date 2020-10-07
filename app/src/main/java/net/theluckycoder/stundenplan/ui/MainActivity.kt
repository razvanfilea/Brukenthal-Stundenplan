package net.theluckycoder.stundenplan.ui

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.snackbar.Snackbar
import net.theluckycoder.stundenplan.viewmodel.MainViewModel
import net.theluckycoder.stundenplan.R
import net.theluckycoder.stundenplan.TimetableType
import net.theluckycoder.stundenplan.utils.NetworkResult
import net.theluckycoder.stundenplan.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainActivityBinding

    private var isToolbarVisible = true
    private var timetableType: TimetableType? = null
    private var useDarkTheme = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.viewer.maxZoom = 6f
        binding.viewer.setNightMode(useDarkTheme)
        binding.viewer.setOnClickListener {
            supportActionBar?.let {
                TransitionManager.beginDelayedTransition(binding.root, Slide(Gravity.TOP))

                if (isToolbarVisible) it.hide() else it.show()

                isToolbarVisible = !isToolbarVisible
            }
        }

        viewModel.getStateLiveData().observe(this) { result ->
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
                    makeErrorSnackbar(result.reasonStringRes)
                        .setAction(R.string.action_retry) {
                            viewModel.reload(timetableType!!)
                        }
                        .show()
                }
            }
        }

        viewModel.darkThemeData.observe(this, { darkTheme ->
            if (useDarkTheme != darkTheme) {
                useDarkTheme = darkTheme

                with(binding.viewer) {
                    setNightMode(darkTheme)
                    loadPages()
                }
            }
        })

        viewModel.timetableTypeData.observe(this, {
            // Load last file first, then attempt to download a new one
            // Since it's very likely that the last downloaded PDF is also the most recent one
            if (savedInstanceState == null)
                viewModel.preload(it)

            if (timetableType != it) {
                timetableType = it
                invalidateOptionsMenu()

                viewModel.reload(it)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_switch_to_high_school)
            .isVisible = timetableType != TimetableType.HIGH_SCHOOL
        menu.findItem(R.id.action_switch_to_middle_school)
            .isVisible = timetableType != TimetableType.MIDDLE_SCHOOL

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_switch_theme -> viewModel.switchTheme(!useDarkTheme)
            R.id.action_refresh -> viewModel.reload(timetableType!!)
            R.id.action_switch_to_high_school -> viewModel.switchTimetableType(TimetableType.HIGH_SCHOOL)
            R.id.action_switch_to_middle_school -> viewModel.switchTimetableType(TimetableType.MIDDLE_SCHOOL)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun makeErrorSnackbar(stringRes: Int) =
        Snackbar.make(binding.root, stringRes, Snackbar.LENGTH_LONG)
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .setActionTextColor(ContextCompat.getColor(this, R.color.white))
            .setBackgroundTint(ContextCompat.getColor(this, R.color.red_800))

    private fun displayPdf(result: NetworkResult.Success) {
        binding.viewer.fromUri(result.fileUri)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onError {
                makeErrorSnackbar(R.string.error_rendering_failed).show()
            }
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
}
