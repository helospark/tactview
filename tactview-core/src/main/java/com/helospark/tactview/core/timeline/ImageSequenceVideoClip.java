package com.helospark.tactview.core.timeline;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileHolder;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileNamePatternToFileResolverService;

public class ImageSequenceVideoClip extends VideoClip {
    private FileNamePatternToFileResolverService fileNamePatternService;

    public ImageSequenceVideoClip(ImageSequenceVideoClip clip, CloneRequestMetadata cloneRequestMetadata) {
        super(clip, cloneRequestMetadata);
        this.fileNamePatternService = clip.fileNamePatternService;
    }

    public ImageSequenceVideoClip(VisualMediaMetadata metadata, VisualMediaSource videoSource, JsonNode savedClip, LoadMetadata loadMetadata,
            FileNamePatternToFileResolverService fileNamePatternService) {
        super(metadata, videoSource, savedClip, loadMetadata);
        this.fileNamePatternService = fileNamePatternService;
    }

    public ImageSequenceVideoClip(VisualMediaMetadata mediaMetadata, VisualMediaSource backingSource, TimelinePosition startPosition, TimelineLength length,
            FileNamePatternToFileResolverService fileNamePatternService) {
        super(mediaMetadata, backingSource, startPosition, length);
        this.fileNamePatternService = fileNamePatternService;
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent, SaveMetadata saveMetadata) {
        super.generateSavedContentInternal(savedContent, saveMetadata);
        savedContent.put("fps", ((VideoMetadata) mediaMetadata).getFps());

        if (saveMetadata.isPackageAllContent()) {
            List<FileHolder> files = fileNamePatternService.filenamePatternToFileResolver(backingSource.getBackingFile());

            String path = "data/" + getId() + "/";
            for (var file : files) {
                saveMetadata.getFilesToCopy().put(path + file.getFile().getName(), file.getFile().getAbsolutePath());
            }

            String filePattern = fileNamePatternService.resolveFilePattern(backingSource.getBackingFile());
            savedContent.put("backingFile", SaveMetadata.LOCALLY_SAVED_SOURCE_PREFIX + path + filePattern);
        } // else saved by parent
    }
}
