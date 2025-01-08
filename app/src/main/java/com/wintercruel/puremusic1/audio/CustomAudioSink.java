package com.wintercruel.puremusic1.audio;

import androidx.annotation.OptIn;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.audio.DefaultAudioSink;


@UnstableApi
public class CustomAudioSink {
    private static PcmDataProcessor pcmProcessor;

    @OptIn(markerClass = UnstableApi.class)
    public static AudioSink createAudioSink() {
        pcmProcessor = new PcmDataProcessor(null); // 初始时不绑定 AudioVisualizerView
        return new DefaultAudioSink.Builder()
                .setAudioProcessors(new AudioProcessor[]{pcmProcessor})
                .build();
    }

    public static void bindVisualizerView(AudioVisualizerView visualizerView) {
        if (pcmProcessor != null) {
            pcmProcessor.bindVisualizerView(visualizerView);
        }
    }
}


