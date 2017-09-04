/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.AudioPlayListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SelectOneSearchWidget allows the user to enter a value in an editable text box and based on
 * input, the searched
 * options only appear which can then be chosen. This is used to narrow down the Select One options
 * For now, audio/video/image etc will be ignored
 *
 * @author Raghu Mittal (raghu.mittal@handsrel.com)
 */
public class SelectOneSearchWidget extends SelectOneWidget implements OnCheckedChangeListener, AudioPlayListener {

    protected EditText searchStr;

    protected FormEntryPrompt prompt;
    protected Integer selectedTag = -1;

    public SelectOneSearchWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        searchStr = new EditText(context);
        searchStr.setId(QuestionWidget.newUniqueId());
        searchStr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        searchStr.setLayoutParams(params);
        setupChangeListener();
        addAnswerView(searchStr);

        doSearch("");
    }

    public void doSearch(String searchStr) {
        // First check if there is nothing on search
        if (searchStr == null || searchStr.trim().length() == 0) {
            createOptions(items, null);
        } else { // Create a List with items that are relevant to the search text
            List<SelectChoice> searchedItems = new ArrayList<SelectChoice>();
            List<Integer> tagList = new ArrayList<Integer>();
            searchStr = searchStr.toLowerCase(Locale.US);
            for (int i = 0; i < items.size(); i++) {
                String choiceText = prompt.getSelectChoiceText(items.get(i)).toLowerCase(Locale.US);
                if (choiceText.contains(searchStr)) {
                    searchedItems.add(items.get(i));
                    tagList.add(i);
                }

            }
            createOptions(searchedItems, tagList);
        }
    }

    public void createOptions(List<SelectChoice> searchedItems, List<Integer> tagList) {
        removeView(answerLayout);
        buttons = new ArrayList<>();

        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        if (searchedItems != null && searchedItems.size() > 0) {
            for (int i = 0; i < searchedItems.size(); i++) {
                RadioButton r = createRadioButton(i);

                if (tagList == null) {
                    r.setTag(i);
                } else {
                    r.setTag(tagList.get(i));
                }

                buttons.add(r);

                if (selectedTag == -1 && searchedItems.get(i).getValue().equals(s)) {
                    r.setChecked(true);
                    selectedTag = (Integer) r.getTag();
                } else if (selectedTag.equals(r.getTag())) {
                    r.setChecked(true);
                }
                answerLayout.addView(createMediaLayout(i, r));
            }
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, searchStr.getId());
        params.setMargins(10, 0, 10, 0);
        addView(answerLayout, params);
    }

    protected void setupChangeListener() {
        searchStr.addTextChangedListener(new TextWatcher() {
            private String oldText = "";

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(oldText)) {
                    Collect.getInstance().getActivityLogger()
                            .logInstanceAction(this, "searchTextChanged", s.toString(),
                                    getPrompt().getIndex());
                    doSearch(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                oldText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            }
        });
    }

    @Override
    public void setFocus(Context context) {
        searchStr.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(searchStr, 0);
        /*
         * If you do a multi-question screen after a "add another group" dialog, this won't
         * automatically pop up. It's an Android issue.
         *
         * That is, if I have an edit text in an activity, and pop a dialog, and in that
         * dialog's button's OnClick() I call edittext.requestFocus() and
         * showSoftInput(edittext, 0), showSoftinput() returns false. However, if the edittext
         * is focused before the dialog pops up, everything works fine. great.
         */
    }
}