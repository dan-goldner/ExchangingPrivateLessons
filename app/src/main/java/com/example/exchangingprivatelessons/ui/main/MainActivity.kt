package com.example.exchangingprivatelessons.ui.main

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration
    private var showLogout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Toolbar = ActionBar */
        setSupportActionBar(binding.toolbar)

        /* NavController */
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        /* Home הוא ה‑Top‑Level היחיד */
        appBarConfig = AppBarConfiguration
            .Builder(R.id.homeFragment, R.id.authFragment)
            .build()

        /* קישור ActionBar ↔︎ NavController */
        setupActionBarWithNavController(navController, appBarConfig)

        /* הצגת כפתור Logout רק במסך‑הבית */
        navController.addOnDestinationChangedListener { _, dest, _ ->
            showLogout = dest.id == R.id.homeFragment
            invalidateOptionsMenu()
        }
    }

    /** Up‑button */
    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()

    /** תפריט */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_logout)?.isVisible = showLogout
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            FirebaseAuth.getInstance().signOut()
            navController.navigate(R.id.authFragment)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
