package com.rudderstack.android.sample.kotlin

import android.app.Application
import com.rudderstack.android.integrations.leanplum.LeanPlumIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1ZOVzjHRL0Vpk627qpkmcIYLrv3"
        private const val DATA_PLANE_URL = "https://edbe3859.ngrok.io"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withFactory(LeanPlumIntegrationFactory.FACTORY)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .build()
        )
    }
}