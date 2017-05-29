package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;

public class RangeWidget extends QuestionWidget {

    public RangeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        RangeQuestion rangeQuestion = (RangeQuestion) prompt.getQuestion();

        int rangeStart = rangeQuestion.getRangeStart().intValue();
        int rangeEnd = rangeQuestion.getRangeEnd().intValue();
        double rangeStep = rangeQuestion.getRangeStep().doubleValue();

        View child = ((Activity) context).getLayoutInflater().inflate(R.layout.seek_bar_layout, null);
        TextView minValue = (TextView) child.findViewById(R.id.minValue);
        minValue.setText(String.valueOf(rangeStart));


        TextView maxValue = (TextView) child.findViewById(R.id.maxValue);
        maxValue.setText(String.valueOf(rangeEnd));

        int max = (int) ((rangeEnd - rangeStart) / rangeStep);

        SeekBar seekBar = (SeekBar) child.findViewById(R.id.seekBar);
        seekBar.setMax(max);

        addAnswerView(child);
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
