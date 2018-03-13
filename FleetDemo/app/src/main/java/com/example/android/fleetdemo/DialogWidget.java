package com.example.android.fleetdemo;

/**
 * Created by Azuga on 27-02-2018.
 */

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class DialogWidget {

    private static HashMap<Long, DialogWidget> dialogMapping = new HashMap<>();

    private long id;
    private Dialog actualDialog;

    private boolean isProgressDialog = false;

    private String title;
    private String positiveText;
    private String neutralText;
    private String negativeText;
    private View contentView;
    private ViewGroup.LayoutParams contentViewParams;
    private String message;
    private int textGravity = Gravity.CENTER;

    private TextView progressMessageView;

    private DialogInterface.OnClickListener positiveListener;
    private DialogInterface.OnClickListener neutralListener;
    private DialogInterface.OnClickListener negativeListener;
    private DialogInterface.OnCancelListener onCancelListener;
    private DialogInterface.OnDismissListener onDismissListener;
    private DialogInterface.OnShowListener onShowListener;

    public static class DialogBuilder {
        private DialogWidget dialogWidget;

        public DialogBuilder(Context context) {
            this(context, false);
        }

        public DialogBuilder(Context context, boolean isProgressDialog) {
            dialogWidget = new DialogWidget(context, System.currentTimeMillis());
            dialogWidget.isProgressDialog = isProgressDialog;
        }

        public DialogBuilder setTitle(int resId) {
            setTitle(FleetApplication.getAppContext().getString(resId));
            return this;
        }

        public DialogBuilder setTitle(String title) {
            dialogWidget.title = title;
            return this;
        }

        public DialogBuilder setMessageText(int resId) {
            return setMessageText(FleetApplication.getAppContext().getString(resId));
        }

        public DialogBuilder setMessageText(String message) {
            dialogWidget.message = message;
            return this;
        }

        public DialogBuilder setMessageGravity(int gravity) {
            dialogWidget.textGravity = gravity;
            return this;
        }

        public DialogBuilder setView(View view) {
            dialogWidget.contentView = view;
            dialogWidget.contentViewParams = null;
            return this;
        }

        @SuppressWarnings("unused")
        public DialogBuilder setView(View view, ViewGroup.LayoutParams viewParams) {
            dialogWidget.contentView = view;
            dialogWidget.contentViewParams = viewParams;
            return this;
        }

        public DialogBuilder setPositiveButton(int resId,
                                               DialogInterface.OnClickListener listener) {
            return setPositiveButton(FleetApplication.getAppContext().getString(resId), listener);
        }

        public DialogBuilder setPositiveButton(String text,
                                               DialogInterface.OnClickListener listener) {
            dialogWidget.positiveText = text;
            dialogWidget.positiveListener = listener;
            return this;
        }

        @SuppressWarnings("unused")
        public DialogBuilder setNeutralButton(int resId, DialogInterface.OnClickListener listener) {
            return setNeutralButton(FleetApplication.getAppContext().getString(resId), listener);
        }

        public DialogBuilder setNeutralButton(String text,
                                              DialogInterface.OnClickListener listener) {
            dialogWidget.neutralText = text;
            dialogWidget.neutralListener = listener;
            return this;
        }

        public DialogBuilder setNegativeButton(int resId,
                                               DialogInterface.OnClickListener listener) {
            return setNegativeButton(FleetApplication.getAppContext().getString(resId), listener);
        }

        public DialogBuilder setNegativeButton(String text,
                                               DialogInterface.OnClickListener listener) {
            dialogWidget.negativeText = text;
            dialogWidget.negativeListener = listener;
            return this;
        }

        @SuppressWarnings("unused")
        public DialogBuilder setOnShowListener(DialogInterface.OnShowListener showListener) {
            dialogWidget.onShowListener = showListener;
            return this;
        }

        public DialogBuilder setOnDismissListener(
                DialogInterface.OnDismissListener dismissListener) {
            dialogWidget.onDismissListener = dismissListener;
            return this;
        }

        public DialogBuilder setOnCancelListener(DialogInterface.OnCancelListener cancelListener) {
            dialogWidget.onCancelListener = cancelListener;
            return this;
        }

        public DialogBuilder setCancelable(boolean isCancelable) {
            dialogWidget.actualDialog.setCancelable(isCancelable);
            return this;
        }

        public DialogBuilder setCanceledOnTouchOutside(boolean isCancelable) {
            dialogWidget.actualDialog.setCanceledOnTouchOutside(isCancelable);
            return this;
        }

        @SuppressWarnings("unused")
        public DialogBuilder setOnKeyListener(DialogInterface.OnKeyListener listener) {
            dialogWidget.actualDialog.setOnKeyListener(listener);
            return this;
        }

        public DialogWidget show() {
            dialogWidget.show();
            return dialogWidget;
        }

        public DialogWidget showProgressDialog(String msg) {
            dialogWidget.showProgressDialog(msg);
            return dialogWidget;
        }

        @SuppressWarnings("unused")
        public DialogWidget getDialogWidget() {
            return dialogWidget;
        }
    }

    public DialogWidget(Context context, long id) {
        actualDialog = new MyDialog(context);
        this.id = id;
    }

    public void show() {
        actualDialog.show();
    }

    public void showProgressDialog(String msg) {
        if (isProgressDialog) {
            //Setup the progress dialog here
            actualDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0);
                }
            });

            contentView = LayoutInflater.from(actualDialog.getContext())
                    .inflate(R.layout.popup_progress, null);
            ImageView loadingImage = (ImageView) contentView
                    .findViewById(R.id.popup_progress_indicator).findViewById(
                            R.id.progress_indicator_arrow);
            progressMessageView = (TextView) contentView.findViewById(R.id.popup_progress_message);
            progressMessageView.setText(msg);

            Animation animation = AnimationUtils.loadAnimation(actualDialog.getContext(),
                    R.anim.popup_progress_rotate_animation);
            loadingImage.startAnimation(animation);

            actualDialog.show();
        } else {
            throw new RuntimeException("Dialog is not a Progress Dialog. Please check your code.");
        }
    }

    public void setProgressMessage(String message) {
        if (isProgressDialog && progressMessageView != null) {
            progressMessageView.setText(message);
        }
    }

    public long getId() {
        return id;
    }

    public boolean isShowing() {
        return actualDialog != null && actualDialog.isShowing();
    }

    public void dismiss() {
        if (isShowing()) {
            actualDialog.dismiss();
            actualDialog = null;
        }
    }

    public void cancel() {
        if (isShowing()) {
            actualDialog.cancel();
            actualDialog = null;
        }
    }

    public static void dismissAllDialog() {
        Iterator<Map.Entry<Long, DialogWidget>> iterator = dialogMapping.entrySet().iterator();
        while (iterator.hasNext()) {
            DialogWidget dialogWidget = iterator.next().getValue();
            //First remove the item here. This will avoid the IllegalState Exception.
            iterator.remove();
            if (dialogWidget != null && dialogWidget.isShowing()) {
                dialogWidget.dismiss();
            }
        }
    }

    public static boolean isAnyDialogVisible() {
        Iterator<Map.Entry<Long, DialogWidget>> iterator = dialogMapping.entrySet().iterator();
        while (iterator.hasNext()) {
            DialogWidget dialogWidget = iterator.next().getValue();
            //First remove the item here. This will avoide the IllegalState Exception.
            if (dialogWidget != null && dialogWidget.isShowing()) {
                return true;
            }
        }

        return false;
    }

    private void addDialogToQueue() {
        dialogMapping.put(id, this);
    }

    private void removeDialogFromQueue() {
        if (dialogMapping.containsKey(id))
            dialogMapping.remove(id);
    }

    private class MyDialog extends Dialog {

        public MyDialog(Context context) {
            super(context);
        }

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
            setContentView(R.layout.popup_base);

            TextView titleView = (TextView) findViewById(R.id.info_header);
            TextView messageTextView = (TextView) findViewById(R.id.popup_content_text);

            if (title != null) {
                titleView.setText(Html.fromHtml(title));
                titleView.setVisibility(View.VISIBLE);
            } else {
                titleView.setVisibility(View.GONE);
            }

            ScrollView scrollView = (ScrollView) findViewById(R.id.popup_scroll_view);
            LinearLayout popupContentView = (LinearLayout) findViewById(R.id.popup_content_view);
            LinearLayout buttonBar = (LinearLayout) findViewById(R.id.popup_button_bar);

            Button positiveButton = (Button) findViewById(R.id.btn_positive);
            Button neutralButton = (Button) findViewById(R.id.btn_neutral);
            Button negativeButton = (Button) findViewById(R.id.btn_negative);

            positiveButton.setTypeface(
                    FontManager.getTypeface(getContext(), FontManager.TypeFaceEnum.PROXIMANOVA_SEMI_BOLD));
            neutralButton.setTypeface(
                    FontManager.getTypeface(getContext(), FontManager.TypeFaceEnum.PROXIMANOVA_SEMI_BOLD));
            negativeButton.setTypeface(
                    FontManager.getTypeface(getContext(), FontManager.TypeFaceEnum.PROXIMANOVA_SEMI_BOLD));

            if (contentView != null) {
                if (contentView instanceof ListView ||
                        contentView instanceof GridView) {
                    if (contentViewParams != null) {
                        popupContentView.addView(contentView, contentViewParams);
                    } else {
                        popupContentView.addView(contentView);
                    }

                    messageTextView.setVisibility(View.GONE);
                    scrollView.setVisibility(View.GONE);
                    popupContentView.setVisibility(View.VISIBLE);
                } else {
                    if (contentViewParams != null) {
                        scrollView.addView(contentView, contentViewParams);
                    } else {
                        scrollView.addView(contentView);
                    }

                    messageTextView.setVisibility(View.GONE);
                    popupContentView.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                }
            } else if (message != null) {
                scrollView.setVisibility(View.GONE);
                popupContentView.setVisibility(View.GONE);
                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setGravity(textGravity);
                messageTextView.setText(Html.fromHtml(message));
            }

            //Dismiss listeners.
            setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    removeDialogFromQueue();
                    if (onDismissListener != null) {
                        onDismissListener.onDismiss(dialog);
                    }
                }
            });

            setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeDialogFromQueue();
                    if (onCancelListener != null) {
                        onCancelListener.onCancel(dialog);
                    }
                }
            });

            setOnShowListener(new OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    addDialogToQueue();
                    if (onShowListener != null) {
                        onShowListener.onShow(dialog);
                    }
                }
            });

            boolean isPositiveButtonEnabled = !FrameworkUtils.isEmptyOrWhitespace(positiveText);
            boolean isNegativeButtonEnabled = !FrameworkUtils.isEmptyOrWhitespace(negativeText);
            boolean isNeutralButtonEnabled = !FrameworkUtils.isEmptyOrWhitespace(neutralText);

            if (!isPositiveButtonEnabled && !isNegativeButtonEnabled && !isNeutralButtonEnabled) {
                buttonBar.setVisibility(View.GONE);
                return;
            } else {
                buttonBar.setVisibility(View.VISIBLE);
            }

            if (isPositiveButtonEnabled) {
                positiveButton.setText(positiveText);
                positiveButton.setOnClickListener(
                        getButtonClickListener(positiveListener, BUTTON_POSITIVE));

                if (!isNegativeButtonEnabled && !isNeutralButtonEnabled) {
                    setButtonState(positiveButton, BUTTON_TYPE.POSTIVE, CORNER.BOTTOM_BOTH);
                } else {
                    setButtonState(positiveButton, BUTTON_TYPE.POSTIVE, CORNER.BOTTOM_RIGHT);
                }
            } else {
                positiveButton.setVisibility(View.GONE);
            }

            if (neutralText != null) {
                neutralButton.setText(neutralText);
                neutralButton.setOnClickListener(
                        getButtonClickListener(neutralListener, BUTTON_NEUTRAL));

                if (isPositiveButtonEnabled && isNegativeButtonEnabled) {
                    setButtonState(neutralButton, BUTTON_TYPE.NEUTRAL, CORNER.NONE);
                } else if (!isPositiveButtonEnabled && !isNegativeButtonEnabled) {
                    setButtonState(neutralButton, BUTTON_TYPE.POSTIVE, CORNER.BOTTOM_BOTH);
                } else if (isNegativeButtonEnabled) {
                    setButtonState(neutralButton, BUTTON_TYPE.POSTIVE, CORNER.BOTTOM_RIGHT);
                } else {
                    setButtonState(neutralButton, BUTTON_TYPE.NEGATIVE, CORNER.BOTTOM_LEFT);
                }
            } else {
                neutralButton.setVisibility(View.GONE);
            }

            if (negativeText != null) {
                negativeButton.setText(negativeText);
                negativeButton.setOnClickListener(
                        getButtonClickListener(negativeListener, BUTTON_NEGATIVE));

                if (!isPositiveButtonEnabled && !isNeutralButtonEnabled) {
                    setButtonState(negativeButton, BUTTON_TYPE.POSTIVE, CORNER.BOTTOM_BOTH);
                } else {
                    setButtonState(negativeButton, BUTTON_TYPE.NEGATIVE, CORNER.BOTTOM_LEFT);
                }
            } else {
                negativeButton.setVisibility(View.GONE);
            }
        }

        private android.view.View.OnClickListener getButtonClickListener(
                final OnClickListener listener, final int which) {
            if (listener == null) {
                return new android.view.View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        cancel();
                    }
                };
            } else {
                return new android.view.View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        listener.onClick(MyDialog.this, which);
                    }
                };
            }
        }

        @SuppressLint("NewApi")
        @SuppressWarnings("deprecation")
        public void setButtonState(Button button, BUTTON_TYPE buttonType, CORNER corner) {
            if (button == null)
                return;

            float[] radiusVals = null;

            float cornerRadius = getContext().getResources()
                    .getDimension(R.dimen.popup_corner_radius);

            switch (corner) {
                case ALL:
                    radiusVals = new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                            cornerRadius, cornerRadius,
                            cornerRadius, cornerRadius};
                    break;
                case TOP_BOTH:
                    radiusVals = new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                            0, 0, 0, 0};
                    break;
                case BOTTOM_BOTH:
                    radiusVals = new float[]{0, 0, 0, 0, cornerRadius, cornerRadius, cornerRadius,
                            cornerRadius};
                    break;
                case LEFT_BOTH:
                    radiusVals = new float[]{cornerRadius, cornerRadius, 0, 0, 0, 0, cornerRadius,
                            cornerRadius};
                    break;
                case RIGHT_BOTH:
                    radiusVals = new float[]{0, 0, cornerRadius, cornerRadius, cornerRadius,
                            cornerRadius, 0, 0};
                    break;
                case TOP_LEFT:
                    radiusVals = new float[]{cornerRadius, cornerRadius, 0, 0, 0, 0, 0, 0, 0, 0};
                    break;
                case TOP_RIGHT:
                    radiusVals = new float[]{0, 0, cornerRadius, cornerRadius, 0, 0, 0, 0};
                    break;
                case BOTTOM_RIGHT:
                    radiusVals = new float[]{0, 0, 0, 0, cornerRadius, cornerRadius, 0, 0};
                    break;
                case BOTTOM_LEFT:
                    radiusVals = new float[]{0, 0, 0, 0, 0, 0, cornerRadius, cornerRadius};
                    break;
                default:
                    break;
            }

            StateListDrawable stateDrawable = new StateListDrawable();
            switch (buttonType) {
                case POSTIVE:
                    stateDrawable.addState(
                            new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_pressed)));
                    stateDrawable.addState(
                            new int[]{android.R.attr.state_focused, android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_pressed)));
                    stateDrawable.addState(new int[]{android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_enabled)));
                    stateDrawable.addState(new int[]{-android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_disabled)));
                    break;
                case NEGATIVE:
                    stateDrawable.addState(
                            new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_pressed)));
                    stateDrawable.addState(
                            new int[]{android.R.attr.state_focused, android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_pressed)));
                    stateDrawable.addState(new int[]{android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_enabled)));
                    stateDrawable.addState(new int[]{-android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_disabled)));
                    break;
                case NEUTRAL:
                    stateDrawable.addState(
                            new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_pressed)));
                    stateDrawable.addState(
                            new int[]{android.R.attr.state_focused, android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_pressed)));
                    stateDrawable.addState(new int[]{android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_enabled)));
                    stateDrawable.addState(new int[]{-android.R.attr.state_enabled},
                            getButton(radiusVals, getContext().getResources()
                                    .getColor(R.color.btn_color_disabled)));
                    break;
            }

            if (stateDrawable != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    button.setBackground(stateDrawable);
                } else {
                    button.setBackgroundDrawable(stateDrawable);
                }
            }
        }

        @SuppressLint("NewApi")
        private Drawable getButton(float[] radius, int color) {
            RectShape rectShape;
            if (radius == null || radius.length < 8) {
                rectShape = new RectShape();
            } else {
                rectShape = new RoundRectShape(radius, null, null);
            }

            ShapeDrawable drawable = new ShapeDrawable(rectShape);
            drawable.getPaint().setColor(color);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int[][] states = new int[][]{new int[]{android.R.attr.state_pressed}};

                int[] colors = new int[]{
                        getContext().getResources().getColor(R.color.btn_color_ripple)};

                return new RippleDrawable(new ColorStateList(states, colors), drawable, null);
            }
            return drawable;
        }
    }

    private enum CORNER {
        ALL,
        TOP_BOTH,
        BOTTOM_BOTH,
        LEFT_BOTH,
        RIGHT_BOTH,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT,
        NONE
    }

    private enum BUTTON_TYPE {
        POSTIVE,
        NEGATIVE,
        NEUTRAL
    }
}
