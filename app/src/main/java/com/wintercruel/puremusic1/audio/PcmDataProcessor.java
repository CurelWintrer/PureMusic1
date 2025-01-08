package com.wintercruel.puremusic1.audio;


import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import org.jtransforms.fft.FloatFFT_1D;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@UnstableApi
public class PcmDataProcessor implements AudioProcessor {
    private ByteBuffer outputBuffer = EMPTY_BUFFER;

    private BlockingQueue<byte[]> pcmQueue = new LinkedBlockingQueue<>();
//    private AudioVisualizerView audioVisualizerView;

    private long lastUpdateTime = 0;
    private static final long MIN_UPDATE_INTERVAL_MS = 20; // 每50ms更新一次频谱

    private static final int SMOOTHING_WINDOW_SIZE = 10; // 平滑窗口大小
    private float[] smoothedMagnitudes = new float[0]; // 存储平滑后的频谱

    private static final float MAX_MAGNITUDE = 50.0f; // 最大频谱幅度，您可以根据需求调整

    private volatile AudioVisualizerView audioVisualizerView; // 使用动态绑定的方式

    // 新增一个绑定方法
    public void bindVisualizerView(AudioVisualizerView visualizerView) {
        this.audioVisualizerView = visualizerView;
    }


    public PcmDataProcessor(AudioVisualizerView audioVisualizerView){
        this.audioVisualizerView= audioVisualizerView;
        startProcessingPCM();
    }


    @Override
    public AudioFormat configure(AudioFormat inputAudioFormat) throws UnhandledAudioFormatException {
        return inputAudioFormat;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void queueInput(ByteBuffer inputBuffer) {
        byte[] pcmData = new byte[inputBuffer.remaining()];
        inputBuffer.get(pcmData);
//        Log.d("PCMDATA", Arrays.toString(pcmData));

        outputBuffer = ByteBuffer.wrap(pcmData); // 正确传递 PCM 数据

        // 添加到队列用于异步处理
        pcmQueue.offer(pcmData);

        // 同时传递给播放缓冲区
        outputBuffer = ByteBuffer.wrap(pcmData);

        if (audioVisualizerView != null) {
            float[] magnitudes = calculateFFT(pcmData); // 计算 FFT


            if (magnitudes.length != 0) {
                magnitudes = smoothData(magnitudes);
                float[] finalMagnitudes = magnitudes;

                audioVisualizerView.post(() -> audioVisualizerView.updateFrequencies(finalMagnitudes));
            }


        }

    }

    // 在独立线程中处理 PCM 数据
    private void startProcessingPCM() {
        new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    byte[] pcmData = pcmQueue.take();
                    if (pcmData == null || pcmData.length == 0) {
//                        Log.e("PcmDataProcessor", "Skipped processing due to empty PCM data.");
                        continue; // 跳过无效数据
                    }

                    float[] magnitudes = calculateFFT(pcmData);

                //    Log.d("数据：", Arrays.toString(magnitudes));

                    if (magnitudes.length == 0) {
//                        Log.e("PcmDataProcessor", "Skipped processing due to empty FFT results.");
                        continue; // 跳过无效结果
                    }

                    magnitudes = smoothData(magnitudes);

//                    Log.d("数据：", Arrays.toString(magnitudes));

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime > MIN_UPDATE_INTERVAL_MS) {
                        if (audioVisualizerView != null) {
               //             float[] finalMagnitudes = applyMagnitudeLimit(magnitudes, MAX_MAGNITUDE);
                            float[] finalMagnitudes=magnitudes;
             //               Log.d("数据：", Arrays.toString(finalMagnitudes));
                            audioVisualizerView.post(() -> audioVisualizerView.updateFrequencies(finalMagnitudes));
                        }
                        lastUpdateTime = currentTime;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }


    // 限制频谱幅度，避免频谱值过高
    private float[] applyMagnitudeLimit(float[] magnitudes, float maxMagnitude) {
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = Math.abs(magnitudes[i]); // 取绝对值
            if (magnitudes[i] > maxMagnitude) {
                magnitudes[i] = magnitudes[i]-40.0f; // 限制幅度
            }
        }
        return magnitudes;
    }


    private float[] smoothData(float[] magnitudes) {
        if (magnitudes == null || magnitudes.length == 0) {
//            Log.e("PcmDataProcessor", "Received empty magnitudes for smoothing.");
            return new float[0]; // 返回空数组
        }

        if (smoothedMagnitudes.length != magnitudes.length) {
            smoothedMagnitudes = new float[magnitudes.length]; // 动态调整大小
            Arrays.fill(smoothedMagnitudes, 0f);
        }

        for (int i = 0; i < magnitudes.length; i++) {
            smoothedMagnitudes[i] = (smoothedMagnitudes[i] * (SMOOTHING_WINDOW_SIZE - 1) + magnitudes[i]) / SMOOTHING_WINDOW_SIZE;
        }

        return smoothedMagnitudes;
    }



    @Override
    public void queueEndOfStream() {
        outputBuffer = EMPTY_BUFFER;
    }

//    @Override
//    public ByteBuffer getOutput() {
//        return outputBuffer;
//    }

    @Override
    public ByteBuffer getOutput() {
        ByteBuffer output = outputBuffer;
        outputBuffer = EMPTY_BUFFER; // 清空缓冲区以防止重复读取
        return output;
    }


    @Override
    public boolean isEnded() {
        return false;
    }

    @Override
    public void flush() {
        outputBuffer = EMPTY_BUFFER;
    }

    @Override
    public void reset() {
        flush();
    }

    // 假设 `pcmData` 是获取的 PCM 数据，`pcmData.length` 为数据长度
    public float[] calculateFFT(byte[] pcmData) {
        if (pcmData == null || pcmData.length <= 0) {
//            Log.e("FFT", "Invalid PCM data: length is 0 or null.");
            return new float[0]; // 返回空数组
        }

        int length = pcmData.length / 2; // 每两个字节是一个 16-bit 的采样值
        if (length <= 0) {
//            Log.e("FFT", "Invalid PCM data: insufficient length.");
            return new float[0]; // 返回空数组
        }

        float[] samples = new float[length];
        for (int i = 0; i < length; i++) {
            samples[i] = ((pcmData[i * 2 + 1] << 8) | (pcmData[i * 2] & 0xFF)) / 32768.0f;
        }

        FloatFFT_1D fft = new FloatFFT_1D(samples.length);
        fft.realForward(samples);

        float[] magnitudes = new float[samples.length / 2];
        for (int i = 0; i < magnitudes.length; i++) {
            float real = samples[2 * i];
            float imag = samples[2 * i + 1];
            magnitudes[i] = (float) Math.sqrt(real * real + imag * imag);
        }

        return magnitudes;
    }



}
