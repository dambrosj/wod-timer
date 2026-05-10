package com.wod.app.ui

/** Navigation route constants — keep them in one place to avoid typos. */
object Routes {
    const val HOME         = "home"
    const val CONFIG       = "config/{type}"
    const val TIMER        = "timer/{type}"
    const val COMPLETION   = "completion"
    const val DIARY        = "diary"
    const val DIARY_DETAIL = "diary/detail"
    const val WODS         = "wods"
    const val WOD_DETAIL   = "wods/{id}"
    const val WOD_EDIT     = "wods/{id}/edit"
    const val SETTINGS     = "settings"

    fun config(type: String) = "config/$type"
    fun timer(type: String)  = "timer/$type"
    fun wodDetail(id: String) = "wods/$id"
    fun wodEdit(id: String)  = "wods/$id/edit"
}
