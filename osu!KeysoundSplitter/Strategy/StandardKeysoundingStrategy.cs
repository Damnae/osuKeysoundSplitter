using osuKeysoundSplitter.Audio.Encode;
using osuKeysoundSplitter.PathProvider;
using System.Collections.Generic;
using System.IO;
namespace osuKeysoundSplitter.Strategy
{
    public class StandardKeysoundingStrategy : BaseKeysoundingStrategy
    {
        private AudioEncoder silentAudioEncoder = new WavAudioEncoder();

        public StandardKeysoundingStrategy(File mapsetFolder,
                KeysoundCache keysoundCache, AudioEncoder audioEncoder,
                int initialSampleType)
            : base(mapsetFolder, new HitnormalKeysoundPathProvider(keysoundCache,
                initialSampleType), audioEncoder)
        {
        }

        public override string rewriteKeysoundData(Keysound keysound, string keysoundData,
                int volume)
        {

            string[] values = keysoundData.Split(',');
            int flags = int.Parse(values[3]);

            if (Utils.isNoteOrCircle(flags))
            {
                string[] hitsoundValues = Utils.splitValues(values[5], ':');
                hitsoundValues[0] = HitnormalKeysoundPathProvider
                        .getSampleType(keysound.filename);
                hitsoundValues[1] = "0";
                hitsoundValues[2] = HitnormalKeysoundPathProvider
                        .getSampleSet(keysound.filename);
                hitsoundValues[3] = volume.ToString();
                hitsoundValues[4] = "";

                values[4] = "0";
                values[5] = Utils.joinValues(hitsoundValues, ":");
                keysoundData = Utils.joinValues(values, ",");

            }
            else if (Utils.isLongNote(flags))
            {
                string[] lnValues = Utils.splitValues(values[5], ':');
                lnValues[1] = HitnormalKeysoundPathProvider
                        .getSampleType(keysound.filename);
                lnValues[2] = "0";
                lnValues[3] = HitnormalKeysoundPathProvider
                        .getSampleSet(keysound.filename);
                lnValues[4] = volume.ToString();
                lnValues[5] = "";

                values[4] = "0";
                values[5] = Utils.joinValues(lnValues, ":");
                keysoundData = Utils.joinValues(values, ",");

            }
            else if (Utils.isSlider(flags))
            {
                if (values.Length < 10)
                    values = Arrays.copyOf(values, 10);

                int nodeCount = int.Parse(values[6]) + 1;

                string[] additionValues = new string[nodeCount];
                for (int i = 0; i < nodeCount; ++i)
                    additionValues[i] = "0";

                string[] sampleTypeValues = new string[nodeCount];
                for (int i = 0; i < nodeCount; ++i)
                    sampleTypeValues[i] = "0:0";

                values[8] = Utils.joinValues(additionValues, "|");
                values[9] = Utils.joinValues(sampleTypeValues, "|");
                keysoundData = Utils.joinValues(values, ",");

            }
            else if (Utils.isSpinner(flags))
            {
                string[] hitsoundValues = Utils.splitValues(values[6], ':');
                hitsoundValues[0] = "0";
                hitsoundValues[1] = "0";
                hitsoundValues[2] = "0";
                hitsoundValues[3] = "0";
                hitsoundValues[4] = "";

                values[4] = "0";
                values[6] = Utils.joinValues(hitsoundValues, ":");
                keysoundData = Utils.joinValues(values, ",");
            }

            return keysoundData;
        }

        public override List<string> rewriteTimingPoints(List<TimingPoint> timingPoints,
                List<Keysound> keysounds)
        {

            removeKeysounding(timingPoints);
            insertKeysounds(timingPoints, keysounds);

            return TimingPoint.buildTimingPointLines(timingPoints);
        }

        private void removeKeysounding(List<TimingPoint> timingPoints)
        {
            foreach (TimingPoint timingPoint in timingPoints)
            {
                timingPoint.sampleType = 2;
                timingPoint.sampleSet = 0;
                timingPoint.volume = 100;
            }
            TimingPoint.simplifyTimingPoints(timingPoints);
        }

        private void insertKeysounds(List<TimingPoint> timingPoints,
                List<Keysound> keysounds)
        {

            for (int i = 0, size = keysounds.Count; i < size; ++i)
            {
                Keysound keysound = keysounds[i];

                bool muteBody = keysound.type == Keysound.Type.LINE;
                if (keysound.type == Keysound.Type.HITOBJECT)
                {
                    string[] keysoundDataLines = keysound.data.split("\n");
                    if (keysoundDataLines.Length > 1)
                        continue;

                    string keysoundData = keysoundDataLines[0];
                    string[] values = keysoundData.Split(',');
                    int flags = int.Parse(values[3]);

                    if (!Utils.isSlider(flags) && !Utils.isSpinner(flags))
                        continue;

                    muteBody = Utils.isSlider(flags);

                }
                else if (keysound.type != Keysound.Type.LINE)
                {
                    continue;
                }

                bool makeSilentSlide = muteBody;

                // Only mute hitsounds inside sliders
                muteBody = muteBody && i < size - 1
                        && keysounds[i + 1].type == Keysound.Type.LINE;

                long startTime = keysound.startTime;
                long endTime = keysound.endTime;

                // Reset normal timing point values
                TimingPoint.getOrCreateTimingPoint(timingPoints, endTime);

                // Set head sampleset and volume
                TimingPoint timingPoint = TimingPoint.getOrCreateTimingPoint(
                        timingPoints, startTime);
                timingPoint.sampleType = HitnormalKeysoundPathProvider
                        .getSampleType(keysound.filename);
                timingPoint.sampleSet = HitnormalKeysoundPathProvider
                        .getSampleSet(keysound.filename);
                timingPoint.volume = 100;

                if (muteBody)
                {
                    // Silence slider ticks
                    TimingPoint silentTimingPoint = TimingPoint
                            .getOrCreateTimingPoint(timingPoints, startTime + 10);
                    silentTimingPoint.volume = 5;
                }

                if (makeSilentSlide)
                {
                    // Make silent slider slide sample
                    string sliderSliderPath = HitnormalKeysoundPathProvider
                            .getSampleTypeName(timingPoint.sampleType)
                            + "-sliderslide"
                            + timingPoint.sampleSet
                            + "."
                            + silentAudioEncoder.getExtension();

                    File silentFile = new File(getMapsetFolder(), sliderSliderPath);
                    if (!silentFile.Exists())
                    {
                        try
                        {
                            silentAudioEncoder.encodeSilence(silentFile);

                        }
                        catch (IOException e)
                        {
                            throw new Exception(e);
                        }
                    }
                }
            }
            TimingPoint.simplifyTimingPoints(timingPoints);
        }
    }
}