
#ifndef EX2_EDIT_DISTANCE_H
#define EX2_EDIT_DISTANCE_H

/**
 * @brief Computes the edit distance between two strings using a recursive approach.
 *
 * The edit distance represents the minimum number of operations
 * required to transform one string into the other.
 *
 * @param s1 First string.
 * @param s2 Second string.
 * @return Edit distance between s1 and s2.
 */
int edit_distance(const char *s1, const char *s2);


/**
 * @brief Computes the edit distance between two strings using dynamic programming.
 *
 * This version is optimized using a memoization table
 * to reduce repeated calculations compared to the recursive version.
 *
 * @param s1 First string.
 * @param s2 Second string.
 * @return Dynamic edit distance between s1 and s2.
 */
int edit_distance_dyn(const char *s1, const char *s2);

#endif //EX2_EDIT_DISTANCE_H
