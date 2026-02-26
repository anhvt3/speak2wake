import json

with open(r'e:\Kethoatngheo\speak2wake\data\vocabulary-de-a1a2.json', 'r', encoding='utf-8') as f:
    vocab = json.load(f)

# Reclassify: German compound words are often long single words
# or multi-word expressions
COMPOUND_WORDS = {
    "Guten Morgen", "Guten Tag", "Auf Wiedersehen", "Gute Nacht",
    "Handschuh", "Krankenhaus", "Geburtstag", "Frühstück",
    "Mittagessen", "Abendessen", "Kühlschrank", "Badezimmer",
    "Schlafzimmer", "Wohnzimmer", "Straßenbahn", "Flughafen",
    "Hauptbahnhof", "Fahrkarte", "Briefkasten", "Waschmaschine",
    "Kopfschmerzen", "Bauchschmerzen", "Halskette", "Sonnenbrille",
    "Regenschirm", "Orangensaft", "Kartoffel", "Schokolade",
    "Bibliothek", "Schwimmbad", "Spielplatz", "Notizbuch",
    "Klassenzimmer", "Grundschule", "Hausaufgabe", "Bleistift",
    "Kugelschreiber", "Kleiderschrank", "Geschirrspüler"
}

for w in vocab:
    if w['difficulty'] == 'sentence':
        continue  # keep sentences
    bare = w.get('bare', '')
    if bare in COMPOUND_WORDS or ' ' in bare:
        w['difficulty'] = 'compound'
    elif len(bare) >= 10:  # Long German words are usually compound
        w['difficulty'] = 'compound'
    else:
        w['difficulty'] = 'single'

# Stats
singles = sum(1 for w in vocab if w['difficulty'] == 'single')
compounds = sum(1 for w in vocab if w['difficulty'] == 'compound')
sents = sum(1 for w in vocab if w['difficulty'] == 'sentence')
print(f"Total: {len(vocab)} items")
print(f"  Single (Easy): {singles}")
print(f"  Compound (Medium): {compounds}")
print(f"  Sentence (Hard): {sents}")

# Sample compounds
comps = [w['bare'] for w in vocab if w['difficulty'] == 'compound'][:15]
print(f"\nSample compounds: {comps}")

# Save
with open(r'e:\Kethoatngheo\speak2wake\data\vocabulary-de-a1a2.json', 'w', encoding='utf-8') as f:
    json.dump(vocab, f, ensure_ascii=False, indent=2)

cats = sorted(set(w["category"] for w in vocab))
with open(r'e:\Kethoatngheo\speak2wake\previews\vocab.js', 'w', encoding='utf-8') as f:
    f.write('var VOCAB = ')
    json.dump(vocab, f, ensure_ascii=False, indent=0)
    f.write(';\n')
    f.write('var CATEGORIES = ' + json.dumps(cats) + ';\n')

print(f"\nvocab.js regenerated")
