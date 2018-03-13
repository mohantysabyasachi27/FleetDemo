package com.example.android.fleetdemo.framework;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.example.android.fleetdemo.BuildConfig;
import com.example.android.fleetdemo.FleetApplication;
import com.example.android.fleetdemo.FrameworkUtils;
import com.example.android.fleetdemo.MainActivity;
import com.example.android.fleetdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Azuga on 07-03-2018.
 */

public class PermissionHelper {
    private static final String TAG = "PermissionHelper";
    private static final String PERMISSION_ACTION_KEY = "smartfleet.request.permission";

    public static final int REQUEST_PERMISSION_ACTION = 10001;
    public static final int REQUEST_OVERLAY_PERMISSION = 10002;
    public static final int REQUEST_GPS_PERMISSION = 10003;

    private static String[] tempArr = new String[0];
    public static final String PERMISSION_TYPE_CODE = "smartfleet.permissionRequestType";
    public static final String PERMISSION_REQUEST_CODE = "smartfleet.permissionRequestCode";
    public static final String PERMISSION_ARRAY = "smartfleet.permissionsArray";
    public static final String PERMISSION_MESSAGE_ID = "smartfleet.messageId";

    private static String[] allPermissions = null;

    private static Intent driveSafePermissionIntent = null;
    private static int usageSettingSupportedState = -1;

    public static boolean hasPermission(ArrayList<String> permissions) {
        return (permissions == null
                || permissions.size() == 0
                || hasPermission(permissions.toArray(tempArr)));
    }

