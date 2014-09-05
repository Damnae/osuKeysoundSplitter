package com.damnae.osukeysoundsplitter;

public class TimingPoint {
	public long time;
	public double secondValue;
	public int beatPerMeasure;
	public int sampleType;
	public int sampleSet;
	public int volume;
	public boolean isInherited;
	public boolean isKiai;

	public double prevousBeatDuration;

	public double getBeatDuration() {
		if (isInherited)
			return prevousBeatDuration;
		return secondValue;
	}

	public TimingPoint createInherited(long startTime) {
		TimingPoint timingPoint = new TimingPoint();
		timingPoint.time = startTime;
		timingPoint.secondValue = isInherited ? secondValue : -100;
		timingPoint.beatPerMeasure = beatPerMeasure;
		timingPoint.sampleType = sampleType;
		timingPoint.sampleSet = sampleSet;
		timingPoint.volume = volume;
		timingPoint.isInherited = true;
		timingPoint.isKiai = isKiai;
		timingPoint.prevousBeatDuration = isInherited ? prevousBeatDuration
				: secondValue;
		return timingPoint;
	}

	public double getMultiplier() {
		if (isInherited)
			return -(secondValue / 100.0);
		return 1;
	}

	public boolean isSimilar(TimingPoint previousTimingPoint) {
		if (getBeatDuration() != previousTimingPoint.getBeatDuration()
				|| getMultiplier() != previousTimingPoint.getMultiplier()
				|| beatPerMeasure != previousTimingPoint.beatPerMeasure
				|| sampleType != previousTimingPoint.sampleType
				|| sampleSet != previousTimingPoint.sampleSet
				|| volume != previousTimingPoint.volume) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(time);
		sb.append(",");
		sb.append(secondValue);
		sb.append(",");
		sb.append(beatPerMeasure);
		sb.append(",");
		sb.append(sampleType);
		sb.append(",");
		sb.append(sampleSet);
		sb.append(",");
		sb.append(volume);
		sb.append(",");
		sb.append(isInherited ? 0 : 1);
		sb.append(",");
		sb.append(isKiai ? 1 : 0);
		return sb.toString();
	}
}