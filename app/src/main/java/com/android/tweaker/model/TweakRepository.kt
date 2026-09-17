package com.android.tweaker.model

object TweakRepository {
    fun getTweaks(): List<TweakItem> = listOf(
        // --- INFO CATEGORY ---
        TweakItem(
            id = "info_battery",
            category = TweakCategoryType.INFO,
            title = "Detailed Battery Status",
            description = "Displays battery health, level, voltage, temperature, and charging status.",
            commandTemplate = "dumpsys battery"
        ),
        TweakItem(
            id = "info_cpu",
            category = TweakCategoryType.INFO,
            title = "Hardware & CPU Specs",
            description = "Reads detailed CPU architecture, core details, and processor hardware specs.",
            commandTemplate = "cat /proc/cpuinfo"
        ),
        TweakItem(
            id = "info_display",
            category = TweakCategoryType.INFO,
            title = "Screen Resolution & Density",
            description = "Queries active screen physical size and display DPI density.",
            commandTemplate = "wm size && wm density"
        ),
        TweakItem(
            id = "info_ram",
            category = TweakCategoryType.INFO,
            title = "Real-Time RAM Usage",
            description = "Shows live system memory allocation, swap, free RAM, and process memory details.",
            commandTemplate = "dumpsys meminfo"
        ),
        TweakItem(
            id = "info_system",
            category = TweakCategoryType.INFO,
            title = "System & Android Version",
            description = "Fetches release version, build ID, security patch level, and system details.",
            commandTemplate = "getprop ro.build.version.release && getprop ro.build.display.id"
        ),
        TweakItem(
            id = "info_network",
            category = TweakCategoryType.INFO,
            title = "Network Status & IP",
            description = "Lists network interfaces, IP addresses (IPv4 & IPv6), and link states.",
            commandTemplate = "ip addr show"
        ),
        TweakItem(
            id = "info_uptime",
            category = TweakCategoryType.INFO,
            title = "System Uptime Statistics",
            description = "Displays how long the device has been running since last reboot.",
            commandTemplate = "uptime"
        ),
        TweakItem(
            id = "info_thermal",
            category = TweakCategoryType.INFO,
            title = "Thermal Sensors & Temperature",
            description = "Monitors system thermal zones, CPU/GPU temperatures, and throttling levels.",
            commandTemplate = "dumpsys thermal"
        ),
        TweakItem(
            id = "info_bootloader",
            category = TweakCategoryType.INFO,
            title = "Bootloader / Knox / DRM Status",
            description = "Inspects verified boot status, Knox tamper state, and hardware security state.",
            commandTemplate = "getprop ro.boot.verifiedbootstate && getprop ro.boot.flash.locked"
        ),
        TweakItem(
            id = "info_features",
            category = TweakCategoryType.INFO,
            title = "Supported Hardware Features",
            description = "Lists all system feature flags, sensors, NFC, Bluetooth, and hardware caps.",
            commandTemplate = "pm list features"
        ),

        // --- APP CONTROL CATEGORY ---
        TweakItem(
            id = "app_disable",
            category = TweakCategoryType.APP_CONTROL,
            title = "Disable / Freeze System App",
            description = "Freezes a system app or bloatware package for User 0.",
            commandTemplate = "pm disable-user --user 0 {package_name}",
            inputType = InputType.SingleText("Package Name", "e.g. com.samsung.android.bouldering")
        ),
        TweakItem(
            id = "app_enable",
            category = TweakCategoryType.APP_CONTROL,
            title = "Re-enable Frozen App",
            description = "Unfreezes and enables a previously disabled application package.",
            commandTemplate = "pm enable {package_name}",
            inputType = InputType.SingleText("Package Name", "e.g. com.samsung.android.bouldering")
        ),
        TweakItem(
            id = "app_uninstall",
            category = TweakCategoryType.APP_CONTROL,
            title = "Uninstall App for Current User",
            description = "Removes a system package for User 0 while keeping base APK system binary.",
            commandTemplate = "pm uninstall -k --user 0 {package_name}",
            inputType = InputType.SingleText("Package Name", "e.g. com.facebook.katana")
        ),
        TweakItem(
            id = "app_grant_perm",
            category = TweakCategoryType.APP_CONTROL,
            title = "Grant Special Permission",
            description = "Grants a sensitive permission (e.g. WRITE_SECURE_SETTINGS) to an application.",
            commandTemplate = "pm grant {package_name} {permission}",
            inputType = InputType.TwoText("Package Name", "e.g. com.example.app", "Permission", "e.g. android.permission.WRITE_SECURE_SETTINGS")
        ),
        TweakItem(
            id = "app_revoke_perm",
            category = TweakCategoryType.APP_CONTROL,
            title = "Revoke App Permission",
            description = "Revokes a specific permission from an installed package.",
            commandTemplate = "pm revoke {package_name} {permission}",
            inputType = InputType.TwoText("Package Name", "e.g. com.example.app", "Permission", "e.g. android.permission.CAMERA")
        ),
        TweakItem(
            id = "app_force_stop",
            category = TweakCategoryType.APP_CONTROL,
            title = "Force Stop App",
            description = "Immediately terminates all active processes and services of a package.",
            commandTemplate = "am force-stop {package_name}",
            inputType = InputType.SingleText("Package Name", "e.g. com.instagram.android")
        ),
        TweakItem(
            id = "app_clear_data",
            category = TweakCategoryType.APP_CONTROL,
            title = "Clear Data & Cache",
            description = "Wipes all user data, cached files, and databases for a package.",
            commandTemplate = "pm clear {package_name}",
            inputType = InputType.SingleText("Package Name", "e.g. com.whatsapp")
        ),
        TweakItem(
            id = "app_restrict_bg",
            category = TweakCategoryType.APP_CONTROL,
            title = "Restrict Background Execution",
            description = "Prevents an app from executing background tasks or wake locks.",
            commandTemplate = "cmd appops set {package_name} RUN_IN_BACKGROUND ignore",
            inputType = InputType.SingleText("Package Name", "e.g. com.facebook.orca")
        ),
        TweakItem(
            id = "app_list_packages",
            category = TweakCategoryType.APP_CONTROL,
            title = "List Installed Packages",
            description = "Lists all installed applications including hidden system packages with path.",
            commandTemplate = "pm list packages -f"
        ),
        TweakItem(
            id = "app_install_apk",
            category = TweakCategoryType.APP_CONTROL,
            title = "Install APK File",
            description = "Installs an APK directly from a device file path.",
            commandTemplate = "pm install -r {path_to_apk}",
            inputType = InputType.SingleText("APK File Path", "e.g. /sdcard/Download/app.apk")
        ),

        // --- SETTINGS CATEGORY ---
        TweakItem(
            id = "set_anim_scale",
            category = TweakCategoryType.SETTINGS,
            title = "Window Animation Speed",
            description = "Speed up OS UI transitions (0.5x makes animations faster, 0.0x turns off).",
            commandTemplate = "settings put global window_animation_scale {option} && settings put global transition_animation_scale {option} && settings put global animator_duration_scale {option}",
            inputType = InputType.Options("Animation Speed", listOf("0.0 (Off)", "0.5 (Fast)", "1.0 (Default)", "1.5 (Slow)"), defaultIndex = 1)
        ),
        TweakItem(
            id = "set_dark_mode",
            category = TweakCategoryType.SETTINGS,
            title = "System Dark Mode",
            description = "Force enable or disable system-wide Dark Theme.",
            commandTemplate = "cmd uimode night {option}",
            inputType = InputType.Options("Dark Mode", listOf("yes", "no"), defaultIndex = 0)
        ),
        TweakItem(
            id = "set_dpi",
            category = TweakCategoryType.SETTINGS,
            title = "Custom Display Density (DPI)",
            description = "Changes screen display DPI scaling.",
            commandTemplate = "wm density {density_value}",
            inputType = InputType.SingleText("DPI Value", "e.g. 420 or reset", defaultValue = "420")
        ),
        TweakItem(
            id = "set_clean_statusbar",
            category = TweakCategoryType.SETTINGS,
            title = "Status Bar Clean Mode",
            description = "Hides icons from the status bar (e.g. wifi, mobile, bluetooth).",
            commandTemplate = "settings put secure icon_blacklist {icon_list}",
            inputType = InputType.SingleText("Icons to Blacklist", "e.g. wifi,mobile,bluetooth", defaultValue = "wifi,mobile,bluetooth")
        ),
        TweakItem(
            id = "set_immersive",
            category = TweakCategoryType.SETTINGS,
            title = "Immersive Mode",
            description = "Hides navigation bar, status bar, or both globally.",
            commandTemplate = "settings put global policy_control {option}",
            inputType = InputType.Options("Immersive Mode", listOf("immersive.full=*", "immersive.status=*", "immersive.navigation=*", "none"), defaultIndex = 0)
        ),
        TweakItem(
            id = "set_max_refresh_rate",
            category = TweakCategoryType.SETTINGS,
            title = "Disable Refresh Rate Throttling",
            description = "Forces maximum screen refresh rate (e.g. 120Hz peak).",
            commandTemplate = "settings put system peak_refresh_rate 120.0 && settings put system min_refresh_rate 120.0"
        ),
        TweakItem(
            id = "set_wifi_scan",
            category = TweakCategoryType.SETTINGS,
            title = "Disable Continuous Background Wi-Fi Scan",
            description = "Stops location services from constantly scanning Wi-Fi networks in background.",
            commandTemplate = "settings put global wifi_scan_always_enabled {option}",
            inputType = InputType.Options("Wi-Fi Scan Always Enabled", listOf("0 (Disabled)", "1 (Enabled)"), defaultIndex = 0)
        ),
        TweakItem(
            id = "set_tether_noprovisioning",
            category = TweakCategoryType.SETTINGS,
            title = "Bypass Carrier Wi-Fi Tethering Check",
            description = "Forces mobile hotspot tethering without carrier entitlement checks.",
            commandTemplate = "settings put global net.tethering.noprovisioning true"
        ),
        TweakItem(
            id = "set_screen_timeout",
            category = TweakCategoryType.SETTINGS,
            title = "Custom Screen Timeout",
            description = "Sets display off timeout duration in milliseconds.",
            commandTemplate = "settings put system screen_off_timeout {timeout_ms}",
            inputType = InputType.SingleText("Timeout (in ms)", "e.g. 60000 for 1 min", defaultValue = "60000")
        ),
        TweakItem(
            id = "set_auto_rotation",
            category = TweakCategoryType.SETTINGS,
            title = "Auto-Rotation Switch",
            description = "Enables or disables screen automatic rotation.",
            commandTemplate = "settings put system accelerometer_rotation {option}",
            inputType = InputType.Options("Auto Rotation", listOf("1 (Enabled)", "0 (Disabled)"), defaultIndex = 0)
        ),

        // --- DANGER CATEGORY ---
        TweakItem(
            id = "danger_factory_reset",
            category = TweakCategoryType.DANGER,
            title = "Factory Reset (Wipe Data)",
            description = "Triggers an immediate recovery wipe of all user data!",
            commandTemplate = "recovery --wipe_data",
            isDanger = true,
            dangerDescription = "This command WILL WIPE ALL USER DATA, APPS, AND FILES from the device! It will immediately reboot into recovery and perform a factory reset."
        ),
        TweakItem(
            id = "danger_reboot_bootloader",
            category = TweakCategoryType.DANGER,
            title = "Reboot to Bootloader / Fastboot",
            description = "Reboots device into fastboot/bootloader mode.",
            commandTemplate = "reboot bootloader",
            isDanger = true,
            dangerDescription = "This will reboot your device into Fastboot/Bootloader mode. Make sure your device supports fastboot mode."
        ),
        TweakItem(
            id = "danger_reboot_recovery",
            category = TweakCategoryType.DANGER,
            title = "Reboot to Recovery Mode",
            description = "Reboots device into recovery mode.",
            commandTemplate = "reboot recovery",
            isDanger = true,
            dangerDescription = "This will reboot your device into stock/custom Recovery mode immediately."
        ),
        TweakItem(
            id = "danger_wm_size",
            category = TweakCategoryType.DANGER,
            title = "Change Screen Resolution",
            description = "Changes display resolution dimensions (Risk of un-clickable broken UI!).",
            commandTemplate = "wm size {dimensions}",
            inputType = InputType.SingleText("Resolution (WidthxHeight)", "e.g. 1080x2400"),
            isDanger = true,
            dangerDescription = "Setting an invalid or unsupported resolution can render the screen UI un-clickable or completely black!"
        ),
        TweakItem(
            id = "danger_emergency_reset_wm",
            category = TweakCategoryType.DANGER,
            title = "Emergency Resolution & DPI Reset",
            description = "Resets display resolution and DPI to factory defaults.",
            commandTemplate = "wm size reset && wm density reset",
            isDanger = true,
            dangerDescription = "Resets display resolution and scaling density back to original stock values."
        ),
        TweakItem(
            id = "danger_disable_gms",
            category = TweakCategoryType.DANGER,
            title = "Disable Google Play Services (GMS)",
            description = "Disables GMS (Completely breaks Google Play, Maps, Push notifications, and most apps).",
            commandTemplate = "pm disable-user --user 0 com.google.android.gms",
            isDanger = true,
            dangerDescription = "Disabling Google Play Services will cause widespread app crashes, loss of push notifications, and account syncing errors!"
        ),
        TweakItem(
            id = "danger_disable_systemui",
            category = TweakCategoryType.DANGER,
            title = "Disable System UI",
            description = "Disables Android System UI (Black screen / frozen interface!).",
            commandTemplate = "pm disable-user --user 0 com.android.systemui",
            isDanger = true,
            dangerDescription = "Disabling System UI will IMMEDIATELY render your status bar, navigation bar, and wallpaper black and unresponsive!"
        ),
        TweakItem(
            id = "danger_kill_media_process",
            category = TweakCategoryType.DANGER,
            title = "Kill Vital System Process (Media)",
            description = "Forces termination of core media provider system process.",
            commandTemplate = "killall android.process.media",
            isDanger = true,
            dangerDescription = "Terminating vital system processes may cause sudden app crashes, media store corruption, or system instability."
        ),
        TweakItem(
            id = "danger_disable_doze",
            category = TweakCategoryType.DANGER,
            title = "Disable Doze Deep Idle Mode",
            description = "Disables device battery Doze power saver mode.",
            commandTemplate = "dumpsys deviceidle disable",
            isDanger = true,
            dangerDescription = "Disabling Doze mode will increase background power drain and reduce battery life."
        ),
        TweakItem(
            id = "danger_doze_constants",
            category = TweakCategoryType.DANGER,
            title = "Custom Doze Constants Tuning",
            description = "Modifies advanced Doze parameters (Light & Deep idle constants).",
            commandTemplate = "settings put global device_idle_constants {parameters}",
            inputType = InputType.SingleText("Parameters", "e.g. inactive_to=30000,sensing_to=0"),
            isDanger = true,
            dangerDescription = "Invalid Doze constants can cause unexpected battery drain or system sleep issues."
        )
    )
}
