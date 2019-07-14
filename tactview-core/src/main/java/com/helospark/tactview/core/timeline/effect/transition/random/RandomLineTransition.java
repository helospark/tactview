package com.helospark.tactview.core.timeline.effect.transition.random;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class RandomLineTransition extends AbstractVideoTransitionEffect {
    private ShuffledNumberService shuffledNumberService;
    private int seed;

    public RandomLineTransition(TimelineInterval interval, ShuffledNumberService shuffledNumberService) {
        super(interval);
        seed = new Random().nextInt();
        this.shuffledNumberService = shuffledNumberService;
    }

    public RandomLineTransition(RandomLineTransition randomLineTransition, CloneRequestMetadata cloneRequestMetadata) {
        super(randomLineTransition, cloneRequestMetadata);
        this.shuffledNumberService = randomLineTransition.shuffledNumberService;
        this.seed = randomLineTransition.seed;
    }

    public RandomLineTransition(JsonNode node, LoadMetadata loadMetadata, ShuffledNumberService shuffledNumberService) {
        super(node, loadMetadata);
        this.shuffledNumberService = shuffledNumberService;
        // Maybe save and load seed
    }

    @Override
    protected ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest transitionRequest) {
        double progress = transitionRequest.getProgress();
        int width = transitionRequest.getFirstFrame().getWidth();
        int height = transitionRequest.getFirstFrame().getHeight();
        List<Integer> lines = shuffledNumberService.shuffledNumbers(height, seed);

        int endIndex = (int) (progress * lines.size());

        ClipImage result = ClipImage.fromSize(width, height);

        for (int i = 0; i < endIndex; ++i) {
            copyLineToResult(result, transitionRequest.getSecondFrame(), lines.get(i));
        }
        for (int i = endIndex; i < lines.size(); ++i) {
            copyLineToResult(result, transitionRequest.getFirstFrame(), lines.get(i));
        }

        return result;
    }

    private void copyLineToResult(ClipImage result, ReadOnlyClipImage firstFrame, Integer y) {
        for (int x = 0; x < firstFrame.getWidth(); ++x) {
            int r = firstFrame.getRed(x, y);
            int g = firstFrame.getGreen(x, y);
            int b = firstFrame.getBlue(x, y);
            int a = firstFrame.getAlpha(x, y);

            result.setRed(r, x, y);
            result.setGreen(g, x, y);
            result.setBlue(b, x, y);
            result.setAlpha(a, x, y);
        }
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new RandomLineTransition(this, cloneRequestMetadata);
    }

}