    public static boolean hasPermission(String... permissions) {
        if (permissions == null || permissions.length == 0)
            return true;

        boolean allGranted = true;
        for (String permission : permissions) {
            if (FrameworkUtils.isEmptyOrWhitespace(permission)) {
                continue;
            }

            if (ContextCompat.checkSelfPermission(FleetApplication.getAppContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        return allGranted;
    }

    public static boolean requestAllPermissions(Fragment fragment, int requestCode) {
        return requestAllPermissions(fragment, requestCode, true);
    }

    public static boolean requestAllPermissions(final Fragment fragment, int requestCode, boolean checkForBlockedPermission) {
        String[] deniedPermissions = getDeniedPermissions(getAllPermissions());
        if (deniedPermissions == null || deniedPermissions.length == 0) {
            return true;
        }

        if (fragment == null || !AppStateListener.isAppVisible()) {
            showPermissionNotification(REQUEST_PERMISSION_ACTION, requestCode,
                    deniedPermissions, R.string.permission_ds_notification_msg);
            return false;
        }

        //Now check if permission is blocked or not
        if (checkForBlockedPermission) {
            boolean permissionBlocked = false;
            for (String permission : deniedPermissions) {
                if (!fragment.shouldShowRequestPermissionRationale(permission)) {
                    permissionBlocked = true;
                    break;
                }
            }

            if (permissionBlocked) {
                onPermissionBlocked(R.string.permission_screen_denied_desc, false, false);
            } else {
                fragment.requestPermissions(deniedPermissions, requestCode);
            }
        } else {
            fragment.requestPermissions(deniedPermissions, requestCode);
        }

        return false;
    }

    @SuppressWarnings("unused")
    public static boolean requestPermission(@NonNull final Fragment fragment, final int requestCode,
                                            @StringRes final int explanationMessageId,
                                            @StringRes int blockedMsgId,
                                            @NonNull String... permissions) {
        final String[] deniedPermissions = getDeniedPermissions(permissions);
        if (deniedPermissions == null || deniedPermissions.length == 0) {
            return true;
        }

        boolean needExplanation = false;
        for (String permission : deniedPermissions) {
            if (fragment.shouldShowRequestPermissionRationale(permission)) {
                needExplanation = true;
                break;
            }
        }

        if (AppStateListener.isAppVisible()) {
            if (needExplanation) {
                UIService.getInstance().showAlert(R.string.app_name, explanationMessageId,
                        R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                fragment.requestPermissions(deniedPermissions, requestCode);
                            }
                        }, true);
            } else {
                onPermissionBlocked(blockedMsgId, false, false);
            }
        } else {
            showPermissionNotification(REQUEST_PERMISSION_ACTION, requestCode, deniedPermissions,
                    explanationMessageId);
        }

        return false;
    }

    public static boolean requestPermission(@NonNull final Activity activity, final int requestCode,
                                            @StringRes final int messageId,
                                            @StringRes int blockedMsgId,
                                            @NonNull String... permissions) {
        if (!AppStateListener.isAppVisible()) {
            return false;
        }

        final String[] deniedPermissions = getDeniedPermissions(permissions);
        if (deniedPermissions == null || deniedPermissions.length == 0) {
            return true;
        }

        boolean needExplanation = false;
        for (String permission : deniedPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                needExplanation = true;
                break;
            }
        }

        if (AppStateListener.isAppVisible()) {
            if (needExplanation) {
                UIService.getInstance().showAlert(R.string.app_name, messageId, R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                ActivityCompat.requestPermissions(activity, deniedPermissions,
                                        requestCode);
                            }
                        }, true);
            } else {
                onPermissionBlocked(blockedMsgId, false, false);
            }
        } else {
            showPermissionNotification(REQUEST_PERMISSION_ACTION, requestCode, deniedPermissions,
                    messageId);
        }

        return false;
    }

    @SuppressWarnings("unused")
    public static boolean requestPermission(final int requestCode, @StringRes final int messageId,
                                            @NonNull String... permissions) {

        String[] deniedPermissions = getDeniedPermissions(permissions);
        if (deniedPermissions == null || deniedPermissions.length == 0) {
            return true;
        }

        showPermissionNotification(REQUEST_PERMISSION_ACTION, requestCode, deniedPermissions,
                messageId);
        return false;
    }

    private static String[] getDeniedPermissions(String[] permissions) {
        if (permissions == null) {
            return null;
        }

        final ArrayList<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (!hasPermission(permission))
                deniedPermissions.add(permission);
        }

        return deniedPermissions.toArray(tempArr);
    }

    private static void showPermissionNotification(int actionCode, int requestCode, String[] permissions, @StringRes int msgId) {
        Intent intent = new Intent(FleetApplication.getAppContext(), MainActivity.class);
        intent.setAction(PERMISSION_ACTION_KEY);
        intent.putExtra(PERMISSION_TYPE_CODE, actionCode);
        intent.putExtra(PERMISSION_REQUEST_CODE, requestCode);
        intent.putExtra(PERMISSION_ARRAY, permissions);
        intent.putExtra(PERMISSION_MESSAGE_ID, msgId);

        PendingIntent pendingIntent = PendingIntent.getActivity(FleetApplication.getAppContext(),
                REQUEST_PERMISSION_ACTION, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(FleetApplication.getAppContext())
                .setSmallIcon(R.drawable.logo).setAutoCancel(true).setContentIntent(pendingIntent)
                .setColor(Color.parseColor("#4A5767")).setContentText(FleetApplication.getAppContext().getString(msgId))
                .setContentTitle(FleetApplication.getAppContext().getString(R.string.app_name));

        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
        textStyle.setBigContentTitle(FleetApplication.getAppContext().getString(R.string.app_name));
        textStyle.bigText(FleetApplication.getAppContext().getString(msgId));
        mBuilder.setStyle(textStyle);
        mBuilder.setLights(0xff00ff00, 500, 3000);
        mBuilder.setVibrate(new long[]{0, 100});

        mBuilder.setContentIntent(pendingIntent);
        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager = (NotificationManager) FleetApplication
                .getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(actionCode, notification);
    }

    public static boolean isUsageSettingsSupported() {
        if (Build.VERSION.SDK_INT < 21) {
            return false;
        }

        if (driveSafePermissionIntent == null) {
            driveSafePermissionIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        }

        if (usageSettingSupportedState == -1) {
            List<ResolveInfo> info = FleetApplication.getAppContext().getPackageManager().queryIntentActivities(driveSafePermissionIntent, 0);
            usageSettingSupportedState = info == null ? 0 : info.size();
        }

        return (usageSettingSupportedState > 0);
    }

    @SuppressWarnings("NewApi")
    public static boolean needPermissionForWhiteListing() {
        if (Build.VERSION.SDK_INT < 21) {
            //below Lollipop we don't need permission
            return false;
        }

        try {
            PackageManager packageManager = FleetApplication.getAppContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(FleetApplication.getAppContext().getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) FleetApplication.getAppContext().getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode != AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }



    @SuppressWarnings("NewApi")
    public static boolean hasOverLayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Settings.canDrawOverlays(FleetApplication.getAppContext())) {
            //If we have the permission remove all overlay permission notification.
            NotificationManager notificationManager = (NotificationManager) FleetApplication
                    .getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(REQUEST_OVERLAY_PERMISSION);
            return true;
        }

        return false;
    }

    @SuppressWarnings("NewApi")
    public static void requestOverlayPermission() {
        if (!hasOverLayPermission()) {
            if (AppStateListener.isAppVisible()) {
                UIService.getInstance().showAlert(R.string.app_name, R.string.initial_setup_cb_msg, R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent overLayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        overLayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        FleetApplication.getAppContext().startActivity(overLayIntent);
                    }
                });
            } else {
                showPermissionNotification(REQUEST_OVERLAY_PERMISSION, -1, null, R.string.permission_ds_notification_msg);
            }
        }
    }

    public static boolean isPermissionRequestIntent(Intent intent) {
        return (intent != null && PERMISSION_ACTION_KEY.equals(intent.getAction()));
    }

    public static String[] getAllPermissions() {
        if (allPermissions != null)
            return allPermissions;

        PackageManager pkgManager = FleetApplication.getAppContext().getPackageManager();

        String pkgName = FleetApplication.getAppContext().getPackageName();
        try {
            PackageInfo pkgInfo = pkgManager.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
            String[] permissions = pkgInfo.requestedPermissions;
            if (permissions == null || permissions.length == 0) {
                return permissions;
            }

            ArrayList<String> dangerousPermissions = new ArrayList<>();
            for (String permission : permissions) {
                try {
                    PermissionInfo info = pkgManager.getPermissionInfo(permission, PackageManager.GET_META_DATA);
                    if (info != null && info.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                        dangerousPermissions.add(permission);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Logger.d(TAG, "Permission not found : " + permission);
                }
            }

            allPermissions = dangerousPermissions.toArray(tempArr);
            return allPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.d(TAG, "Package name not found", e);
        }

        return null;
    }

    private static void onPermissionBlocked(@StringRes int blockedMsgID, boolean showCancel, boolean cancellable) {
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                FleetApplication.getAppContext().startActivity(intent);
            }
        };

        if (showCancel) {
            UIService.getInstance().showAlert(R.string.permission_screen_denied_title,
                    R.string.permission_screen_denied_desc,
                    R.string.ok, clickListener, R.string.cancel, null, cancellable);
        } else {
            UIService.getInstance().showAlert(R.string.permission_screen_denied_title,
                    R.string.permission_screen_denied_desc,
                    R.string.ok, clickListener, cancellable);
        }
    }

    @SuppressWarnings("NewApi")
    public static boolean isIgnoringBatteryOptimizations() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ((PowerManager) FleetApplication.getAppContext().getSystemService(Context.POWER_SERVICE))
                        .isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID));
    }

    public static boolean requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
            FleetApplication.getAppContext().startActivity(intent);
            return true;
        } catch (Exception e) {
            Logger.e(TAG, "Error while requesting battery optimization.");
            return false;
        }
    }




}
