package org.odk.collect.android.utilities;

import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.listeners.ThousandsSeparatorTextWatcher;

import java.text.NumberFormat;
import java.util.Locale;

public class TextWidgetUtils {

    public static Integer getIntegerAnswerValueFromIAnswerData(IAnswerData dataHolder) {
        Integer d = null;
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue != null) {
                if (dataValue instanceof Double) {
                    d = ((Double) dataValue).intValue();
                } else {
                    d = (Integer) dataValue;
                }
            }
        }
        return d;
    }

    public static Double getDoubleAnswerValueFromIAnswerData(IAnswerData dataHolder) {
        Double d = null;
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue != null) {
                if (dataValue instanceof Integer) {
                    d = (double) (Integer) dataValue;
                } else {
                    d = (Double) dataValue;
                }
            }
        }
        return d;
    }

    public static IAnswerData getIAnswerDataFromInteger(String answer, boolean useThousandSeparator) {
        if (useThousandSeparator) {
            answer = ThousandsSeparatorTextWatcher.getOriginalString(answer);
        }

        if (answer.isEmpty()) {
            return null;
        } else {
            try {
                return new IntegerData(Integer.parseInt(answer));
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

    public static IAnswerData getIAnswerDataFromDecimal(String answer, boolean useThousandSeparator) {
        if (useThousandSeparator) {
            answer = ThousandsSeparatorTextWatcher.getOriginalString(answer);
        }

        if (answer.isEmpty()) {
            return null;

        } else {
            try {
                return new DecimalData(Double.parseDouble(answer));
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

    public static IAnswerData getIAnswerDataFromStringNumber(String answer, boolean useThousandSeparator) {
        if (useThousandSeparator) {
            answer = ThousandsSeparatorTextWatcher.getOriginalString(answer);
        }

        if (answer.isEmpty()) {
            return null;
        } else {
            try {
                return new StringData(answer);
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

    public static void adjustEditTextAnswerToIntegerWidget(EditText answerText, boolean useThousandSeparator, IAnswerData answerData) {
        if (useThousandSeparator) {
            answerText.addTextChangedListener(new ThousandsSeparatorTextWatcher(answerText));
        }
        answerText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
        // only allows numbers and no periods
        answerText.setKeyListener(new DigitsKeyListener(true, false));
        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        if (useThousandSeparator) {
            //11 since for a nine digit number , their will be 2 separators.
            fa[0] = new InputFilter.LengthFilter(11);
        }
        answerText.setFilters(fa);

        Integer i = TextWidgetUtils.getIntegerAnswerValueFromIAnswerData(answerData);

        if (i != null) {
            answerText.setText(String.format(Locale.US, "%d", i));
            Selection.setSelection(answerText.getText(), answerText.getText().toString().length());
        }
    }

    public static void adjustEditTextAnswerToDecimalWidget(EditText answerText, boolean useThousandSeparator, IAnswerData answerData) {
        if (useThousandSeparator) {
            answerText.addTextChangedListener(new ThousandsSeparatorTextWatcher(answerText));
        }

        answerText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        // only numbers are allowed
        answerText.setKeyListener(new DigitsKeyListener(true, true));

        // only 15 characters allowed
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(15);
        if (useThousandSeparator) {
            fa[0] = new InputFilter.LengthFilter(19);
        }
        answerText.setFilters(fa);

        Double d = TextWidgetUtils.getDoubleAnswerValueFromIAnswerData(answerData);

        if (d != null) {
            // truncate to 15 digits max in US locale
            // use US locale because DigitsKeyListener can't be localized before API 26
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMaximumFractionDigits(15);
            nf.setMaximumIntegerDigits(15);
            nf.setGroupingUsed(false);

            String formattedValue = nf.format(d);
            answerText.setText(formattedValue);

            Selection.setSelection(answerText.getText(), answerText.getText().length());
        }
    }
}
