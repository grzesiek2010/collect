/*
 * Copyright (C) 2013 Nafundi
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.BearingActivity;
import org.odk.collect.android.databinding.BearingWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.interfaces.BinaryDataReceiver;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * BearingWidget is the widget that allows the user to get a compass heading.
 */
@SuppressLint("ViewConstructor")
public class BearingWidget extends QuestionWidget implements BinaryDataReceiver {
    BearingWidgetAnswerBinding binding;

    private final boolean isSensorAvailable;
    private final Drawable textBackground;
    private final WaitingForDataRegistry waitingForDataRegistry;
    private final SensorManager sensorManager;

    public BearingWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry, SensorManager sensorManager) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.sensorManager = sensorManager;

        isSensorAvailable = checkForRequiredSensors();
        textBackground = binding.answerText.getBackground();
        binding.answerText.setBackground(null);

        String answerText = questionDetails.getPrompt().getAnswerText();
        if (answerText != null && !answerText.equals("")) {
            binding.bearingButton.setText(getContext().getString(R.string.replace_bearing));
            binding.answerText.setText(answerText);
        }
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = BearingWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        if (prompt.isReadOnly()) {
            binding.bearingButton.setVisibility(GONE);
        } else {
            binding.bearingButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.bearingButton.setOnClickListener(v -> onButtonClick());
        }
        binding.answerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        binding.answerText.setText(null);
        binding.bearingButton.setText(getContext().getString(R.string.get_bearing));
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        String answerText = binding.answerText.getText().toString();
        return answerText.isEmpty() ? null : new StringData(answerText);
    }

    @Override
    public void setBinaryData(Object answer) {
        binding.answerText.setText((String) answer);
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        if (isSensorAvailable) {
            binding.bearingButton.setOnLongClickListener(l);
        }
        binding.answerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        if (isSensorAvailable) {
            binding.bearingButton.cancelLongPress();
        }
        binding.answerText.cancelLongPress();
    }

    private boolean checkForRequiredSensors() {
        boolean isAccelerometerSensorAvailable = false;
        boolean isMagneticFieldSensorAvailable = false;

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            isAccelerometerSensorAvailable = true;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            isMagneticFieldSensorAvailable = true;
        }
        return isAccelerometerSensorAvailable && isMagneticFieldSensorAvailable;
    }

    private void onButtonClick() {
        if (isSensorAvailable) {
            Intent intent = new Intent(getContext(), BearingActivity.class);
            waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
            ((Activity) getContext()).startActivityForResult(intent, RequestCodes.BEARING_CAPTURE);
        } else {
            binding.bearingButton.setEnabled(false);
            ToastUtils.showLongToast(R.string.bearing_lack_of_sensors);
            binding.answerText.setBackground(textBackground);
            binding.answerText.setFocusable(true);
            binding.answerText.setFocusableInTouchMode(true);
            binding.answerText.requestFocus();
        }
    }
}
