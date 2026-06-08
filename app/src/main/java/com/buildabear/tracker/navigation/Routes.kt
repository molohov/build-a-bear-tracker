package com.buildabear.tracker.navigation

object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{bearId}"
    const val CUSTOM_NEW = "custom/new"
    const val CUSTOM_EDIT = "custom/edit/{bearId}"
    const val FILTERS = "filters"
    const val FILTER_EDIT = "filters/edit?filterId={filterId}"
    const val SETTINGS = "settings"

    fun detail(bearId: String) = "detail/$bearId"
    fun customEdit(bearId: String) = "custom/edit/$bearId"
    fun filterEdit(filterId: String? = null) = "filters/edit?filterId=${filterId ?: ""}"
}
