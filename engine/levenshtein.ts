/**
 * Calculate Levenshtein distance between two strings.
 * Uses O(min(m,n)) space with single-row DP instead of full 2D matrix.
 */
export function levenshteinDistance(a: string, b: string): number {
  // Ensure a is the shorter string to minimize space usage
  if (a.length > b.length) {
    [a, b] = [b, a];
  }

  const m = a.length;
  const n = b.length;

  // Previous row of distances
  let prev = new Array(m + 1);
  for (let i = 0; i <= m; i++) prev[i] = i;

  for (let j = 1; j <= n; j++) {
    const curr = new Array(m + 1);
    curr[0] = j;
    for (let i = 1; i <= m; i++) {
      if (a[i - 1] === b[j - 1]) {
        curr[i] = prev[i - 1];
      } else {
        curr[i] = 1 + Math.min(prev[i], curr[i - 1], prev[i - 1]);
      }
    }
    prev = curr;
  }

  return prev[m];
}

/**
 * Normalized Levenshtein score (0-1, where 1 = perfect match)
 */
export function levenshteinScore(a: string, b: string): number {
  const maxLen = Math.max(a.length, b.length);
  if (maxLen === 0) return 1;
  return 1 - levenshteinDistance(a, b) / maxLen;
}
