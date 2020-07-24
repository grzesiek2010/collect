package org.odk.collect.android.fragments.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.databinding.SelectMinimalDialogLayoutBinding;
import org.odk.collect.android.fragments.viewmodels.SelectMinimalViewModel;
import org.odk.collect.android.widgets.interfaces.BinaryDataReceiver;
import org.odk.collect.material.MaterialFullScreenDialogFragment;

public class SelectMinimalDialog extends MaterialFullScreenDialogFragment {
    private SelectMinimalDialogLayoutBinding binding;

    private AbstractSelectListAdapter selectListAdapter;
    private boolean isFlex;
    private boolean isAutocomplete;

    private SelectMinimalViewModel viewModel;
    private SearchView searchView;
    private BinaryDataReceiver listener;

    public SelectMinimalDialog() {
    }

    public SelectMinimalDialog(AbstractSelectListAdapter selectListAdapter, boolean isFlex, boolean isAutoComplete) {
        this.selectListAdapter = selectListAdapter;
        this.isFlex = isFlex;
        this.isAutocomplete = isAutoComplete;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof BinaryDataReceiver) {
            listener = (BinaryDataReceiver) context;
        }
        viewModel = new ViewModelProvider(this, new SelectMinimalViewModel.Factory(selectListAdapter, isFlex, isAutocomplete)).get(SelectMinimalViewModel.class);
        if (viewModel.getSelectListAdapter() == null) {
            dismiss();
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SelectMinimalDialogLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        initToolbar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    protected void onCloseClicked() {
        viewModel.getSelectListAdapter().getFilter().filter("");
        listener.setBinaryData(viewModel.getSelectListAdapter().getSelectedItems());
        dismiss();
    }

    @Override
    protected void onBackPressed() {
        viewModel.getSelectListAdapter().getFilter().filter("");
        listener.setBinaryData(viewModel.getSelectListAdapter().getSelectedItems());
        dismiss();
    }

    @Nullable
    @Override
    protected Toolbar getToolbar() {
        return getView().findViewById(R.id.toolbar);
    }

    private void initToolbar() {
        getToolbar().setNavigationIcon(R.drawable.ic_arrow_back);

        if (viewModel.isAutoComplete()) {
            addSearchBar();
        }
    }

    private void addSearchBar() {
        getToolbar().inflateMenu(R.menu.select_minimal_dialog_menu);

        searchView = (SearchView) getToolbar().getMenu().findItem(R.id.menu_filter).getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.getSelectListAdapter().getFilter().filter(newText);
                return false;
            }
        });
    }

    private void initRecyclerView() {
        binding.choicesRecyclerView.initRecyclerView(viewModel.getSelectListAdapter(), viewModel.isFlex());
    }
}