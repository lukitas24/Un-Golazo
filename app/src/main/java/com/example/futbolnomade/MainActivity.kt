package com.example.futbolnomade

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.FragmentActivity
import com.example.futbolnomade.presentation.navigation.AppNavigation
import com.example.futbolnomade.presentation.notification.NotificationTarget

class MainActivity : FragmentActivity() {

    private val notificationTarget =
        mutableStateOf<NotificationTarget?>(
            null
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        notificationTarget.value =
            NotificationTarget.fromIntent(
                intent
            )

        setContent {
            AppNavigation(
                notificationTarget =
                    notificationTarget.value,

                onNotificationHandled = {
                    notificationTarget.value =
                        null
                }
            )
        }
    }

    override fun onNewIntent(
        intent: Intent
    ) {
        super.onNewIntent(intent)

        setIntent(intent)

        notificationTarget.value =
            NotificationTarget.fromIntent(
                intent
            )
    }
}