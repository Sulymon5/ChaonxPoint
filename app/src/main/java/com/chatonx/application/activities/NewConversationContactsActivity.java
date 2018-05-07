package com.chatonx.application.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.chatonx.application.R;
import com.chatonx.application.activities.groups.AddMembersToGroupActivity;
import com.chatonx.application.adapters.others.TextWatcherAdapter;
import com.chatonx.application.adapters.recyclerView.contacts.ContactsAdapter;
import com.chatonx.application.animations.AnimationsUtil;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.app.ChatonXApplication;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.PreferenceManager;
import com.chatonx.application.interfaces.LoadingData;
import com.chatonx.application.models.users.contacts.ContactsModel;
import com.chatonx.application.presenters.users.ContactsPresenter;
import com.chatonx.application.ui.RecyclerViewFastScroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class NewConversationContactsActivity extends AppCompatActivity implements LoadingData {
    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.empty)
    LinearLayout emptyContacts;
    private ContactsAdapter mSelectContactsAdapter;
    private ContactsPresenter mContactsPresenter;

    @BindView(R.id.toolbar_progress_bar)
    ProgressBar toolbarProgressBar;

    @BindView(R.id.close_btn_search_view)
    ImageView closeBtn;
    @BindView(R.id.search_input)
    TextInputEditText searchInput;
    @BindView(R.id.clear_btn_search_view)
    ImageView clearBtn;
    @BindView(R.id.app_bar_search_view)
    View searchView;

    @BindView(R.id.main_view)
    LinearLayout MainView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);
        ButterKnife.bind(this);

        searchInput.setFocusable(true);
        initializerSearchView(searchInput, clearBtn);
        initializerView();
        setTypeFaces();
        mContactsPresenter = new ContactsPresenter(this);
       mContactsPresenter.onCreate();


    }

    private void setTypeFaces() {
        if (AppConstants.ENABLE_FONTS_TYPES) {
            searchInput.setTypeface(AppHelper.setTypeFace(this, "Futura"));
        }
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_select_contacts));

        }
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mSelectContactsAdapter = new ContactsAdapter();
        ContactsList.setLayoutManager(mLinearLayoutManager);
        ContactsList.setAdapter(mSelectContactsAdapter);

        //fix slow recyclerview start
        ContactsList.setHasFixedSize(true);
        ContactsList.setItemViewCacheSize(10);
        ContactsList.setDrawingCacheEnabled(true);
        ContactsList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // set recyclerView to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);

        closeBtn.setOnClickListener(v -> closeSearchView());
        clearBtn.setOnClickListener(v -> clearSearchView());
    }

    /**
     * method to initialize the search view
     */
    public void initializerSearchView(TextInputEditText searchInput, ImageView clearSearchBtn) {

        final Context context = this;
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        });
        searchInput.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                clearSearchBtn.setVisibility(View.GONE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSelectContactsAdapter.setString(s.toString());
                Search(s.toString().trim());
                clearSearchBtn.setVisibility(View.VISIBLE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    clearSearchBtn.setVisibility(View.GONE);
                    mContactsPresenter.getContacts();
                }
            }
        });

    }


    @SuppressWarnings("unused")
    @OnClick(R.id.new_group)
    public void newGroup() {
        startActivity(new Intent(this, AddMembersToGroupActivity.class));
        finish();
        AnimationsUtil.setSlideInAnimation(this);
    }

    /**
     * method to close the searchview with animation
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.close_btn_search_view)
    public void closeSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_for_button_animtion_exit);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    private void launcherSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_for_button_animtion_enter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_contacts:
                launcherSearchView();
                break;
            case R.id.refresh_contacts:
                mContactsPresenter.onRefresh();
                break;
            case android.R.id.home:
                finish();

                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setSlideInAnimation(this);
    }
    /**
     * method to show contacts list
     *
     * @param contactsModels this is parameter for ShowContacts  method
     */
    public void ShowContacts(List<ContactsModel> contactsModels) {

        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(String.format(Locale.getDefault(), "%s " + getResources().getString(R.string.of) + "%s", contactsModels.size(), PreferenceManager.getContactSize(this)));
        if (contactsModels.size() != 0) {
            fastScroller.setVisibility(View.VISIBLE);
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);
            RealmList<ContactsModel> usersModelRealmList = new RealmList<>();
            usersModelRealmList.addAll(contactsModels);
            mSelectContactsAdapter.setContacts(usersModelRealmList);
            /*
            mContactsModelList = usersModelRealmList;
            mSelectContactsAdapter.setContacts(usersModelRealmList);*/
        } else {
            fastScroller.setVisibility(View.GONE);
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainView.setVisibility(View.GONE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        MainView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onShowLoading() {
        toolbarProgressBar.setVisibility(View.VISIBLE);
        toolbarProgressBar.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onHideLoading() {
        toolbarProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Contacts Fragment " + throwable.getMessage());

        toolbarProgressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_menu, menu);
        return true;
    }

    /**
     * method to clear/reset the search view
     */
    public void clearSearchView() {
        if (searchInput.getText() != null) {
            searchInput.setText("");
            mContactsPresenter.getContacts();
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);
        }
    }

    /**
     * method to start searching
     *
     * @param string this  is parameter for Search method
     */
    public void Search(String string) {

        List<ContactsModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);
            mSelectContactsAdapter.animateTo(filteredModelList);
            ContactsList.scrollToPosition(0);
        } else {
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
        }
    }

    /**
     * method to filter the list of contacts
     *
     * @param query this parameter for FilterList  method
     * @return this for what method will return
     */
    private List<ContactsModel> FilterList(String query) {
        Realm realm = ChatonXApplication.getRealmDatabaseInstance();
        List<ContactsModel> contactsModelList = new ArrayList<>();

        List<ContactsModel> usersModels = realm.where(ContactsModel.class)
                .equalTo("Exist", true)
                .notEqualTo("id", PreferenceManager.getID(this))
                .beginGroup()
                .contains("phone", query, Case.INSENSITIVE)
                .or()
                .contains("username", query, Case.INSENSITIVE)
                .endGroup()
                .findAll();
        contactsModelList.addAll(usersModels);

        if (!realm.isClosed())
            realm.close();
        return contactsModelList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mContactsPresenter!=null)
        mContactsPresenter.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


}
