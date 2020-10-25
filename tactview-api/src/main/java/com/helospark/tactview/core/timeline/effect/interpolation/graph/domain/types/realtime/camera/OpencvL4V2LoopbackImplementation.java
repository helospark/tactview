package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime.camera;

import com.helospark.lightdi.annotation.ConditionalOnProperty;
import com.helospark.tactview.core.util.conditional.ConditionalOnPlatform;
import com.helospark.tactview.core.util.conditional.TactviewPlatform;
import com.helospark.tactview.core.util.jpaplugin.NativeImplementation;
import com.sun.jna.Library;

@ConditionalOnPlatform(TactviewPlatform.LINUX)
@ConditionalOnProperty(property = "tactview.realtime", havingValue = "true")
@NativeImplementation("v4l2loopback")
public interface OpencvL4V2LoopbackImplementation extends Library {

    void sendImageToLoopbackCamera(ImageToLoopbackRequest request);

}
