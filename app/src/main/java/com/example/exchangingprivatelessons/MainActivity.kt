package com.example.exchangingprivatelessons

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        /* 1. Toolbar */
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        /* 2. NavController */
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        /* 3. גרף עם StartDestination דינמי */
        val graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
            setStartDestination(
                if (FirebaseAuth.getInstance().currentUser != null)
                    R.id.homeFragment else R.id.loginFragment
            )
        }
        navController.graph = graph

        /* 4. AppBarConfiguration (מסכי Top-Level) */
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.signUpFragment, R.id.homeFragment)
        )

        /* 5. חיבור ActionBar ← NavController */
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}
