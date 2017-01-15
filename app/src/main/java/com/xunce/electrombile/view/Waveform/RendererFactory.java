package com.xunce.electrombile.view.Waveform;

import android.support.annotation.ColorInt;

/**
 * Created by yangxu on 2017/1/15.
 */

public class RendererFactory {
    public WaveformRender createSimpleWaveformRender(@ColorInt int foreground, @ColorInt int background) {
        return SimpleWaveformRenderer.newInstance(background, foreground);
    }
}
