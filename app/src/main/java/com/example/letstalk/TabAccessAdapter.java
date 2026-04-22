package com.example.letstalk;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabAccessAdapter extends FragmentPagerAdapter {

    public TabAccessAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int i) {

        switch (i) {

            case 0:
                return new ChatsFragment();

            case 1:
                return new GroupFragment();

            case 2:
                return new ContactsFragment();

            case 3:
                return new ContactsFragment(); // (keep as you wrote)

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {

            case 0:
                return "Chats";

            case 1:
                return "Groups";

            case 2:
                return "Contacts";

            case 3:
                return "Friends";

            default:
                return null;
        }
    }
}