package com.example.exchangingprivatelessons

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.ui.navigateUp


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. Toolbar
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        // 2. navController
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController                // ← navController מוקצה כאן

        // 3. גרף עם יעד פתיחה דינמי
        val graph = navController.navInflater
            .inflate(R.navigation.nav_graph)
            .apply {
                setStartDestination(
                    if (FirebaseAuth.getInstance().currentUser != null)
                        R.id.homeFragment else R.id.loginFragment
                )
            }
        navController.graph = graph

        // 4. AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.signUpFragment, R.id.homeFragment)
        )

        // 5. חיבור ה-Toolbar
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }


    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}
