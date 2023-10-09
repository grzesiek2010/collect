package org.odk.collect.android.formentry.questions;

import static android.view.View.GONE;
import static org.odk.collect.android.utilities.ViewUtils.dpFromPx;
import static org.odk.collect.android.utilities.ViewUtils.pxFromDp;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.google.android.material.button.MaterialButton;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.interfaces.ButtonClickListener;
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard;

public final class WidgetViewUtils {

    private static final int WIDGET_ANSWER_STANDARD_MARGIN_MODIFIER = 4;

    private WidgetViewUtils() {

    }

    public static int getStandardMargin(Context context) {
        Resources resources = context.getResources();
        int marginStandard = dpFromPx(context, resources.getDimensionPixelSize(org.odk.collect.androidshared.R.dimen.margin_standard));

        return marginStandard - WIDGET_ANSWER_STANDARD_MARGIN_MODIFIER;
    }

    public static TextView getCenteredAnswerTextView(Context context, int answerFontSize) {
        TextView textView = createAnswerTextView(context, answerFontSize);
        textView.setGravity(Gravity.CENTER);

        return textView;
    }

    public static TextView createAnswerTextView(Context context, int answerFontSize) {
        return createAnswerTextView(context, "", answerFontSize);
    }

    public static TextView createAnswerTextView(Context context, CharSequence text, int answerFontSize) {
        TextView textView = new TextView(context);

        textView.setId(R.id.answer_text);
        textView.setTextColor(new ThemeUtils(context).getColorOnSurface());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        textView.setPadding(20, 20, 20, 20);
        textView.setText(text);

        return textView;
    }

    public static ImageView createAnswerImageView(Context context) {
        final ImageView imageView = new ImageView(context);
        imageView.setId(View.generateViewId());
        imageView.setTag("ImageView");
        imageView.setPadding(10, 10, 10, 10);
        imageView.setAdjustViewBounds(true);
        return imageView;
    }

    public static Button createSimpleButton(Context context, @IdRes final int withId, boolean readOnly, String text, ButtonClickListener listener) {
        final MaterialButton button = (MaterialButton) LayoutInflater
                .from(context)
                .inflate(R.layout.widget_answer_button, null, false);

        if (readOnly) {
            button.setVisibility(GONE);
        } else {
            button.setId(withId);
            button.setText(text);
            button.setContentDescription(text);

            TableLayout.LayoutParams params = new TableLayout.LayoutParams();

            float marginExtraSmall = context.getResources().getDimension(org.odk.collect.androidshared.R.dimen.margin_extra_small);
            int topMargin = pxFromDp(context, marginExtraSmall);
            params.setMargins(7, topMargin, 7, 5);

            button.setLayoutParams(params);

            button.setOnClickListener(v -> {
                if (MultiClickGuard.allowClick(QuestionWidget.class.getName())) {
                    listener.onButtonClick(withId);
                }
            });
        }

        return button;
    }

    public static Button createSimpleButton(Context context, @IdRes int id, boolean readOnly, ButtonClickListener listener) {
        return createSimpleButton(context, id, readOnly, null, listener);
    }

    public static Button createSimpleButton(Context context, boolean readOnly, String text, ButtonClickListener listener) {
        return createSimpleButton(context, R.id.simple_button, readOnly, text, listener);
    }
}
