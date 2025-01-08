package com.wintercruel.puremusic1.audio;

import android.content.Context;
import android.os.Handler;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.audio.AudioRendererEventListener;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.mediacodec.MediaCodecAdapter;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;

import java.util.ArrayList;

@UnstableApi
public class CustomRenderersFactory extends DefaultRenderersFactory {



    public CustomRenderersFactory(Context context) {
        super(context);

    }

    @Override
    protected void buildAudioRenderers(
            Context context,
            @ExtensionRendererMode int extensionRendererMode,
            MediaCodecSelector mediaCodecSelector,
            boolean enableDecoderFallback,
            AudioSink audioSink,
            Handler eventHandler,
            AudioRendererEventListener eventListener,
            ArrayList<Renderer> out) {

        // 创建自定义 AudioRenderer
        out.add(new CustomAudioRenderer(
                context,
                MediaCodecAdapter.Factory.DEFAULT,
                mediaCodecSelector,
                enableDecoderFallback,
                eventHandler,
                eventListener,
                CustomAudioSink.createAudioSink())); // 传递 AudioVisualizerView

        // 保留默认行为
        super.buildAudioRenderers(
                context,
                extensionRendererMode,
                mediaCodecSelector,
                enableDecoderFallback,
                audioSink,
                eventHandler,
                eventListener,
                out);
    }
}


