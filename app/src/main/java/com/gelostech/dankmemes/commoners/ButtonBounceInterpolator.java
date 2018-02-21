package com.gelostech.dankmemes.commoners;

/**
 * Created by tirgei on 6/19/17.
 */

public class ButtonBounceInterpolator implements android.view.animation.Interpolator {
    double amplitude = 1;
    double frequency = 5;

    public ButtonBounceInterpolator(double amplitude, double frequency){
        this.amplitude = amplitude;
        this.frequency = frequency;
    }

    @Override
    public float getInterpolation(float input) {
        return (float) (-1 * Math.pow(Math.E, -input/ amplitude) * Math.cos(frequency * input) + 1);
    }
}
