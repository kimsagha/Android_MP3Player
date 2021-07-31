package com.example.mp3player;

public interface ICallback {
        void mp3PlayerEvent(int progressMin, int progressSec, int durationMin, int durationSec);
}
