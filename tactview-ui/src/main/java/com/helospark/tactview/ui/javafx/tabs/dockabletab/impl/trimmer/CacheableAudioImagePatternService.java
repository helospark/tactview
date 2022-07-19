package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.trimmer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.AudioImagePatternService;

import javafx.scene.image.Image;

@Component
public class CacheableAudioImagePatternService {
    private AudioImagePatternService audioImagePatternService;

    Cache<Object, Object> coffeinCache = Caffeine.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(1_000_000, TimeUnit.MILLISECONDS)
            .build();

    public CacheableAudioImagePatternService(AudioImagePatternService audioImagePatternService) {
        this.audioImagePatternService = audioImagePatternService;
    }

    public Image createAudioImagePattern(AudibleTimelineClip audibleTimelineClip, int width, int height, double visibleStartPosition, double visibleEndPosition) {

        AudioImagePatternCache cacheKey = new AudioImagePatternCache(audibleTimelineClip, width, height, visibleStartPosition, visibleEndPosition);

        return (Image) coffeinCache.get(cacheKey, cacheKey2 -> {
            return audioImagePatternService.createAudioImagePattern(audibleTimelineClip, width, height, visibleStartPosition, visibleEndPosition);
        });

    }

    static class AudioImagePatternCache {
        AudibleTimelineClip audibleTimelineClip;
        int width;
        int height;
        double visibleStartPosition;
        double visibleEndPosition;

        public AudioImagePatternCache(AudibleTimelineClip audibleTimelineClip, int width, int height, double visibleStartPosition, double visibleEndPosition) {
            this.audibleTimelineClip = audibleTimelineClip;
            this.width = width;
            this.height = height;
            this.visibleStartPosition = visibleStartPosition;
            this.visibleEndPosition = visibleEndPosition;
        }

        @Override
        public int hashCode() {
            return Objects.hash(audibleTimelineClip.getId(), height, visibleEndPosition, visibleStartPosition, width);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AudioImagePatternCache other = (AudioImagePatternCache) obj;
            return Objects.equals(audibleTimelineClip.getId(), other.audibleTimelineClip.getId()) && height == other.height
                    && Double.doubleToLongBits(visibleEndPosition) == Double.doubleToLongBits(other.visibleEndPosition)
                    && Double.doubleToLongBits(visibleStartPosition) == Double.doubleToLongBits(other.visibleStartPosition) && width == other.width;
        }

    }
}
