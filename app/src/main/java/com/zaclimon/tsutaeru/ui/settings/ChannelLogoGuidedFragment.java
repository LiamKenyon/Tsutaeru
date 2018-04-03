package com.zaclimon.tsutaeru.ui.settings;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepSupportFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;

import com.google.android.media.tv.companionlibrary.EpgSyncJobService;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.utils.TvContractUtils;
import com.zaclimon.tsutaeru.R;
import com.zaclimon.tsutaeru.service.TsutaeruJobService;
import com.zaclimon.tsutaeru.util.Constants;

import java.util.List;

/**
 * Setting fragment which will either enable or disable the channel logos as seen on
 * the Live Channels application
 *
 * @author zaclimon
 * Creation date: 23/06/17
 */

public class ChannelLogoGuidedFragment extends GuidedStepSupportFragment {

    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.TSUTAERU_PREFERENCES, Context.MODE_PRIVATE);
        boolean logoActivated = sharedPreferences.getBoolean(Constants.CHANNEL_LOGO_PREFERENCE, true);
        String title = getString(R.string.channel_logo_title);
        String description = getString(R.string.channel_logo_description);
        String breadcrumb;

        if (logoActivated) {
            breadcrumb = getString(R.string.current_status_text, getString(R.string.activated_text));
        } else {
            breadcrumb = getString(R.string.current_status_text, getString(R.string.deactivated_text));
        }

        return (new GuidanceStylist.Guidance(title, description, breadcrumb, null));
    }

    @Override
    public void onCreateActions(List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction.Builder yesAction = new GuidedAction.Builder(getActivity());
        GuidedAction.Builder noAction = new GuidedAction.Builder(getActivity());
        yesAction.title(R.string.yes_text);
        noAction.title(R.string.no_text);
        yesAction.id(GuidedAction.ACTION_ID_YES);
        noAction.id(GuidedAction.ACTION_ID_NO);
        actions.add(yesAction.build());
        actions.add(noAction.build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction guidedAction) {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.TSUTAERU_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean initialAction = sharedPreferences.getBoolean(Constants.CHANNEL_LOGO_PREFERENCE, true);

        if (guidedAction.getId() == GuidedAction.ACTION_ID_YES) {
            editor.putBoolean(Constants.CHANNEL_LOGO_PREFERENCE, true);
        } else {
            editor.putBoolean(Constants.CHANNEL_LOGO_PREFERENCE, false);
        }

        editor.apply();

        boolean modifiedAction = sharedPreferences.getBoolean(Constants.CHANNEL_LOGO_PREFERENCE, true);

         /*
          Sync the channels to reflect the latest changes only if the stream type is different from
          before. Remove only the logos if the user doesn't want them. In that case, do an AsyncTask
          since it might freeze the user experience.
          */

        if (modifiedAction != initialAction) {

            if (!modifiedAction) {
                new AsyncRemoveLogos().execute();
            }

            String inputId = TvContract.buildInputId(Constants.TV_INPUT_SERVICE_COMPONENT);
            EpgSyncJobService.requestImmediateSync(getActivity(), inputId, new ComponentName(getActivity(), TsutaeruJobService.class));
            add(getFragmentManager(), new EpgSyncLoadingGuidedFragment());
        } else {
            getActivity().finish();
        }
    }

    /**
     * Private class that will remove all Ace TV channels logo's from the system's database
     * in a asynchronous way.
     *
     * @author zaclimon
     * Creation date: 23/06/17
     */
    private class AsyncRemoveLogos extends AsyncTask<Void, Void, Void> {

        public Void doInBackground(Void... params) {

            if (isAdded()) {
                ContentResolver contentResolver = getActivity().getContentResolver();
                List<Channel> channels = TvContractUtils.getChannels(contentResolver);

                for (Channel channel : channels) {
                    Uri channelLogoUri = TvContract.buildChannelLogoUri(channel.getId());
                    contentResolver.delete(channelLogoUri, null, null);
                }
            }
            return (null);
        }

    }

}