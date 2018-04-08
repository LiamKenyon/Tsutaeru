package com.zaclimon.tsutaeru.ui.settings

import android.content.Context
import android.os.Bundle
import android.support.v17.leanback.app.GuidedStepSupportFragment
import android.support.v17.leanback.widget.GuidanceStylist
import android.support.v17.leanback.widget.GuidedAction
import com.zaclimon.tsutaeru.R
import com.zaclimon.tsutaeru.util.Constants

/**
 * Settings section that enables one to use an installed player instead of the integrated one
 * from the application.
 *
 * @author zaclimon
 */
class VodExternalPlayerGuidedFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        val sharedPreferences = context?.getSharedPreferences(Constants.TSUTAERU_PREFERENCES, Context.MODE_PRIVATE)
        val isExternalPlayerUsed = sharedPreferences?.getBoolean(Constants.EXTERNAL_PLAYER_PREFERENCE, false) ?: false

        val title = getString(R.string.epg_offset_title)
        val description = getString(R.string.epg_offset_description)
        val breadcrumb = if (isExternalPlayerUsed) { getString(R.string.activated_text) } else { getString(R.string.deactivated_text) }

        return GuidanceStylist.Guidance(title, description, breadcrumb, null)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        val yesAction = GuidedAction.Builder(context)
        val noAction = GuidedAction.Builder(context)
        yesAction.title(R.string.yes_text)
        noAction.title(R.string.no_text)
        yesAction.id(GuidedAction.ACTION_ID_YES)
        noAction.id(GuidedAction.ACTION_ID_NO)
        actions.add(yesAction.build())
        actions.add(noAction.build())
    }

    override fun onGuidedActionClicked(action: GuidedAction?) {
        val editor = context?.getSharedPreferences(Constants.TSUTAERU_PREFERENCES, Context.MODE_PRIVATE)?.edit()
        val id = action?.id

        editor?.putBoolean(Constants.EXTERNAL_PLAYER_PREFERENCE, (id == GuidedAction.ACTION_ID_YES))
        editor?.apply()
        activity?.finish()
    }
}