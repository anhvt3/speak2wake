import json

with open(r'e:\Kethoatngheo\speak2wake\data\vocabulary-de-a1a2.json','r',encoding='utf-8') as f:
    vocab = json.load(f)

cats = sorted(set(w["category"] for w in vocab))

# Write vocab as separate JS file
with open(r'e:\Kethoatngheo\speak2wake\previews\vocab.js','w',encoding='utf-8') as f:
    f.write('const VOCAB = ')
    json.dump(vocab, f, ensure_ascii=False, indent=0)
    f.write(';\n')
    f.write('const CATEGORIES = ' + json.dumps(cats) + ';\n')

print(f"vocab.js written with {len(vocab)} words")
