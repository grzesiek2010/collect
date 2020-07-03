package org.odk.collect.android.fragments.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;

public class SelectMinimalViewModel extends ViewModel {

    private AbstractSelectListAdapter selectListAdapter;
    private FormEntryPrompt formEntryPrompt;

    private SelectMinimalViewModel(AbstractSelectListAdapter selectListAdapter, FormEntryPrompt formEntryPrompt) {
        this.selectListAdapter = selectListAdapter;
        this.formEntryPrompt = formEntryPrompt;
    }

    public AbstractSelectListAdapter getSelectListAdapter() {
        return selectListAdapter;
    }

    public FormEntryPrompt getFormEntryPrompt() {
        return formEntryPrompt;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private AbstractSelectListAdapter selectListAdapter;
        private FormEntryPrompt formEntryPrompt;

        public Factory(AbstractSelectListAdapter selectListAdapter, FormEntryPrompt formEntryPrompt) {
            this.selectListAdapter = selectListAdapter;
            this.formEntryPrompt = formEntryPrompt;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SelectMinimalViewModel(selectListAdapter, formEntryPrompt);
        }
    }
}
