package com.skcraft.launcher.swing;

import javax.swing.*;

/**
 * @author dags <dags@dags.me>
 */
public class FloatSlider extends JSlider {

    private final float min;
    private final float max;

    public FloatSlider(float min, float max) {
        super(new DefaultBoundedRangeModel(0, 0, 0, 1000));
        this.min = min;
        this.max = max;
    }

    @Override
    public void setValue(int value) {
        super.setValue(value);
    }

    public float getFloatValue() {
        return map(getValue());
    }

    public void setValue(float value) {
        int mapped = map(value);
        setValue(mapped);
    }

    private float map(int value) {
        BoundedRangeModel model = getModel();
        float modelRange = model.getMaximum() - model.getMinimum();
        float modelProg = (value - model.getMinimum()) / modelRange;
        float range = max - min;
        return min + (modelProg * range);
    }

    private int map(float value) {
        float range = max - min;
        float prog = (value - min) / range;
        BoundedRangeModel model = getModel();
        int modelRange = model.getMaximum() - model.getMinimum();
        return model.getMinimum() + Math.round(prog * modelRange);
    }
}
