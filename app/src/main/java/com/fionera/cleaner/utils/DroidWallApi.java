package com.fionera.cleaner.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.fionera.cleaner.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class DroidWallApi {

    public static final int SPECIAL_UID_ANY = -10;
    public static final String SCRIPT_FILE = "droid_wall.sh";

    public static final String PREFS_CACHE = "DroidWallAppCachePrefs";
    public static final String PREFS_CONFIG = "DroidWallConfigPrefs";
    public static final String PREF_3G_UIDS = "AllowedUids3G";
    public static final String PREF_WIFI_UIDS = "AllowedUidsWifi";

    public static final String ITFS_WIFI[] = {"wlan+", "tiwlan+", "eth+"};
    public static final String ITFS_3G[] = {"rmnet+", "pdp+", "ppp+", "uwbr+"};

    public static DroidApp applications[] = null;
    private static boolean hasRoot = false;

    private static void alert(Context ctx, CharSequence msg) {
        if (ctx != null) {
            new AlertDialog.Builder(ctx).setNeutralButton(android.R.string.ok, null).setMessage(msg)
                    .show();
        }
    }

    private static String scriptHeader(Context ctx) {
        final String dir = ctx.getCacheDir().getAbsolutePath();
        return "IPTABLES=iptables\n" +
                "BUSYBOX=busybox\n" +
                "GREP=grep\n" +
                "ECHO=echo\n" +
                "# Try to find busybox\n" +
                "if " + dir + "/busybox_g1 --help >/dev/null 2>/dev/null ; then\n" +
                "	BUSYBOX=" + dir + "/busybox_g1\n" +
                "	GREP=\"$BUSYBOX grep\"\n" +
                "	ECHO=\"$BUSYBOX echo\"\n" +
                "elif busybox --help >/dev/null 2>/dev/null ; then\n" +
                "	BUSYBOX=busybox\n" +
                "elif /system/xbin/busybox --help >/dev/null 2>/dev/null ; then\n" +
                "	BUSYBOX=/system/xbin/busybox\n" +
                "elif /system/bin/busybox --help >/dev/null 2>/dev/null ; then\n" +
                "	BUSYBOX=/system/bin/busybox\n" +
                "fi\n" +
                "# Try to find grep\n" +
                "if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n" +
                "	if $ECHO 1 | $BUSYBOX grep -q 1 >/dev/null 2>/dev/null ; then\n" +
                "		GREP=\"$BUSYBOX grep\"\n" +
                "	fi\n" +
                "	# Grep is absolutely required\n" +
                "	if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n" +
                "		$ECHO The grep command is required. DroidWall will not work.\n" +
                "		exit 1\n" +
                "	fi\n" +
                "fi\n" +
                "# Try to find iptables\n" +
                "if " + dir + "/iptables_g1 --version >/dev/null 2>/dev/null ; then\n" +
                "	IPTABLES=" + dir + "/iptables_g1\n" +
                "elif " + dir + "/iptables_n1 --version >/dev/null 2>/dev/null ; then\n" +
                "	IPTABLES=" + dir + "/iptables_n1\n" +
                "fi\n";
    }

    public static boolean applyIpTablesRules(Context ctx, boolean showErrors) {
        if (ctx == null) {
            return false;
        }
        saveRules(ctx);
        return applySavedIpTablesRules(ctx, showErrors);
    }

    /**
     * 单纯的保存规则
     *
     * @param ctx
     */
    public static void saveRules(Context ctx) {
        final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_CONFIG, 0);
        final DroidApp[] apps = getApps(ctx);
        final StringBuilder newuids_wifi = new StringBuilder();
        final StringBuilder newuids_3g = new StringBuilder();
        if (apps != null) {
            for (DroidApp app : apps) {
                if (app.selected_wifi) {
                    if (newuids_wifi.length() != 0) {
                        newuids_wifi.append('|');
                    }
                    newuids_wifi.append(app.uid);
                }
                if (app.selected_3g) {
                    if (newuids_3g.length() != 0) {
                        newuids_3g.append('|');
                    }
                    newuids_3g.append(app.uid);
                }
            }
            final Editor edit = prefs.edit();
            edit.putString(PREF_WIFI_UIDS, newuids_wifi.toString());
            edit.putString(PREF_3G_UIDS, newuids_3g.toString());
            edit.apply();
        }
    }

    public static boolean applySavedIpTablesRules(Context ctx, boolean showErrors) {
        if (ctx == null) {
            return false;
        }
        final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_CONFIG, 0);
        final String savedUids_wifi = prefs.getString(PREF_WIFI_UIDS, "");
        final String savedUids_3g = prefs.getString(PREF_3G_UIDS, "");
        final List<Integer> uids_wifi = new LinkedList<>();
        if (savedUids_wifi.length() > 0) {
            for (String uid_wifi : savedUids_wifi.split("|")) {
                if (!uid_wifi.equals("") && !uid_wifi.equals("|")) {
                    uids_wifi.add(Integer.parseInt(uid_wifi));
                }
            }
        }
        final List<Integer> uids_3g = new LinkedList<>();
        if (savedUids_3g.length() > 0) {
            for (String uid_3g : savedUids_3g.split("|")) {
                if (!uid_3g.equals("") && !uid_3g.equals("|")) {
                    uids_3g.add(Integer.parseInt(uid_3g));
                }
            }
        }
        return applyIpTablesRulesImpl(ctx, uids_wifi, uids_3g, showErrors);
    }

    private static boolean applyIpTablesRulesImpl(Context ctx, List<Integer> uidsWifi,
                                                  List<Integer> uids3g, boolean showErrors) {
        if (ctx == null) {
            return false;
        }
        assertBinaries(ctx, showErrors);

        final StringBuilder script = new StringBuilder();
        try {
            script.append(scriptHeader(ctx));
            script.append("$IPTABLES --version || exit 1\n" +
                                  "# Create the droidwall chains if necessary\n" +
                                  "$IPTABLES -L droidwall >/dev/null 2>/dev/null || $IPTABLES " +
                                  "--new droidwall || exit 2\n" +
                                  "$IPTABLES -L droidwall-3g >/dev/null 2>/dev/null || $IPTABLES " +
                                  "--new droidwall-3g || exit 3\n" +
                                  "$IPTABLES -L droidwall-wifi >/dev/null 2>/dev/null || " +
                                  "$IPTABLES --new droidwall-wifi || exit 4\n" +
                                  "$IPTABLES -L droidwall-reject >/dev/null 2>/dev/null || " +
                                  "$IPTABLES --new droidwall-reject || exit 5\n" +
                                  "# Add droidwall chain to OUTPUT chain if necessary\n" +
                                  "$IPTABLES -L OUTPUT | $GREP -q droidwall || $IPTABLES -A " +
                                  "OUTPUT -j droidwall || exit 6\n" +
                                  "# Flush existing rules\n" +
                                  "$IPTABLES -F droidwall || exit 7\n" +
                                  "$IPTABLES -F droidwall-3g || exit 8\n" +
                                  "$IPTABLES -F droidwall-wifi || exit 9\n" +
                                  "$IPTABLES -F droidwall-reject || exit 10\n" +
                                  "");
            script.append("# Create the reject rule (log disabled)\n" +
                                  "$IPTABLES -A droidwall-reject -j REJECT || exit 11\n" +
                                  "");
            script.append("# Main rules (per interface)\n");
            for (String itf : ITFS_WIFI) {
                script.append("$IPTABLES -A droidwall -o ").append(itf)
                        .append(" -j droidwall-wifi || exit\n");
            }
            for (String itf : ITFS_3G) {
                script.append("$IPTABLES -A droidwall -o ").append(itf)
                        .append(" -j droidwall-3g || exit\n");
            }

            script.append("# Filtering rules\n");
            final String targetRule = ("droidwall-reject");
            if (uidsWifi.indexOf(SPECIAL_UID_ANY) >= 0) {
                script.append("$IPTABLES -A droidwall-wifi -j ").append(targetRule)
                        .append(" || exit\n");
            } else {
                for (final Integer uid : uidsWifi) {
                    script.append("$IPTABLES -A droidwall-wifi -m owner --uid-owner ").append(uid)
                            .append(" -j ").append(targetRule).append(" || exit\n");
                }
            }
            if (uids3g.indexOf(SPECIAL_UID_ANY) >= 0) {
                script.append("$IPTABLES -A droidwall-3g -j ").append(targetRule)
                        .append(" || exit\n");
            } else {
                for (final Integer uid : uids3g) {
                    script.append("$IPTABLES -A droidwall-3g -m owner --uid-owner ").append(uid)
                            .append(" -j ").append(targetRule).append(" || exit\n");
                }
            }

            final StringBuilder res = new StringBuilder();
            int code = runScriptAsRoot(ctx, script.toString(), res);
            if (showErrors && code != 0) {
                String msg = res.toString();
                Log.e("DroidWall", msg);
                // Remove unnecessary help message from output
                if (msg.contains(
                        "\nTry `iptables -h' or 'iptables --help' for more information.")) {
                    msg = msg.replace(
                            "\nTry `iptables -h' or 'iptables --help' for more information.", "");
                }
                alert(ctx, "应用规则失败，错误码: " + code + "\n\n" + msg.trim());
            } else {
                return true;
            }
        } catch (Exception e) {
            if (showErrors) {
                alert(ctx, "应用规则失败: " + e);
            }
        }
        return false;
    }

    public static boolean purgeIpTables(Context ctx, boolean showErrors) {
        StringBuilder res = new StringBuilder();
        try {
            assertBinaries(ctx, showErrors);
            int code = runScriptAsRoot(ctx, scriptHeader(ctx) +
                    "$IPTABLES -F droidwall\n" +
                    "$IPTABLES -F droidwall-reject\n" +
                    "$IPTABLES -F droidwall-3g\n" +
                    "$IPTABLES -F droidwall-wifi\n", res);
            if (showErrors && code != 0) {
                alert(ctx, "移除规则失败，错误码: " + code + "\n" + res);
                return false;
            }
            return true;
        } catch (Exception e) {
            if (showErrors) {
                alert(ctx, "移除规则失败: " + e);
            }
            return false;
        }
    }

    public static void showIpTablesRules(Context ctx) {
        try {
            final StringBuilder res = new StringBuilder();
            runScriptAsRoot(ctx, scriptHeader(ctx) +
                    "$ECHO $IPTABLES\n" +
                    "$IPTABLES -L -v\n", res);
            alert(ctx, res);
        } catch (Exception e) {
            alert(ctx, "获取错误: " + e);
        }
    }

    public static DroidApp[] getApps(Context ctx) {
        if (applications != null) {
            return applications;
        }
        final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_CACHE, 0);
        final String savedUids_wifi = prefs.getString(PREF_WIFI_UIDS, "");
        final String savedUids_3g = prefs.getString(PREF_3G_UIDS, "");
        int selected_wifi[] = new int[0];
        int selected_3g[] = new int[0];
        if (savedUids_wifi.length() > 0) {
            selected_wifi = new int[savedUids_wifi.split("|").length];
            for (int i = 0; i < selected_wifi.length; i++) {
                String uid = savedUids_wifi.split("|")[i];
                if (!uid.equals("")) {
                    try {
                        selected_wifi[i] = Integer.parseInt(uid);
                    } catch (Exception ex) {
                        selected_wifi[i] = -1;
                    }
                }
            }
            Arrays.sort(selected_wifi);
        }
        if (savedUids_3g.length() > 0) {
            selected_3g = new int[savedUids_3g.split("|").length];
            for (int i = 0; i < selected_3g.length; i++) {
                String uid = savedUids_3g.split("|")[i];
                if (!uid.equals("")) {
                    try {
                        selected_3g[i] = Integer.parseInt(uid);
                    } catch (Exception ex) {
                        selected_3g[i] = -1;
                    }
                }
            }
            Arrays.sort(selected_3g);
        }
        try {
            final PackageManager pkgmanager = ctx.getPackageManager();
            final List<ApplicationInfo> installed = pkgmanager.getInstalledApplications(0);
            final HashMap<Integer, DroidApp> map = new HashMap<>();
            final Editor edit = prefs.edit();
            boolean changed = false;
            String name;
            String cacheKey;
            DroidApp app;
            for (final ApplicationInfo appInfo : installed) {
                app = map.get(appInfo.uid);
                if (app == null && PackageManager.PERMISSION_GRANTED != pkgmanager
                        .checkPermission(Manifest.permission.INTERNET, appInfo.packageName)) {
                    continue;
                }
                cacheKey = "cache.label." + appInfo.packageName;
                name = prefs.getString(cacheKey, "");
                if (name.length() == 0) {
                    name = pkgmanager.getApplicationLabel(appInfo).toString();
                    edit.putString(cacheKey, name);
                    changed = true;
                }
                if (app == null) {
                    app = new DroidApp(appInfo.uid, name, false, false);
                    map.put(appInfo.uid, app);
                } else {
                    final String newnames[] = new String[app.names.length + 1];
                    System.arraycopy(app.names, 0, newnames, 0, app.names.length);
                    newnames[app.names.length] = name;
                    app.names = newnames;
                }
                // check if this application is selected
                if (!app.selected_wifi && Arrays.binarySearch(selected_wifi, app.uid) >= 0) {
                    app.selected_wifi = true;
                }
                if (!app.selected_3g && Arrays.binarySearch(selected_3g, app.uid) >= 0) {
                    app.selected_3g = true;
                }
            }
            if (changed) {
                edit.apply();
            }
            final DroidApp special[] = {new DroidApp(SPECIAL_UID_ANY,
                                                     "(Any application) - Same as selecting all "
                                                             + "applications",
                                                     false, false), new DroidApp(
                    android.os.Process.getUidForName("root"),
                    "(root) - Applications running as root", false, false), new DroidApp(
                    android.os.Process.getUidForName("media"), "Media server", false,
                    false), new DroidApp(android.os.Process.getUidForName("vpn"), "VPN networking",
                                         false, false),};
            for (DroidApp aSpecial : special) {
                app = aSpecial;
                if (app.uid != -1 && !map.containsKey(app.uid)) {
                    if (Arrays.binarySearch(selected_wifi, app.uid) >= 0) {
                        app.selected_wifi = true;
                    }
                    if (Arrays.binarySearch(selected_3g, app.uid) >= 0) {
                        app.selected_3g = true;
                    }
                    map.put(app.uid, app);
                }
            }
            applications = new DroidApp[map.size()];
            int index = 0;
            for (DroidApp application : map.values()) {
                applications[index++] = application;
            }
            return applications;
        } catch (Exception e) {
            alert(ctx, "错误: " + e);
        }
        return null;
    }

    public static boolean hasRootAccess(Context ctx, boolean showErrors) {
        if (hasRoot) {
            return true;
        }
        final StringBuilder res = new StringBuilder();
        try {
            if (runScriptAsRoot(ctx, "exit 0", res) == 0) {
                hasRoot = true;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (showErrors) {
            alert(ctx, "获取Root权限失败");
        }
        return false;
    }

    public static int runScriptAsRoot(Context ctx, String script,
                                      StringBuilder res) throws IOException {
        return runScriptAsRoot(ctx, script, res, 40000);
    }

    public static int runScriptAsRoot(Context ctx, String script, StringBuilder res, long timeout) {
        return runScript(ctx, script, res, timeout, true);
    }

    /**
     * Runs a script, wither as root or as a regular user (multiple commands separated by "\n").
     *
     * @param ctx     mandatory context
     * @param script  the script to be executed
     * @param res     the script output response (stdout + stderr)
     * @param timeout timeout in milliseconds (-1 for none)
     * @return the script exit code
     */
    public static int runScript(Context ctx, String script, StringBuilder res, long timeout,
                                boolean asRoot) {
        final File file = new File(ctx.getCacheDir(), SCRIPT_FILE);
        final ScriptRunner runner = new ScriptRunner(file, script, res, asRoot);
        runner.start();
        try {
            if (timeout > 0) {
                runner.join(timeout);
            } else {
                runner.join();
            }
            if (runner.isAlive()) {
                runner.interrupt();
                runner.join(150);
                runner.join(50);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return runner.exitCode;
    }

    public static boolean assertBinaries(Context ctx, boolean showErrors) {
        boolean changed = false;
        try {
            File file = new File(ctx.getCacheDir(), "iptables_g1");
            if (!file.exists()) {
                copyRawFile(ctx, R.raw.iptables_g1, file, "755");
                changed = true;
            }
            file = new File(ctx.getCacheDir(), "iptables_n1");
            if (!file.exists()) {
                copyRawFile(ctx, R.raw.iptables_n1, file, "755");
                changed = true;
            }
            file = new File(ctx.getCacheDir(), "busybox_g1");
            if (!file.exists()) {
                copyRawFile(ctx, R.raw.busybox_g1, file, "755");
                changed = true;
            }
            if (changed) {
                ShowToast.show("管理工具安装成功");
            }
        } catch (Exception e) {
            if (showErrors) {
                alert(ctx, "安装网络管理二进制文件失败: " + e);
            }
            return false;
        }
        return true;
    }

    private static void copyRawFile(Context ctx, int resid, File file,
                                    String mode) throws IOException, InterruptedException {
        final String absPath = file.getAbsolutePath();
        final FileOutputStream out = new FileOutputStream(file);
        final InputStream is = ctx.getResources().openRawResource(resid);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        Runtime.getRuntime().exec("chmod " + mode + " " + absPath).waitFor();
    }

    /**
     * Internal thread used to execute scripts (as root or not).
     */
    private static final class ScriptRunner
            extends Thread {
        private final File file;
        private final String script;
        private final StringBuilder res;
        private final boolean asRoot;

        public int exitCode = -1;
        private Process exec;

        public ScriptRunner(File file, String script, StringBuilder res, boolean asRoot) {
            this.file = file;
            this.script = script;
            this.res = res;
            this.asRoot = asRoot;
        }

        @Override
        public void run() {
            try {
                final String absolutePath = file.getAbsolutePath();
                Runtime.getRuntime().exec("chmod 777 " + absolutePath).waitFor();
                final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file));
                out.write("#!/system/bin/sh\n");
                out.write(script);
                if (!script.endsWith("\n")) {
                    out.write("\n");
                }
                out.write("exit\n");
                out.flush();
                out.close();
                if (asRoot) {
                    // Create the "su" request to run the script
                    exec = Runtime.getRuntime().exec("su -c " + absolutePath);
                } else {
                    // Create the "sh" request to run the script
                    exec = Runtime.getRuntime().exec("sh " + absolutePath);
                }
                InputStreamReader r = new InputStreamReader(exec.getInputStream());
                final char buf[] = new char[1024];
                int read;
                // Consume the "stdout"
                while ((read = r.read(buf)) != -1) {
                    if (res != null) {
                        res.append(buf, 0, read);
                    }
                }
                // Consume the "stderr"
                r = new InputStreamReader(exec.getErrorStream());
                while ((read = r.read(buf)) != -1) {
                    if (res != null) {
                        res.append(buf, 0, read);
                    }
                }
                // get the process exit code
                if (exec != null) {
                    this.exitCode = exec.waitFor();
                }
            } catch (InterruptedException ex) {
                if (res != null) {
                    res.append("\n操作超时");
                }
            } catch (Exception ex) {
                if (res != null) {
                    res.append("\n").append(ex);
                }
            }
        }
    }

    public static final class DroidApp {
        /**
         * linux user id
         */
        public int uid;
        /**
         * application names belonging to this user id
         */
        public String names[];
        /**
         * indicates if this application is selected for wifi
         */
        public boolean selected_wifi;
        /**
         * indicates if this application is selected for 3g
         */
        public boolean selected_3g;
        /**
         * toString cache
         */
        private String toString;

        public DroidApp() {
        }

        public DroidApp(int uid, String name, boolean selected_wifi, boolean selected_3g) {
            this.uid = uid;
            this.names = new String[]{name};
            this.selected_wifi = selected_wifi;
            this.selected_3g = selected_3g;
        }

        /**
         * Screen representation of this application
         */
        @Override
        public String toString() {
            if (toString == null) {
                final StringBuilder s = new StringBuilder();
                if (uid > 0) {
                    s.append(uid).append(": ");
                }
                for (int i = 0; i < names.length; i++) {
                    if (i != 0) {
                        s.append(", ");
                    }
                    s.append(names[i]);
                }
                s.append("\n");
                toString = s.toString();
            }
            return toString;
        }
    }
}
