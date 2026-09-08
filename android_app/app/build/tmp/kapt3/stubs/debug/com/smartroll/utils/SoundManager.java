package com.smartroll.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/smartroll/utils/SoundManager;", "", "()V", "clickSoundId", "", "soundPool", "Landroid/media/SoundPool;", "init", "", "context", "Landroid/content/Context;", "playClick", "view", "Landroid/view/View;", "app_debug"})
public final class SoundManager {
    @org.jetbrains.annotations.Nullable()
    private static android.media.SoundPool soundPool;
    private static int clickSoundId = 0;
    @org.jetbrains.annotations.NotNull()
    public static final com.smartroll.utils.SoundManager INSTANCE = null;
    
    private SoundManager() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void playClick(@org.jetbrains.annotations.NotNull()
    android.view.View view) {
    }
}