import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AlgorithmStuff {
    
    /**
     * Ex: aaaa = 3
     * abc xyz ova hdi abc = 3
     * 
     * @param str
     * @return
     */
    public static int findLongestRepeatingStr(String str) {
        if (str.length() < 3) {
            return 0;
        }
        
        int minChunkSize = 3;
        Map<String, Integer> candidates = new HashMap<>();
        // grab an incrementally increasing chunk and see if it gets repeated anywhere.
        for (int i = 0; i < str.length(); i++) {
            if (i+minChunkSize >= str.length()) {
                break; // no more chunks available
            }
            
            String chunk = str.substring(i, i+minChunkSize);
            String workingCopy = str;
            while (true) {
                int loc = workingCopy.indexOf(chunk);
                if (loc >= 0) {
                    if (candidates.containsKey(chunk)) {
                        int count = candidates.get(chunk);
                        candidates.put(chunk, (count+1));
                    } else {
                        candidates.put(chunk, 1);
                    }
                    // get rid of everything up to and including that chunk and then check again.
                    if (loc + chunk.length() >= workingCopy.length()) {
                        break; // nothing left to look for
                    } else {
                        workingCopy = workingCopy.substring(loc + chunk.length());
                    }
                } else {
                    break;
                }
            }
        }
        
        // see which candidate has the greatest count.
        int largest = 0;
        String winner = null;
        Set<Entry<String,Integer>> entrySet = candidates.entrySet();
        for (Entry<String, Integer> entry : entrySet) {
            String candidate = entry.getKey();
            if (candidate.length() > largest) {
                winner = candidate;
                largest = candidate.length();
            }
        }
        
        return winner.length();
    }
    
    public static int numMatchingChars(String secret, String guess) {
        // game of Jotto: guess a secret 5-letter English word with no repeated letters given a dictionary with valid words.
        // put secret chars into a TreeSet, see how many guess chars are in TreeSet
        return 0;
    }
    
    public static int findDoubleLargest(int[] arr) {
        // find the largest, then see if the largest is 2x larger than all others.
        return -1;
    }
    
    /**
     * Given a string s and a non-empty string p, find all the start indices of p's anagrams in s.
     * Strings consists of lowercase English letters only and the length of both strings s and p will not be larger than 20,100.
     * The order of output does not matter.
     * 
     * Example 1:
     * Input:
        s: "cbaebabacd" p: "abc"
            0123456789      012
        
        Output:
        [0, 6]
        
        Explanation:
        The substring with start index = 0 is "cba", which is an anagram of "abc".
        The substring with start index = 6 is "bac", which is an anagram of "abc".
     * 
     * Example 2:
     * Input:
        s: "abab" p: "ab"
        
        Output:
        [0, 1, 2]
        
        Explanation:
        The substring with start index = 0 is "ab", which is an anagram of "ab".
        The substring with start index = 1 is "ba", which is an anagram of "ab".
        The substring with start index = 2 is "ab", which is an anagram of "ab".
     * 
     * Example 3:
     * Input:
     * s: "ababababab" p: "aab"
     * 
     * Output:
     * [0, 2, 4, 6]
     * 
     * @param s
     * @param p
     * @return
     */
    public static List<Integer> findAnagrams(String s, String p) {
        List<Integer> results = new ArrayList<>();
        if (p.length() > s.length()) {
            return results;
        }
        
        List<Character> pChars = new ArrayList<>();
        char[] pArr = p.toCharArray();
        for (int i = 0; i < pArr.length; i++) {
            char pChar = pArr[i];
            pChars.add(pChar);
        }
        
        char[] sArr = s.toCharArray();
        int chunkSize = pArr.length;
        for (int i = 0; i < sArr.length; i++) {
            int endIndex = i+chunkSize;
            if ((endIndex-1) >= sArr.length) {
                break;
            }
            
            String chunk = s.substring(i, endIndex);
            List<Character> chunkChars = new ArrayList<>();
            for (int j = 0; j < chunk.length(); j++) {
                chunkChars.add(chunk.charAt(j));
            }

            boolean isAnagram = true;
            for (int k = 0; k < pArr.length; k++) {
                char pChar = pArr[k];
                if (!chunkChars.remove(Character.valueOf(pChar))) {
                    isAnagram = false;
                    break;
                }
            }
            if (isAnagram) {
                results.add(i);
            }
        }
        
        return results;
    }
    
    public static List<Integer> findAnagramsWithBitVector(String s, String p) {
        List<Integer> results = new ArrayList<>();
        if (p.length() > s.length()) {
            return results;
        }
        
        int pBits = 0;
        char[] pArr = p.toCharArray();
        for (int i = 0; i < pArr.length; i++) {
            int charAt = pArr[i] - 'a'; // this gives the exponent by which to raise with 'a' starting at 0.
            // 1 is a base multiple of 2, so we have 2^charAt. the result is 2^0=a, 2^1=b, ..., 2^25=z
            pBits |= (1 << charAt);
        }
        
        char[] sArr = s.toCharArray();
        int chunkSize = pArr.length;
        for (int i = 0; i < sArr.length; i++) {
            int endIndex = i+chunkSize;
            if ((endIndex-1) >= sArr.length) {
                break;
            }
            
            String chunk = s.substring(i, endIndex);
            int chunkBits = 0;
            for (int j = 0; j < chunk.length(); j++) {
                int chunkChar = chunk.charAt(j) - 'a';
                chunkBits |= (1 << chunkChar);
            }
            
            // if the chunk bits are the same as p's bits, then both have the same chars.
            if (chunkBits == pBits) {
                results.add(i);
            }
        }
        
        return results;
    }
    
    // runtime is always 64 (number of bits in a long) which is O(1) average
    public static int numSetBits(long A) {
        int count = 0;
        while (A > 0) {
            if ( (A & 1) != 0) {
                count++;
            }
            A >>= 1;
        }
        
        return count;
    }
    
    // A & (A-1) unsets the last set bit. runtime is O(M) where m is the number of bits that are set (worst case is 64).
    public static int numSetBits2(long A) {
        int totalOnes = 0;
        long z = A;
        while (z!=0) {
            totalOnes += 1;
            z = z & (z-1);
        }
        
        return totalOnes;
    }

    /**
     * Given properties containing hierarchical search terms separated by dots, return a map that indicates whether each search 
     * term has child search terms or not. Example:</br>
     * </br>
     * PSI.CCNum=...
     * PSI.SSN.GermanSSN=...
     * PSI.SSN.EnglishSSN=...</br>
     * </br>
     * PSI: true
     * CCNum or PSI.CCNum: false
     * SSN or PSI.SSN: true
     * GermanSSN: false
     * EnglishSSN: false
     * 
     * @param props
     * @return
     */
//    public Map<String, Boolean> searchTermHasChildMap(Properties props) {
//        
//    }
    
    public static int lengthOfLongestSubstring(String s) {
        int n = s.length();
        // this is our sliding window containing the longest possible substring w/o repeating chars.
        Set<Character> window = new HashSet<>();
        int ans = 0, i = 0, j = 0;
        while (i < n && j < n) {
            // try to extend the range of the sliding window by looking at the next char to see if it's unique.
            char next = s.charAt(j);
            if (window.add(next)){
                j += 1;
                ans = Math.max(ans, j - i);
            } else {
                // the next char already exists in our window, so remove it. this will create a new unique window.
                window.remove(s.charAt(i++));
            }
        }
        return ans;
    }
    
    /**
     * Given a string s, find the longest palindromic substring in s. You may assume that the maximum length of s is 1000.
     * 
     * Examples:
     * babad -> bab OR aba
     * cbbd -> bb
     * cbcd -> cbc
     * 
     * @param s
     * @return
     */
    public static String longestPalindrome(String s) {
        int n = s.length();
        // this is our sliding window containing the chars to test.
        StringBuilder window = new StringBuilder();
        
        String candidate = "";
        int start = 0, end = 0;
        while (start < n && end < n) {
            char next = s.charAt(end);
            window.append(next);
            // check if this is a palindrome by looping through from the start and from the end simultaneously.
            boolean windowIsPalindrome = true;
            for (int j = 0, k = window.length()-1; j < window.length() && k >= 0; j++, k--) {
                if (window.charAt(j) != window.charAt(k)) {
                    windowIsPalindrome = false;
                    break;
                }
            }

            if (windowIsPalindrome) {
                String p = window.toString();
                if (p.length() > candidate.length()) {
                    candidate = p;
                }
            }
            if (candidate.length() == n) {
                break;
            }
            // increase the window size.
            end++;
            if (end >= n) {
                // if we've covered all the characters, start building the window again sliding it forward.
                window = new StringBuilder();
                start++;
                end = start;
            }
        }
        return candidate;
    }
    
    public static void main(String[] args) {
        System.out.println(findLongestRepeatingStr("aaaa"));
        System.out.println(findLongestRepeatingStr("abc xyz ova hdi abc"));
        System.out.println(findAnagrams("cbaebabacd", "abc"));
        System.out.println(findAnagrams("abab", "ab"));
        System.out.println(findAnagrams("ababababab", "aab"));

        System.out.println("");
        System.out.println(findAnagramsWithBitVector("cbaebabacd", "abc"));
        System.out.println(findAnagramsWithBitVector("abab", "ab"));
        System.out.println(findAnagramsWithBitVector("ababababab", "aab"));
        
        System.out.println("numSetBits2(11)=" + numSetBits2(11L));
        System.out.println("numSetBits2(Long.MAX_VALUE)=" + numSetBits2(Long.MAX_VALUE));
        System.out.println();

        System.out.println("lengthOfLongestSubstring(abcabcbb)=" + lengthOfLongestSubstring("abcabcbb"));
        System.out.println("lengthOfLongestSubstring(dvdf)=" + lengthOfLongestSubstring("dvdf"));
        System.out.println("lengthOfLongestSubstring(bbbbb)=" + lengthOfLongestSubstring("bbbbb"));
        
        System.out.println();
        System.out.println("longestPalindrom(babad)=" + longestPalindrome("babad"));
        System.out.println("longestPalindrom(cbbd)=" + longestPalindrome("cbbd"));
        System.out.println("longestPalindrom(cbcd)=" + longestPalindrome("cbcd"));
        System.out.println("longestPalindrom(aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa)\n=" 
                            + longestPalindrome("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        System.out.println("longestPalindrom(abababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababa)\n=" 
                            + longestPalindrome("abababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababa"));
    }
}
