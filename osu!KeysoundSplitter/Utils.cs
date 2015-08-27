using System.IO;
using System.Text;

namespace osuKeysoundSplitter
{
    public class Utils
    {
        public static string[] splitValues(string value, char separator)
        {
            int[] indexes = new int[value.Length];
            int indexCount = 0;
            for (int i = 0, size = value.Length; i < size; ++i)
            {
                char c = value[i];
                if (c == separator)
                {
                    indexes[indexCount] = i;
                    ++indexCount;
                }
            }

            string[] values = new string[indexCount + 1];
            values[0] = value.Substring(0, indexes[0]);
            for (int i = 1; i < indexCount; ++i)
            {
                values[i] = value.Substring(indexes[i - 1] + 1, indexes[i]);
            }
            values[values.Length - 1] = value.Substring(
                    indexes[indexCount - 1] + 1, value.Length);

            return values;
        }

        public static string joinValues(string[] hitsoundValues, string separator)
        {
            StringBuilder sb = new StringBuilder();
            foreach (string hitsoundValue in hitsoundValues)
            {
                if (sb.Length > 0)
                    sb.Append(separator);
                sb.Append(hitsoundValue);
            }
            return sb.ToString();
        }

        public static string parseKeyValueKey(string line)
        {
            return line.Substring(0, line.IndexOf(":")).Trim();
        }

        public static string parseKeyValueValue(string line)
        {
            return line.Substring(line.IndexOf(":") + 1, line.Length).Trim();
        }

        public static bool isNoteOrCircle(int flags)
        {
            return (flags & 1) != 0;
        }

        public static bool isLongNote(int flags)
        {
            return (flags & 128) != 0;
        }

        public static bool isSlider(int flags)
        {
            return (flags & 2) != 0;
        }

        public static bool isSpinner(int flags)
        {
            return (flags & 8) != 0;
        }
    }
}