package org.odk.collect.android.formlist;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormMapActivity;
import org.odk.collect.android.adapters.FormListAdapter;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.fragments.FormListFragment;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.PermissionUtils;

import static android.app.Activity.RESULT_OK;

public class FormChooserListFragment extends FormListFragment {

    @Override
    public void onViewCreated(@NonNull View rootView, Bundle savedInstanceState) {
        sortingOptions = new int[] {
                R.string.sort_by_name_asc, R.string.sort_by_name_desc,
                R.string.sort_by_date_asc, R.string.sort_by_date_desc,
        };

        setupAdapter();

        super.onViewCreated(rootView, savedInstanceState);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        hideProgressBarIfAllowed();
        listAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        listAdapter.swapCursor(null);
    }

    private boolean hideOldFormVersions() {
        return GeneralSharedPreferences.getInstance().getBoolean(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS, false);
    }

    @Override
    protected CursorLoader getCursorLoader() {
        return new FormsDao().getFormsCursorLoader(getFilterText(), getSortingOrder(), hideOldFormVersions());
    }

    @Override
    protected String getSortingOrderKey() {
        return "formChooserListSortingOrder";
    }

    @Override
    public void onListItemClick(ListView listView, View v, int position, long rowId) {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            // get uri to form
            long idFormsTable = listView.getAdapter().getItemId(position);
            Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, idFormsTable);

            String action = getActivity().getIntent().getAction();
            if (Intent.ACTION_PICK.equals(action)) {
                // caller is waiting on a picked form
                getActivity().setResult(RESULT_OK, new Intent().setData(formUri));
            } else {
                // caller wants to view/edit a form, so launch formentryactivity
                Intent intent = new Intent(Intent.ACTION_EDIT, formUri);
                intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                startActivity(intent);
            }

            getActivity().finish();
        }
    }

    private void init() {
        sortingOptions = new int[] {
                R.string.sort_by_name_asc, R.string.sort_by_name_desc,
                R.string.sort_by_date_asc, R.string.sort_by_date_desc,
        };

        setupAdapter();
    }

    private void setupAdapter() {
        String[] columnNames = {
                FormsProviderAPI.FormsColumns.DISPLAY_NAME,
                FormsProviderAPI.FormsColumns.JR_VERSION,
                hideOldFormVersions() ? FormsProviderAPI.FormsColumns.MAX_DATE : FormsProviderAPI.FormsColumns.DATE,
                FormsProviderAPI.FormsColumns.GEOMETRY_XPATH
        };
        int[] viewIds = {
                R.id.form_title,
                R.id.form_subtitle,
                R.id.form_subtitle2,
                R.id.map_view
        };

        listAdapter = new FormListAdapter(
                getListView(), FormsProviderAPI.FormsColumns.JR_VERSION, getActivity(), R.layout.form_chooser_list_item,
                this::onMapButtonClick, columnNames, viewIds);
        getListView().setAdapter(listAdapter);
    }

    private void onMapButtonClick(AdapterView<?> parent, View view, int position, long id) {
        final Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, id);
        final Intent intent = new Intent(Intent.ACTION_EDIT, formUri, getActivity(), FormMapActivity.class);
        new PermissionUtils().requestLocationPermissions(getActivity(), new PermissionListener() {
            @Override public void granted() {
                startActivity(intent);
            }

            @Override public void denied() { }
        });
    }
}
