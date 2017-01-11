package fr.onagui.alignment.method;


public class LevenshteinDistance {
	
	private static int min3(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
 
    /** Compute the Levenshtein distance.<br/>
     * Source from Wikipedia:<a href="http://en.wikibooks.org/wiki/Algorithm_implementation/Strings/Levenshtein_distance#Java">here</a>
     * @param str1 The first String.
     * @param str2 The second String.
     * @return The levenshtein distance.
     */
    public static int computeLevenshteinDistance(char[] str1, char[] str2) {
        int[][] distance = new int[str1.length+1][str2.length+1];
 
        for (int i=0; i<=str1.length; i++)
            distance[i][0] = i;
        for (int j=0; j<=str2.length; j++)
            distance[0][j]=j;
 
        for (int i=1; i<=str1.length; i++)
            for (int j=1;j<=str2.length; j++)
                  distance[i][j]= min3(distance[i-1][j]+1, distance[i][j-1]+1, 
                                       distance[i-1][j-1]+((str1[i-1]==str2[j-1])?0:1));
 
        return distance[str1.length][str2.length];
    }
    
    /** Compute the normalized levenshtein distance.<br/>
     * The result is defined in the set [0, 1].
     * The normalization in the sum of the two array length.
     * @param str1 The first string.
     * @param str2 The second string.
     * @return The normalized levenshtein distance.
     */
    public static double computeNormalizedLevenshteinDistance(char[] str1, char[] str2) {
    	return (double)computeLevenshteinDistance(str1, str2) / (double)(str1.length + str2.length); 
    }	
}
