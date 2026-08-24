package ir.mafatih.aljinan.util

import android.content.Context
import android.content.Intent
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import ir.mafatih.aljinan.R

private const val MENU_ID_SHARE_SELECTION = 9001

/**
 * Makes the TextView's content selectable (long-press to get selection
 * handles, drag to extend) and adds a "Share" action to the selection
 * floating toolbar that sends exactly the highlighted substring via the
 * system share sheet. Implemented with an explicit ActionMode.Callback
 * (rather than relying on OEM defaults) so the action is guaranteed to
 * show up consistently across devices/Android versions.
 */
fun TextView.enableSelectableShare(context: Context, shareTitle: String) {
    // Guard against re-initializing selection state on every RecyclerView
    // rebind: repeatedly calling setTextIsSelectable(true) on an already
    // selectable TextView recreates its internal touch/gesture handling,
    // which on recycled views can leave long-press no longer responding.
    if (!isTextSelectable) {
        setTextIsSelectable(true)
    }
    customSelectionActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean = true

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if (menu?.findItem(MENU_ID_SHARE_SELECTION) == null) {
                menu?.add(0, MENU_ID_SHARE_SELECTION, 0, context.getString(R.string.action_share))
            }
            return true
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            if (item?.itemId != MENU_ID_SHARE_SELECTION) return false
            val start = selectionStart
            val end = selectionEnd
            if (start in 0 until end && end <= (text?.length ?: 0)) {
                val selected = text.subSequence(start, end).toString()
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, selected)
                }
                context.startActivity(Intent.createChooser(sendIntent, shareTitle))
            }
            mode?.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {}
    }
}
