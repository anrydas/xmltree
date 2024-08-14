package das.tools.gui.search;

import java.util.Arrays;

public class LevenshteinDistance {
    public static int calculateRecursive(String s1, String s2) {
        if (s1.isEmpty()) {
            return s2.length();
        }

        if (s2.isEmpty()) {
            return s1.length();
        }

        int sub = calculateRecursive(s1.substring(1),
                s2.substring(1)) + substCost(s1.charAt(0), s2.charAt(0));
        int ins = calculateRecursive(s1, s2.substring(1)) + 1;
        int del = calculateRecursive(s1.substring(1), s2) + 1;

        return min(sub, ins, del);
    }

    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    public static int substCost(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static int calculateDynamic(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + substCost(s1.charAt(i - 1), s2.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
