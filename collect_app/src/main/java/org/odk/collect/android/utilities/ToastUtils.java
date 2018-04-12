package org.odk.collect.android.utilities;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.collect.android.application.Collect;

public class ToastUtils {

    private ToastUtils() {

    }

    public static void showShortToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    public static void showShortToast(int messageResource) {
        showToast(messageResource, Toast.LENGTH_SHORT);
    }

    public static void showLongToast(String message) {
        showToast(message, Toast.LENGTH_LONG);
    }

    public static void showLongToast(int messageResource) {
        showToast(messageResource, Toast.LENGTH_LONG);
    }

    private static void showToast(String message, int duration) {
        Toast.makeText(Collect.getInstance(), message, duration).show();
    }

    private static void showToast(int messageResource, int duration) {
        Toast.makeText(Collect.getInstance(), Collect.getInstance().getString(messageResource), duration).show();
    }

    @SuppressLint("ShowToast")
    public static void showShortToastInMiddle(int messageResource) {
        showToastInMiddle(Toast.makeText(Collect.getInstance(), Collect.getInstance().getString(messageResource), Toast.LENGTH_SHORT));
    }

    @SuppressLint("ShowToast")
    public static void showShortToastInMiddle(String message) {
        showToastInMiddle(Toast.makeText(Collect.getInstance(), message, Toast.LENGTH_SHORT));
    }

    @SuppressLint("ShowToast")
    public static void showLongToastInMiddle(int messageResource) {
        showToastInMiddle(Toast.makeText(Collect.getInstance(), Collect.getInstance().getString(messageResource), Toast.LENGTH_LONG));
    }

    private static void showToastInMiddle(Toast toast) {
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(21);

        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
