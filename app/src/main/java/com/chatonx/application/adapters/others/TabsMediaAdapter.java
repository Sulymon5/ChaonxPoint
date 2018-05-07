package com.chatonx.application.adapters.others;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.chatonx.application.fragments.media.DocumentsFragment;
import com.chatonx.application.fragments.media.LinksFragment;
import com.chatonx.application.fragments.media.MediaFragment;

/**
 * Created by mac
 */
public class TabsMediaAdapter extends FragmentPagerAdapter {


    public TabsMediaAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MediaFragment.newInstance("MEDIA");
            case 1:
                return DocumentsFragment.newInstance("DOCUMENTS");
            case 2:
                return LinksFragment.newInstance("LINKS");
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "MEDIA";
            case 1:
                return "DOCUMENTS";
            case 2:
            default:
                return "LINKS";
        }
    }
}