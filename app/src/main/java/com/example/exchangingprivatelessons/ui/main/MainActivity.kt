/* ui/main/MainActivity.kt */
package com.example.exchangingprivatelessons.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.ActivityMainBinding
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
