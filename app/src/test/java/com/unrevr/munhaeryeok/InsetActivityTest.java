package com.unrevr.munhaeryeok;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class InsetActivityTest {
    @Test
    @Config(sdk = {35, 36})
    public void cutoutBarsAndKeyboardUpdateWithoutAccumulatingPadding() {
        try (ActivityController<SettingInfoActivity> controller = Robolectric.buildActivity(SettingInfoActivity.class).setup()) {
            ViewGroup content = controller.get().findViewById(android.R.id.content);
            View root = content.getChildAt(0);
            WindowInsets bars = new WindowInsets.Builder()
                    .setInsets(WindowInsets.Type.statusBars(), android.graphics.Insets.of(0, 48, 0, 0))
                    .setInsets(WindowInsets.Type.navigationBars(), android.graphics.Insets.of(0, 0, 0, 24))
                    .setInsets(WindowInsets.Type.displayCutout(), android.graphics.Insets.of(64, 0, 0, 0))
                    .build();
            WindowInsets keyboard = new WindowInsets.Builder(bars)
                    .setInsets(WindowInsets.Type.ime(), android.graphics.Insets.of(0, 0, 0, 280))
                    .build();
            root.dispatchApplyWindowInsets(bars);
            assertEquals(48, root.getPaddingTop());
            assertEquals(64, root.getPaddingLeft());
            assertEquals(24, root.getPaddingBottom());
            root.dispatchApplyWindowInsets(keyboard);
            root.dispatchApplyWindowInsets(keyboard);
            assertEquals(280, root.getPaddingBottom());
            root.dispatchApplyWindowInsets(bars);
            assertEquals(24, root.getPaddingBottom());
        }
    }

    @Test
    @Config(sdk = 28)
    public void minimumSdkAlsoReservesSpaceForStatusBar() {
        try (ActivityController<SettingInfoActivity> controller = Robolectric.buildActivity(SettingInfoActivity.class).setup()) {
            ViewGroup content = controller.get().findViewById(android.R.id.content);
            View root = content.getChildAt(0);
            WindowInsetsCompat legacy = new WindowInsetsCompat.Builder()
                    .setSystemWindowInsets(Insets.of(0, 32, 0, 0)).build();
            ViewCompat.dispatchApplyWindowInsets(root, legacy);
            assertEquals(32, root.getPaddingTop());
        }
    }
}
