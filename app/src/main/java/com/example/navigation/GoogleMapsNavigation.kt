package com.example.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun openGoogleMapsNavigation(
    context: Context,
    destination: String,
    latitude: Double?,
    longitude: Double?,
    placeId: String?
) {
    val destinationValue = if (latitude != null && longitude != null) {
        "$latitude,$longitude"
    } else {
        destination
    }

    val encodedDestination = Uri.encode(destinationValue)
    val encodedPlaceId = placeId?.takeIf { it.isNotBlank() }?.let(Uri::encode)
    val url = buildString {
        append("https://www.google.com/maps/dir/?api=1")
        append("&destination=").append(encodedDestination)
        if (encodedPlaceId != null) {
            append("&destination_place_id=").append(encodedPlaceId)
        }
        append("&travelmode=driving")
        append("&dir_action=navigate")
    }

    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Toast.makeText(context, "Unable to open Google Maps", Toast.LENGTH_SHORT).show()
    }
}
