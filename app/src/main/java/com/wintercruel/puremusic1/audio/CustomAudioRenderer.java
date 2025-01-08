package com.wintercruel.puremusic1.audio;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.audio.AudioRendererEventListener;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer;
import androidx.media3.exoplayer.mediacodec.MediaCodecAdapter;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;

@UnstableApi public class CustomAudioRenderer extends MediaCodecAudioRenderer {

    @OptIn(markerClass = UnstableApi.class) public CustomAudioRenderer(
            Context context,
            MediaCodecAdapter.Factory codecAdapterFactory,
            MediaCodecSelector mediaCodecSelector,
            boolean enableDecoderFallback,
            Handler eventHandler,
            AudioRendererEventListener eventListener,
            AudioSink audioSink) {
        super(
                context,
                codecAdapterFactory,
                mediaCodecSelector,
                enableDecoderFallback,
                eventHandler,
                eventListener,
                audioSink
        );
    }
}

