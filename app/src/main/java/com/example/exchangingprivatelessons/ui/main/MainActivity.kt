package com.example.exchangingprivatelessons.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.ActivityMainBinding
import com.example.exchangingprivatelessons.domain.repository.TakenLessonRepository
import com.example.exchangingprivatelessons.ui.lesson.AddEditLessonFragmentArgs
import com.example.exchangingprivatelessons.ui.lesson.LessonListFragmentArgs
import com.example.exchangingprivatelessons.ui.request.RequestsFragmentArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var takenRepo: TakenLessonRepository

    /** MainViewModel – מפעיל את הסנכרון החי הגלובלי */
    private val mainVm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* -------- Debug: observe taken lessons -------- */
        takenRepo.observeTakenLessons()
            .onEach { Log.d("TakenTest", it.toString()) }
            .launchIn(lifecycleScope)

        /* -------- Auth listener -------- */
        auth.addAuthStateListener { a ->
            if (a.currentUser == null) {
                val nav = findNavController(R.id.nav_host_fragment)
                if (nav.currentDestination?.id != R.id.authFragment) {
                    nav.navigate(
                        R.id.authFragment,
                        null,
                        navOptions {
                            popUpTo(nav.graph.startDestinationId) { inclusive = true }
                        }
                    )
                }
            }
        }

        /* -------- Quick Firestore sanity check -------- */
        auth.currentUser?.let { user ->
            Firebase.firestore
                .collection("users")
                .document(user.uid)
                .collection("takenLessons")
                .limit(1)
                .get()
                .addOnSuccessListener { Log.d("FS", "read OK") }
                .addOnFailureListener { e -> Log.e("FS", "read failed", e) }
        }


        /* -------- Toolbar -------- */
        setSupportActionBar(binding.toolbar)

        /* -------- NavController -------- */
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        appBarConfig = AppBarConfiguration.Builder(
            R.id.homeFragment,
            R.id.authFragment
        ).build()

        setupActionBarWithNavController(navController, appBarConfig)

        /* -------- Logout button visibility -------- */
        navController.addOnDestinationChangedListener { _, dest, _ ->
            invalidateOptionsMenu()
            binding.toolbar.menu.findItem(R.id.action_logout)?.isVisible =
                dest.id == R.id.homeFragment
        }

        /* -------- Dynamic toolbar titles -------- */
        navController.addOnDestinationChangedListener { _, dest, args ->
            val title = when (dest.id) {
                R.id.lessonListFragment -> {
                    when (LessonListFragmentArgs.fromBundle(args!!).mode) {
                        "AVAILABLE" -> getString(R.string.tab_available)
                        "TAKEN"     -> getString(R.string.tab_took)
                        "MINE"      -> getString(R.string.tab_my)
                        else        -> dest.label
                    }
                }
                R.id.requestsFragment -> {
                    val mode = RequestsFragmentArgs.fromBundle(args!!).mode
                    if (mode == "RECEIVED")
                        getString(R.string.tab_requests_i_received)
                    else getString(R.string.tab_requests_i_sent)
                }
                R.id.addEditLessonFragment -> {
                    val lessonId = AddEditLessonFragmentArgs.fromBundle(args!!).lessonId
                    if (lessonId.isNullOrBlank())
                        getString(R.string.title_add_lesson)
                    else getString(R.string.title_edit_lesson)
                }
                else -> dest.label
            }
            binding.toolbar.title = title
        }
    }


    override fun onStart() {
        super.onStart()

        auth.currentUser?.reload()
            ?.addOnFailureListener { e ->
                if (e is FirebaseAuthInvalidUserException) {
                    auth.signOut()
                }
            }
    }

    /* -------- Up‑button -------- */
    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()

    /* -------- Menu -------- */
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            auth.signOut()
            navController.navigate(R.id.authFragment)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
