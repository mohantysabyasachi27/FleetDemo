package com.example.android.fleetdemo.framework;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.android.fleetdemo.DialogWidget;
import com.example.android.fleetdemo.FleetApplication;
import com.example.android.fleetdemo.FrameworkUtils;
import com.example.android.fleetdemo.R;

/**
 * Created by Azuga on 07-03-2018.
 */

public final class UIService {
    private static final String TAG = "UIService";

    public static final int ERROR_TYPE_NONE = -1;
    public static final int ERROR_TYPE_GENERAL = 0;
    public static final int ERROR_TYPE_NETWORK = 1;
    public static final int ERROR_TYPE_NO_VEHICLE = 2;
    public static final int ERROR_TYPE_NO_GPS = 3;

    private static UIService instance = null;
    private DialogWidget progressDialog;


    private UIService() {
    }

    public static boolean isFragmentAnimationDisabled = false;

    public static synchronized UIService getInstance() {
        if (instance == null) {
            instance = new UIService();
        }

        return instance;
    }

    public Activity getActivity() {
        return AppStateListener.getInstance().getActivity();
    }

    public void hideKeyboard() {
        Activity activity = AppStateListener.getInstance().getTopMostActivity();
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                if (activity.getCurrentFocus() != null) {
                    if (activity.getCurrentFocus().getWindowToken() != null) {
                        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
            }
        }
    }

    public void showAlert(int resId) {
        showAlert(R.string.app_name, resId);
    }

    public void showAlert(int titleRestId, int msgResId) {
        showAlert(titleRestId, msgResId, R.string.ok, getGenericClickListener());
    }

    public void showAlert(String msg) {
        showAlert(FleetApplication.getAppContext().getString(R.string.app_name), msg);
    }

    public void showAlert(String title, String msg) {
        showAlert(title, msg, FleetApplication.getAppContext().getString(R.string.ok), getGenericClickListener());
    }

    public void showAlert(int titleRedId, int msgResId, int btnNameRes, DialogInterface.OnClickListener clickListener) {
        showAlert(titleRedId, msgResId, btnNameRes, clickListener, false);
    }

    public void showAlert(int titleRedId, int msgResId, int btnNameRes, DialogInterface.OnClickListener clickListener, boolean isCancelable) {
        showAlert(FleetApplication.getAppContext().getString(titleRedId), FleetApplication.getAppContext().getString(msgResId), FleetApplication.getAppContext().getString(btnNameRes),
                clickListener, isCancelable);
    }

    public void showAlert(String title, String msg, String btnName, DialogInterface.OnClickListener clickListener) {
        showAlert(title, msg, btnName, clickListener, false);
    }

