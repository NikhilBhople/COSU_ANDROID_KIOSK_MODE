package nikhil.bhople.kioskmode.kiosk;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.UserManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;

public class KioskModeManager {


    private static final String Battery_PLUGGED_ANY = Integer.toString(BatteryManager.BATTERY_PLUGGED_AC
            | BatteryManager.BATTERY_PLUGGED_USB | BatteryManager.BATTERY_PLUGGED_WIRELESS);

    private static final String DONT_STAY_ON = "0";

    final static int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    public static void startKioskMode(Activity activity) {

        ComponentName mAdminComponentName = DeviceAdmin.getComponentName(activity.getApplicationContext());
        DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager) activity.getApplicationContext()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (mDevicePolicyManager != null) {
            if (mDevicePolicyManager.isDeviceOwnerApp(activity.getPackageName())) {
                // get this app package name
                String[] packages = {activity.getPackageName()};
                // mDPM is the admin package, and allow the specified packages to lock task
                mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, packages);
                lockAction(activity);
            } else {
                Toast.makeText(activity.getApplicationContext(), "Device is not owner", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void exitKioskMode(Activity activity) {
        ComponentName mAdminComponentName = DeviceAdmin.getComponentName(activity.getApplicationContext());
        DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager) activity.getApplicationContext()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);

        unlockAction(activity, mDevicePolicyManager, mAdminComponentName);
    }

    private static void lockAction(Activity activity) {
        ComponentName mAdminComponentName = DeviceAdmin.getComponentName(activity.getApplicationContext());
        DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager) activity.getApplicationContext()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        setDefaultCosuPolicies(true, activity, mDevicePolicyManager, mAdminComponentName);
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        final View decorView = activity.getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

            public void onSystemUiVisibilityChange(int visibility) {
                // Note that system bars will only be "visible" if none of the
                // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                } else {
                    // TODO: The system bars are NOT visible. Make any desired
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.
                }
            }
        });
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity.startLockTask();
    }

    private static void unlockAction(Activity activity, DevicePolicyManager mDevicePolicyManager,
                                     ComponentName mAdminComponentName) {
        setDefaultCosuPolicies(false, activity, mDevicePolicyManager, mAdminComponentName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, false);
            mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, false);
        }

        mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, activity.getPackageName());
        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, new String[]{});
        activity.stopLockTask();
        PackageManager pm = activity.getPackageManager();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> infoList = pm.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
        // Scan the list to find the first match that isn't my own app
        for (ResolveInfo info : infoList) {
            if (!activity.getPackageName().equals(info.activityInfo.packageName)) {
                // This is the first match that isn't my package, so copy the
                // package and class names into to the HOME Intent
                homeIntent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                break;
            }
        }

        // Clear device owner permission
        //mDevicePolicyManager.clearDeviceOwnerApp(activity.getPackageName());

        // Launch the default OTHER HOME screen
        activity.startActivity(homeIntent);
        activity.finish();
    }

    private static void setDefaultCosuPolicies(boolean active, Activity activity,
                                               DevicePolicyManager mDevicePolicyManager, ComponentName mAdminComponentName) {
        // disable keyguard and status bar // set user restrictions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active, mDevicePolicyManager, mAdminComponentName);
        }
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active, mDevicePolicyManager, mAdminComponentName);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active, mDevicePolicyManager, mAdminComponentName);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active, mDevicePolicyManager,
                mAdminComponentName);
        // setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
            mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);
        }


        // enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active, mDevicePolicyManager, mAdminComponentName);

        // set System Update policy

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (active) {
                mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,
                        SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
            } else {
                mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null);
            }
        }

        // set this Activity as a lock task package

        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName,
                active ? new String[]{activity.getPackageName()} : new String[]{});

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            // set Cosu activity as home intent receiver so that it is started
            // on reboot
            mDevicePolicyManager.addPersistentPreferredActivity(mAdminComponentName, intentFilter,
                    new ComponentName(activity.getPackageName(), activity.getClass().getName()));
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName,
                    activity.getPackageName());
        }
    }

    private static void setUserRestriction(String restriction, boolean disallow,
                                           DevicePolicyManager mDevicePolicyManager, ComponentName mAdminComponentName) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
        }
    }

    private static void enableStayOnWhilePluggedIn(boolean enabled, DevicePolicyManager mDevicePolicyManager,
                                                   ComponentName mAdminComponentName) {
        if (enabled) {
            mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    Battery_PLUGGED_ANY);
        } else {
            mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    DONT_STAY_ON);
        }
    }
}