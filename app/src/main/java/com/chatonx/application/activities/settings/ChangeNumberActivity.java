package com.chatonx.application.activities.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.chatonx.application.R;
import com.chatonx.application.animations.AnimationsUtil;

/**
 * Created by Abderrahim El imame on 8/17/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class ChangeNumberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setSlideInAnimation(this);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
