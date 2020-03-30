package org.odk.collect.android.formlist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.utilities.PermissionUtils;

import static org.odk.collect.android.utilities.PermissionUtils.finishAllActivities;

public class FormChooserListActivity extends CollectAbstractActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_chooser_list);
        initToolbar();

        new PermissionUtils().requestStoragePermissions(this, new PermissionListener() {
            @Override
            public void granted() {
                // must be at the beginning of any activity that can be called from an external intent
                try {
                    new StorageInitializer().createOdkDirsOnStorage();
                } catch (RuntimeException e) {
                    createErrorDialog(e.getMessage());
                    return;
                }
            }

            @Override
            public void denied() {
                finishAllActivities(FormChooserListActivity.this);
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.enter_data));
        setSupportActionBar(toolbar);
    }

    private void createErrorDialog(String errorMsg) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = (dialog, i) -> {
            if (i == DialogInterface.BUTTON_POSITIVE) {
                finish();
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(getString(R.string.ok), errorListener);
        alertDialog.show();
    }
}
