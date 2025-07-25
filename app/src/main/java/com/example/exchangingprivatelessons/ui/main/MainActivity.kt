/* ui/main/MainActivity.kt */
package com.example.exchangingprivatelessons.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.ActivityMainBinding
import com.example.exchangingprivatelessons.ui.lesson.AddEditLessonFragmentArgs
import com.example.exchangingprivatelessons.ui.lesson.LessonListFragmentArgs
import com.example.exchangingprivatelessons.ui.request.RequestsFragmentArgs
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding      : ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig : AppBarConfiguration

    /** יוצר את MainViewModel – מפעיל את ה‑Live‑Sync הגלובלי */
    private val mainVm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                // לא מחובר → נווט ל‑Auth אם לא שם כבר
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

        /* Toolbar */
        setSupportActionBar(binding.toolbar)

        /* NavController */
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        appBarConfig = AppBarConfiguration
            .Builder(R.id.homeFragment, R.id.authFragment)
            .build()
        setupActionBarWithNavController(navController, appBarConfig)

        /* כפתור Logout מוצג רק במסך הבית */
        navController.addOnDestinationChangedListener { _, dest, _ ->
            invalidateOptionsMenu()
            binding.toolbar.menu.findItem(R.id.action_logout)?.isVisible =
                dest.id == R.id.homeFragment
        }
        // MainActivity.kt  – בתוך onCreate אחרי שהגדרת navController
        navController.addOnDestinationChangedListener { _, dest, args ->

            val title = when (dest.id) {

                /* -------- Lessons list -------- */
                R.id.lessonListFragment -> {
                    val mode = LessonListFragmentArgs.fromBundle(args!!).mode
                    when (mode) {
                        "AVAILABLE" -> getString(R.string.tab_available)
                        "TAKEN"     -> getString(R.string.tab_took)
                        "MINE"      -> getString(R.string.tab_my)
                        else        -> dest.label          // fallback
                    }
                }

                /* -------- Requests -------- */
                R.id.requestsFragment -> {
                    val mode = RequestsFragmentArgs.fromBundle(args!!).mode
                    if (mode == "RECEIVED")
                        getString(R.string.tab_requests_i_received)
                    else getString(R.string.tab_requests_i_sent)
                }

                /* -------- Add / Edit Lesson -------- */
                R.id.addEditLessonFragment -> {
                    val lessonId = AddEditLessonFragmentArgs
                        .fromBundle(args!!)
                        .lessonId
                    if (lessonId.isNullOrBlank())
                        getString(R.string.title_add_lesson)
                    else  getString(R.string.title_edit_lesson)
                }

                /* -------- מסכים אחרים – השתמש בלייבל מה‑navGraph -------- */
                else -> dest.label
            }

            binding.toolbar.title = title
        }

    }



    /* Up‑button */
    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()

    /* תפריט */
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            FirebaseAuth.getInstance().signOut()
            navController.navigate(R.id.authFragment)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
