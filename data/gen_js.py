import json

with open(r'e:\Kethoatngheo\speak2wake\data\vocabulary-de-a1a2.json','r',encoding='utf-8') as f:
    data = json.load(f)

lines = ['const VOCAB = [']
for w in data:
    word = w["word"].replace('"','\\"')
    bare = w["bare"].replace('"','\\"')
    trans = w["translation"].replace('"','\\"')
    pho = w["phonetic"].replace('"','\\"')
    lines.append(f'  {{id:"{w["id"]}",word:"{word}",bare:"{bare}",article:"{w["article"]}",translation:"{trans}",level:"{w["level"]}",phonetic:"{pho}",category:"{w["category"]}"}},')
lines.append('];')

with open(r'e:\Kethoatngheo\speak2wake\data\vocab_js.txt','w',encoding='utf-8') as f:
    f.write('\n'.join(lines))
print(f'Wrote {len(data)} words as JS')
