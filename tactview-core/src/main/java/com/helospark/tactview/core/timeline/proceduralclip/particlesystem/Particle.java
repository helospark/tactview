package com.helospark.tactview.core.timeline.proceduralclip.particlesystem;

public class Particle {
    double x, y;
    double xVel, yVel;
    double bornTime;
    double maxAge;

    public Particle cloneParticle() {
        Particle particle = new Particle();

        particle.x = x;
        particle.y = y;
        particle.xVel = xVel;
        particle.yVel = yVel;
        particle.bornTime = bornTime;
        particle.maxAge = maxAge;

        return particle;
    }
}
