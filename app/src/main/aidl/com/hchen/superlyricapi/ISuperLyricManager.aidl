package com.hchen.superlyricapi;
import com.hchen.superlyricapi.SuperLyricData;
interface ISuperLyricManager {
    void registerPublisher();
    void unregisterPublisher();
    boolean isPublisherRegistered();
    void sendLyric(in SuperLyricData data);
    void sendStop(in SuperLyricData data);
}