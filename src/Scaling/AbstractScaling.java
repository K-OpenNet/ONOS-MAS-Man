package Scaling;

abstract class AbstractScaling implements Scaling{
    protected scalingType scalingName;

    public scalingType getScalingName() {
        return scalingName;
    }

    public void setScalingName(scalingType scalingName) {
        this.scalingName = scalingName;
    }
}
