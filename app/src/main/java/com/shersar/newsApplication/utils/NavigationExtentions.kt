package com.shersar.newsApplication.utils

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import com.shersar.newsApplication.R


fun Fragment.activityNavController() = requireActivity().findNavController(R.id.newsNavHostFragment)

fun NavController.navigateSafely(@IdRes actionId: Int){
    currentDestination?.getAction(actionId)?.let { navigate(actionId) }
}

fun NavController.navigateSafely(directions: NavDirections){
    currentDestination?.getAction(directions.actionId)?.let { navigate(directions) }
}

fun NavController.navigateSafe(@IdRes resActionId: Int, args: Bundle?, hasPopUp: Boolean = false) {
    val destinationId = currentDestination?.getAction(resActionId)?.destinationId
    currentDestination?.let { node ->
        val currentNode = when (node) {
            is NavGraph -> node
            else -> node.parent
        }
        if (destinationId != null) {
            currentNode?.findNode(destinationId)
                ?.let { navigate(resActionId, args) }
        }
    }
}