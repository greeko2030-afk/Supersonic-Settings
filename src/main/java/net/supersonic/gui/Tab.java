package net.supersonic.gui;

public enum Tab {
    GAMEPLAY("Gameplay", "Customize your in-game experience.", 0),
    VISUALS("Visuals", "Tune graphics quality and effects.", 1),
    PERFORMANCE("Performance", "Maximize FPS. Minimize lag. Built for speed.", 2),
    HUD("HUD & Overlay", "Customize your in-game HUD and overlays.", 3),
    AUDIO("Audio", "Fine tune your in-game sound experience.", 4),
    MULTIPLAYER("Multiplayer", "Enhance your multiplayer experience.", 5),
    VOICE("Voice Chat", "Speak to chat or run commands. Left Shift + V.", 9),
    CONTROLS("Controls", "Customize your keybinds and mouse settings.", 6),
    MODS("Mods & Addons", "See your installed mods and open folders.", 7),
    CAPTURE("Replay & Capture", "Record, review and share your best moments.", 8);

    public final String title;
    public final String subtitle;
    public final int icon;

    Tab(String title, String subtitle, int icon) {
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
    }
}
