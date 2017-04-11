/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.StableArrayAdapter;
import org.odk.collect.android.views.RankListView;

import java.util.ArrayList;

public class RankWidget extends QuestionWidget {

    private RankListView mRankList;

    public RankWidget(Context context, FormEntryPrompt p) {
        super(context, p);

        LinearLayout rankHolder = new LinearLayout(context);
        rankHolder.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout positionHolder = new LinearLayout(context);
        positionHolder.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams positionHolderParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        positionHolderParams.setMargins(0, 0, 5, 0);
        positionHolder.setLayoutParams(positionHolderParams);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        ArrayList<String> rankItems = new ArrayList<>();
        for (SelectChoice selectChoice : p.getSelectChoices()) {
            rankItems.add(selectChoice.getValue());

            TextView positionText = (TextView) inflater.inflate(R.layout.rank_element, null);
            positionText.setText(String.valueOf(selectChoice.getIndex() + 1));

            LinearLayout.LayoutParams positionTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            positionTextParams.setMargins(0, 0, 0, 5);
            positionHolder.addView(positionText, positionTextParams);
        }

        prepareRankListView(rankItems);

        rankHolder.addView(positionHolder);
        rankHolder.addView(mRankList);
        addAnswerView(rankHolder);

        setListViewHeightBasedOnChildren(mRankList);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int totalHeight = 0;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
            listView.requestLayout();
        }
    }

    private void prepareRankListView(ArrayList<String> rankItems) {
        StableArrayAdapter adapter = new StableArrayAdapter(getContext(), R.layout.rank_element, rankItems);
        mRankList = new RankListView(getContext());
        mRankList.setCheeseList(rankItems);
        mRankList.setAdapter(adapter);
        mRankList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mRankList.setDivider(new ColorDrawable(Color.TRANSPARENT));
        mRankList.setDividerHeight(5);
        mRankList.setNestedScrollingEnabled(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.weight=1.0f;

        mRankList.setLayoutParams(params);
    }

    @Override
    public IAnswerData getAnswer() {
        return null;
    }

    @Override
    public void clearAnswer() {

    }

    @Override
    public void setFocus(Context context) {

    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {

    }
}
