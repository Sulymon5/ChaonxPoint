package com.chatonx.application.adapters.recyclerView.media;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatonx.application.R;
import com.chatonx.application.app.AppConstants;
import com.chatonx.application.helpers.AppHelper;
import com.chatonx.application.helpers.UtilsString;
import com.chatonx.application.models.messages.MessagesModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class LinksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<MessagesModel> mMessagesModel;
    private LayoutInflater mInflater;

    public LinksAdapter(Activity mActivity) {
        this.mActivity = mActivity;
        mInflater = LayoutInflater.from(mActivity);
    }

    public void setMessages(List<MessagesModel> mMessagesList) {
        this.mMessagesModel = mMessagesList;
        notifyDataSetChanged();
    }


    public List<MessagesModel> getMessages() {
        return mMessagesModel;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_links, parent, false);
        return new LinksViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final LinksViewHolder linksViewHolder = (LinksViewHolder) holder;
        final MessagesModel messagesModel = this.mMessagesModel.get(position);
        try {
            if (messagesModel.getMessage() != null) {
                if (UtilsString.checkForUrls(messagesModel.getMessage())) {
                    String url = UtilsString.getUrl(messagesModel.getMessage());
                    if (url != null)
                        if (!url.startsWith("http://")) {
                            if (!url.startsWith("https://")) {
                                url = (new StringBuilder()).append("http://").append(url).toString();
                            }
                        }
                    AppHelper.LogCat(" valid " + url);
                    linksViewHolder.urlLink.setText(url);

                }
            }

        } catch (Exception e) {
            AppHelper.LogCat("" + e.getMessage());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mMessagesModel != null) {
            return mMessagesModel.size();
        } else {
            return 0;
        }
    }


    public class LinksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.image_preview)
        ImageView imagePreview;

        @BindView(R.id.title_link)
        TextView titleLink;

        @BindView(R.id.description)
        TextView descriptionLink;

        @BindView(R.id.url)
        TextView urlLink;

        LinksViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setTypeFaces();
            itemView.setOnClickListener(this);
        }


        private void setTypeFaces() {
            if (AppConstants.ENABLE_FONTS_TYPES) {
                titleLink.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
                descriptionLink.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
                urlLink.setTypeface(AppHelper.setTypeFace(mActivity, "Futura"));
            }
        }

        @Override
        public void onClick(View view) {
            MessagesModel messagesModel = mMessagesModel.get(getAdapterPosition());
            String url = UtilsString.getUrl(messagesModel.getMessage());
            if (url != null)
                if (!url.startsWith("http://")) {
                    if (!url.startsWith("https://")) {
                        url = (new StringBuilder()).append("http://").append(url).toString();
                    }
                }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mActivity.startActivity(intent);
        }
    }
}

