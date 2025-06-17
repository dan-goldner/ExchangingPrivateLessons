package com.example.exchangingprivatelessons

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp                     // ext-fun
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // הסר כל טיפול ב־insets ידנית
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        val graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
            setStartDestination(
                if (FirebaseAuth.getInstance().currentUser != null)
                    R.id.takeLessonsFragment else R.id.loginFragment
            )
        }
        navController.graph = graph

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.signUpFragment, R.id.takeLessonsFragment)
        )
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}
