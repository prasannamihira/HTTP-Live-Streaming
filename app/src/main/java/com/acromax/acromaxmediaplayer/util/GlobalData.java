package com.acromax.acromaxmediaplayer.util;

import com.acromax.acromaxmediaplayer.model.AudioChunk;

/**
 * Created by Mihira on 6/30/2018.
 */
public class GlobalData {


    public static AudioChunk audioChunk1;
    public static AudioChunk audioChunk2;
    public int audioLength;

    private static GlobalData globalData;

    private GlobalData() {
    }

    public static synchronized GlobalData getInstance() {
        if (globalData == null) {
            globalData = new GlobalData();

            audioChunk1 = new AudioChunk();
            audioChunk2 = new AudioChunk();

        }
        return globalData;
    }

    public static AudioChunk getAudioChunk1() {
        return audioChunk1;
    }

    public static void setAudioChunk1(AudioChunk audioChunk1) {
        GlobalData.audioChunk1 = audioChunk1;
    }

    public static AudioChunk getAudioChunk2() {
        return audioChunk2;
    }

    public static void setAudioChunk2(AudioChunk audioChunk2) {
        GlobalData.audioChunk2 = audioChunk2;
    }

    public int getAudioLength() {
        return audioLength;
    }

    public void setAudioLength(int audioLength) {
        this.audioLength = audioLength;
    }
}
