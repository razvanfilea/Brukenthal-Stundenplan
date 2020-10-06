package net.theluckycoder.stundenplan.ui

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.viewer.maxZoom = 6f
        binding.viewer.setNightMode(true)
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
                    configurePdfViewer(result)
                }
                is NetworkResult.Loading -> {
                    with(binding.progressBar) {
                        isIndeterminate = result.indeterminate
                        progress = result.progress
                        showProgressBar()
                    }
                }
                is NetworkResult.Failed -> {
                    hideProgressBar()
                    Snackbar.make(binding.root, result.stringRes, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_switch_theme -> {
                with(binding.viewer) {
                    setNightMode(viewModel.switchTheme())
                    loadPages()
                }
            }
            R.id.action_refresh -> viewModel.reload()
            R.id.action_switch_to_high_school -> viewModel.switchTimetableType(TimetableType.HIGH_SCHOOL)
            R.id.action_switch_to_middle_school -> viewModel.switchTimetableType(TimetableType.MIDDLE_SCHOOL)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun configurePdfViewer(result: NetworkResult.Success) {
        binding.viewer.fromUri(result.fileUri)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onError { Snackbar.make(binding.root, R.string.error_rendering_failed, Snackbar.LENGTH_LONG).show() }
            .enableAntialiasing(true)
            .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
            .nightMode(result.darkTheme)
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
