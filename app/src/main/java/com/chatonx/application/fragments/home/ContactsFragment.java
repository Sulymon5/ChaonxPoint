package com.chatonx.application.fragments.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chatonx.application.R;
import com.chatonx.application.adapters.recyclerView.contacts.ContactsAdapter;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.interfaces.LoadingData;
import com.chatonx.application.models.users.Pusher;
import com.chatonx.application.models.users.contacts.ContactsModel;
import com.chatonx.application.models.users.contacts.PusherContacts;
import com.chatonx.application.presenters.users.ContactsPresenter;
import com.chatonx.application.ui.CustomProgressView;
import com.chatonx.application.ui.RecyclerViewFastScroller;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

/**
 * Created by Abderrahim El imame on 02/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsFragment extends Fragment implements LoadingData {

    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;
    @BindView(R.id.empty)
    LinearLayout emptyContacts;


    @BindView(R.id.progress_bar_load)
    CustomProgressView progressBarLoad;

    private RealmList<ContactsModel> mContactsModelList = new RealmList<>();
    private ContactsAdapter mContactsAdapter;
    //private ContactsPresenter mContactsPresenter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, mView);
        EventBus.getDefault().register(this);
       // mContactsPresenter = new ContactsPresenter(this);
       // mContactsPresenter.onCreate();
        initializerView();
        return mView;
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mContactsAdapter = new ContactsAdapter(mContactsModelList);
        setHasOptionsMenu(true);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        ContactsList.setAdapter(mContactsAdapter);
        ContactsList.setItemAnimator(new DefaultItemAnimator());
        ContactsList.getItemAnimator().setChangeDuration(0);

        //fix slow recyclerview start
        ContactsList.setHasFixedSize(true);
        ContactsList.setItemViewCacheSize(10);
        ContactsList.setDrawingCacheEnabled(true);
        ContactsList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        ///fix slow recyclerview end
        // set recycler view to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_contacts:
                //mContactsPresenter.onRefresh();
                break;
        }
        return true;
    }


    public void onProgressShow() {
        progressBarLoad.setVisibility(View.VISIBLE);
        progressBarLoad.setColor(AppHelper.getColor(getActivity(), R.color.colorPrimaryDark));
    }

    public void onProgressHide() {
        progressBarLoad.setVisibility(View.GONE);
    }

    /**
     * method to show contacts list
     *
     * @param contactsModelList this is parameter for  ShowContacts method
     */
    public void ShowContacts(List<ContactsModel> contactsModelList, boolean isRefresh) {
        RealmList<ContactsModel> contactsModels = new RealmList<ContactsModel>();
        for (ContactsModel contactsModel : contactsModelList) {
            contactsModels.add(contactsModel);
        }
        if (!isRefresh) {
            mContactsModelList = contactsModels;
        } else {
            mContactsAdapter.setContacts(contactsModels);
        }
        if (contactsModels.size() != 0) {
            fastScroller.setVisibility(View.VISIBLE);
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);

        } else {
            fastScroller.setVisibility(View.GONE);
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
        }
    }

    /**
     * method to update contacts
     *
     * @param contactsModelList this is parameter for  updateContacts method
     */
    public void updateContacts(List<ContactsModel> contactsModelList) {
        RealmList<ContactsModel> contactsModels = new RealmList<ContactsModel>();
        contactsModels.addAll(contactsModelList);
        this.mContactsModelList = contactsModels;
        //mContactsPresenter.getContacts(true);
    }


    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PusherContacts pusher) {
       // mContactsPresenter.onEventMainThread(pusher);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //mContactsPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onShowLoading() {

    }

    @Override
    public void onHideLoading() {

    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Contacts Fragment " + throwable.getMessage());

    }


    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_UPDATE_USER_STATE:
                if (pusher.getData().equals(AppConstants.EVENT_BUS_USER_IS_ONLINE))
                    mContactsAdapter.updateItem(pusher.getUserID(),true);
                else if (pusher.getData().equals(AppConstants.EVENT_BUS_USER_IS_OFFLINE))
                    mContactsAdapter.updateItem(pusher.getUserID(),false);
                break;
        }
    }
}