    public void showAlert(final String title, final String msg, final String btnName, final DialogInterface.OnClickListener clickListener, final boolean isCancelable) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                DialogWidget.DialogBuilder builder = new DialogWidget.DialogBuilder(getActivity());
                builder.setTitle(title).setMessageText(msg).setCancelable(isCancelable).setNeutralButton(btnName, clickListener);
                builder.show();
            }
        });
    }

    public void showAlert(int titleResId, int msgResId, int okBtnResId, DialogInterface.OnClickListener okClickListener, int cancelBtnResId,
                          DialogInterface.OnClickListener cancelClickListener) {
        showAlert(titleResId, msgResId, okBtnResId, okClickListener, cancelBtnResId, cancelClickListener, true);
    }

    public void showAlert(String title, String msg, String okBtnName,
                          DialogInterface.OnClickListener okClickListener, String cancelBtnName, DialogInterface.OnClickListener cancelClickListener) {
        showAlert(title, msg, okBtnName, okClickListener, cancelBtnName, cancelClickListener, true);
    }

    public void showAlert(int titleResId, int msgResId, int okBtnResId, DialogInterface.OnClickListener okClickListener,
                          int cancelBtnResId, DialogInterface.OnClickListener cancelClickListener, boolean isCancelable) {
        showAlert(FleetApplication.getAppContext().getString(titleResId),
                FleetApplication.getAppContext().getString(msgResId),
                FleetApplication.getAppContext().getString(okBtnResId),
                okClickListener, FleetApplication.getAppContext().getString(cancelBtnResId), cancelClickListener, isCancelable);
    }

    public void showAlert(final String title, final String msg, final String okBtnName,
                          final DialogInterface.OnClickListener okClickListener, final String cancelBtnName,
                          final DialogInterface.OnClickListener cancelClickListener, final boolean isCancelable) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                DialogWidget.DialogBuilder builder = new DialogWidget.DialogBuilder(getActivity());
                builder.setTitle(title).setMessageText(msg).setCancelable(isCancelable);
                if (cancelBtnName == null) {
                    builder.setNeutralButton(okBtnName, okClickListener);
                } else {
                    builder.setPositiveButton(okBtnName, okClickListener);
                    builder.setNegativeButton(cancelBtnName, cancelClickListener);
                }
                builder.show();
            }
        });
    }

    public void dismissAllAlert() {
        DialogWidget.dismissAllDialog();
        progressDialog = null;
    }

    public boolean isDialogShowing() {
        return (progressDialog != null && progressDialog.isShowing()) || DialogWidget.isAnyDialogVisible();
    }

    private DialogInterface.OnClickListener getGenericClickListener() {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        };
    }

    public void showProgressBar(int msgResId) {
        showProgressBar(FleetApplication.getAppContext().getString(msgResId));
    }

    public void showProgressBar(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.setProgressMessage(message);
                    return;
                }

                DialogWidget.DialogBuilder builder = new DialogWidget.DialogBuilder(getActivity(), true);
                builder.setCancelable(false);
                builder.setCanceledOnTouchOutside(false);
                progressDialog = builder.showProgressDialog(message);
            }
        });
    }

    public void hideProgressBar() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.cancel();
                    progressDialog = null;
                }
            }
        });
    }

    public void runOnUiThread(Runnable runnable) {
        if (AppStateListener.getInstance().getActivity() != null) {
            AppStateListener.getInstance().getActivity().runOnUiThread(runnable);
        }
    }

    public void showInAppError(int resId) {
        showInAppError(FleetApplication.getAppContext().getString(resId));
    }

    public void showInAppError(int resId, int errorType) {
        showInAppError(FleetApplication.getAppContext().getString(resId), errorType);
    }

    public void showInAppError(String message) {
        showInAppError(message, ERROR_TYPE_GENERAL);
    }

    public void showInAppError(String message, int errorType) {
        showInAppError(message, null, null, errorType);
    }

    public void showInAppError(String message, String btnText, android.view.View.OnClickListener clickListener) {
        showInAppError(message, btnText, clickListener, ERROR_TYPE_GENERAL);
    }

    public void showInAppError(final String message, final String btnText,
                               final android.view.View.OnClickListener clickListener, final int errorType) {
        if (FrameworkUtils.isEmptyOrWhitespace(message))
            return;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
                if (uiProtocol != null) {
                    uiProtocol.showInAppError(message, btnText, clickListener, errorType);
                } else {
                    Logger.e(TAG, "showInAppError : UIProtocolCompliantActivity is null");
                }
            }
        });
    }

    public int getInAppErrorType() {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            return uiProtocol.getInAppErrorType();
        } else {
            Logger.e(TAG, "getInAppErrorType : UIProtocolCompliantActivity is null");
        }
        return ERROR_TYPE_NONE;
    }

    public void hideInAppError() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
                if (uiProtocol != null) {
                    uiProtocol.hideInAppError();
                } else {
                    Logger.e(TAG, "hideInAppError : UIProtocolCompliantActivity is null");
                }
            }
        });
    }

    @SuppressWarnings("unused")
    public String getActiveFragmentTag() {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            return uiProtocol.getActiveFragmentTag();
        } else {
            Logger.e(TAG, "getActiveFragmentTag : UIProtocolCompliantActivity is null");
            return null;
        }
    }

    public BaseFragment getActiveFragment() {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            return uiProtocol.getActiveFragment();
        } else {
            Logger.e(TAG, "getActiveFragment : UIProtocolCompliantActivity is null");
            return null;
        }
    }

    @SuppressWarnings("unused")
    public BaseFragment getFragmentFromHistory(String tag) {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            return uiProtocol.getFragmentFromHistory(tag);
        } else {
            Logger.e(TAG, "getFragmentFromHistory : UIProtocolCompliantActivity is null");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public int getBackStackFragmentsCount() {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            return uiProtocol.getBackStackFragmentsCount();
        } else {
            Logger.e(TAG, "getBackStackFragmentsCount : UIProtocolCompliantActivity is null.");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public boolean isFragmentInHistory(String fragmentTag) {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            return uiProtocol.isFragmentInHistory(fragmentTag);
        } else {
            Logger.e(TAG, "isFragmentInHistory : UIProtocolCompliantActivity is null.");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public boolean showFragmentFromHistory(String fragmentTag) {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            return uiProtocol.showFragmentFromHistory(fragmentTag);
        } else {
            Logger.e(TAG, "showFragmentFromHistory : UIProtocolCompliantActivity is null.");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public boolean popFragmentFromBackStack() {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            return uiProtocol.popFragmentFromBackStack();
        } else {
            Logger.e(TAG, "popFragmentFromBackStack : UIProtocolCompliantActivity is null.");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public void clearBackStack() {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            isFragmentAnimationDisabled = true;
            uiProtocol.clearBackStack();
            isFragmentAnimationDisabled = false;
        } else {
            Logger.e(TAG, "clearBackStack : UIProtocolCompliantActivity is null.");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public void addFragment(BaseFragment fragmentToAdd) {
        addFragment(fragmentToAdd, true);
    }

    public void addFragment(BaseFragment fragmentToAdd, boolean addToHistory) {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            uiProtocol.addFragment(fragmentToAdd, addToHistory);
        } else {
            Logger.e(TAG, "addFragment : UIProtocolCompliantActivity is null.");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public void doBack() {
        if (AppStateListener.getInstance().getTopMostActivity() != null)
            AppStateListener.getInstance().getTopMostActivity().onBackPressed();
    }

    public void navigateToHome() {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            uiProtocol.navigateToHome();
        } else {
            Logger.e(TAG, "navigateToHome : UIProtocolCompliantActivity is null");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }



    @SuppressWarnings("unused")
    public void moveAppToBackground() {
        if (AppStateListener.getInstance().getActivity() != null) {
            AppStateListener.getInstance().getActivity().moveTaskToBack(true);
        }
    }

    public boolean refreshFragment(Class<? extends BaseFragment> target) {
        final BaseFragment fragment = getActiveFragment();
        if (fragment != null && fragment.isResumed() && target != null && target.isInstance(fragment)) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (fragment != null)
                        fragment.refreshData();
                }
            });
            return true;
        }

        return false;
    }

    public void setNavBarVisibility(boolean visible) {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            uiProtocol.setNavBarVisibility(visible);
        } else {
            Logger.e(TAG, "setNavBarVisibility : UIProtocolCompliantActivity is null");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public void setupHomeButton() {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            uiProtocol.showLeftNavButton();
        } else {
            Logger.e(TAG, "setupHomeButton : UIProtocolCompliantActivity is null");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public void hideHomeButton() {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            uiProtocol.hideLeftNavButton();
        } else {
            Logger.e(TAG, "hideHomeButton : UIProtocolCompliantActivity is null");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public void hideRightNavigationButton() {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            uiProtocol.hideRightNavButton();
        } else {
            Logger.e(TAG, "hideRightNavigationButton : UIProtocolCompliantActivity is null");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public void setupRightNavButton(View view, android.view.View.OnClickListener clickListener) {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            uiProtocol.showRightNavButton(view, clickListener);
        } else {
            Logger.e(TAG, "setupRightNavButton : UIProtocolCompliantActivity is null");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public void setTitle(String title) {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            uiProtocol.setTitle(title);
        } else {
            Logger.e(TAG, "setTi tle : UIProtocolCompliantActivity is null");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    @SuppressWarnings("unused")
    public void setNavBarColor(int color) {
        UIProtocol uiProtocol = AppStateListener.getInstance().getUIProtocolCompliantActivity();
        if (uiProtocol != null) {
            uiProtocol.setNavBarBgColor(color);
        } else {
            Logger.e(TAG, "setNavBarColor : UIProtocolCompliantActivity is null");
            throw new RuntimeException("UIProtocolCompliantActivity is null.");
        }
    }

    public Activity getRootActivity() {
        Activity root = getActivity();
        while (root != null && !root.isTaskRoot()) {
            root = root.getParent();
        }

        return root;
    }
}
