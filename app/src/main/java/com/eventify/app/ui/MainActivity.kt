package com.eventify.app.ui
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import com.eventify.app.databinding.ActivityMainBinding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.eventify.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    var navController: NavController? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        FirebaseFirestore.getInstance().clearPersistence()

        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)


        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as? NavHostFragment
        navController = navHostFragment?.navController
        navController?.let {
            NavigationUI.setupActionBarWithNavController(
                activity = this,
                navController = it
            )
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_bar)
        navController?.let { NavigationUI.setupWithNavController(bottomNavigationView, it) }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (navController?.currentBackStackEntry?.destination?.id != navController?.graph?.startDestinationId) {
                    navController?.popBackStack()
                } else {
                    finish()
                }
            }
            R.id.logout -> {
                auth.signOut() // Firebase sign-out
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> navController?.let { NavigationUI.onNavDestinationSelected(item, it) }
        }
        return true
    }
}

