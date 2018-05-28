package org.thewdj.blueserial;

import android.os.Bundle;
import android.app.ActionBar;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import org.thewdj.blueserial.core.Controller;

/**
 * Created by drzzm on 2016.11.5.
 */
public class ControllerActivity extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(new Controller(), config);
    }

}
