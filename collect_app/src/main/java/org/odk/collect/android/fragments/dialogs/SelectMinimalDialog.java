package org.odk.collect.android.fragments.dialogs;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;

import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.databinding.RecyclerViewBinding;
import org.odk.collect.android.fragments.viewmodels.SelectMinimalViewModel;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.material.MaterialFullScreenDialogFragment;

public class SelectMinimalDialog extends MaterialFullScreenDialogFragment {
    private RecyclerViewBinding binding;

    private AbstractSelectListAdapter selectListAdapter;
    private FormEntryPrompt formEntryPrompt;

    private SelectMinimalViewModel viewModel;

    public SelectMinimalDialog(AbstractSelectListAdapter selectListAdapter, FormEntryPrompt formEntryPrompt) {
        this.selectListAdapter = selectListAdapter;
        this.formEntryPrompt = formEntryPrompt;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this, new SelectMinimalViewModel.Factory(selectListAdapter, formEntryPrompt)).get(SelectMinimalViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycler_view, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        binding = RecyclerViewBinding.inflate(((Activity) getActivity()).getLayoutInflater());

        setUpToolbar();
        setUpRecyclerView();
    }

    @Override
    protected void onCloseClicked() {

    }

    @Override
    protected void onBackPressed() {

    }

    @Nullable
    @Override
    protected Toolbar getToolbar() {
        return getView().findViewById(R.id.toolbar);
    }

    private void setUpToolbar() {
        getToolbar().setTitle("Select");
        getToolbar().setNavigationIcon(null);
    }

    protected void setUpRecyclerView() {
        RecyclerView recyclerView = binding.getRoot();

        if (WidgetAppearanceUtils.isFlexAppearance(viewModel.getFormEntryPrompt())) {
            setUpFlexRecyclerView(recyclerView);
        } else {
            setUpGridRecyclerView(recyclerView);
        }

        recyclerView.setAdapter(viewModel.getSelectListAdapter());
    }

    private void setUpFlexRecyclerView(RecyclerView recyclerView) {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    private void setUpGridRecyclerView(RecyclerView recyclerView) {
        int numColumns = getNumOfColumns();

        if (numColumns == 1) {
            DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.inset_divider_64dp);

            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                DrawableCompat.setTint(DrawableCompat.wrap(drawable), new ThemeUtils(getContext()).getColorOnSurface());
            }

            divider.setDrawable(drawable);
            recyclerView.addItemDecoration(divider);
        }

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numColumns));
    }

    private int getNumOfColumns() {
        return WidgetAppearanceUtils.getNumberOfColumns(viewModel.getFormEntryPrompt(), getContext());
    }
}
