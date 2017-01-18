package com.olmatix.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahman on 12/18/2016.
 */

public class OlmatixPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private Context mContext;

    public OlmatixPagerAdapter(FragmentManager manager) {
        super(manager);
    }

//    public View getTabView(int position) {
//        // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
//        View v = LayoutInflater.from(mContext).inflate(R.layout.costum_tab, null);
//        TextView tv = (TextView) v.findViewById(R.id.tab_text);
//        tv.setText(mFragmentTitleList.get(position));
//        ImageView img = (ImageView) v.findViewById(R.id.tab_icon);
//        img.setImageResource(MainActivity.tabIcons[position]);
//        return v;
//    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return mFragmentTitleList.get(position);
    }
}
