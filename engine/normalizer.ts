const GERMAN_ARTICLES = ['der', 'die', 'das', 'ein', 'eine', 'einem', 'einen', 'einer', 'eines'];

/**
 * Normalize text for comparison:
 * - lowercase
 * - trim whitespace
 * - strip German articles
 * - remove punctuation
 * - normalize German umlauts for comparison
 */
export function normalize(text: string): string {
  let result = text.toLowerCase().trim();

  // Remove punctuation
  result = result.replace(/[.,!?;:'"()\-]/g, '');

  // Strip leading German articles
  for (const article of GERMAN_ARTICLES) {
    if (result.startsWith(article + ' ')) {
      result = result.substring(article.length + 1).trim();
      break;
    }
  }

  // Trim again
  result = result.trim();

  return result;
}

/**
 * Normalize umlauts to ASCII equivalents for loose comparison
 */
export function normalizeUmlauts(text: string): string {
  return text
    .replace(/ä/g, 'ae')
    .replace(/ö/g, 'oe')
    .replace(/ü/g, 'ue')
    .replace(/ß/g, 'ss')
    .replace(/Ä/g, 'Ae')
    .replace(/Ö/g, 'Oe')
    .replace(/Ü/g, 'Ue');
}
