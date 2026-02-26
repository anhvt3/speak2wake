import json, re

with open(r'e:\Kethoatngheo\speak2wake\data\vocabulary-de-a1a2.json','r',encoding='utf-8') as f:
    vocab = json.load(f)

# Build JS vocab using JSON.dumps (proper escaping)
vocab_js_items = []
for w in vocab:
    item = json.dumps(w, ensure_ascii=False)
    vocab_js_items.append(item)

vocab_js = 'const VOCAB=[' + ',\n'.join(vocab_js_items) + '];'

cats = sorted(set(w["category"] for w in vocab))
cats_js = json.dumps(cats)

# Read the current HTML
with open(r'e:\Kethoatngheo\speak2wake\previews\index.html','r',encoding='utf-8') as f:
    html = f.read()

# Find the script section and replace vocab data
# Replace everything between "// === VOCABULARY DATA" and "const CATEGORIES"
pattern = r'// === VOCABULARY DATA.*?\nconst VOCAB=\[.*?\];'
replacement = '// === VOCABULARY DATA (500 words) ===\n' + vocab_js
html = re.sub(pattern, replacement, html, flags=re.DOTALL)

# Also fix the CATEGORIES line
pattern2 = r'const CATEGORIES = \[.*?\];'
html = re.sub(pattern2, f'const CATEGORIES = {cats_js};', html)

# Fix any remaining escaped single quotes from Python f-string
html = html.replace("\\'", "'")

# Now fix the main JS issue: Python f-string {{ }} should become { }
# But this was already handled... let me check for other issues

# Fix article field for non-article words (basics, adjectives etc have wrong article values)
# This is a data issue in the vocab, not HTML

with open(r'e:\Kethoatngheo\speak2wake\previews\index.html','w',encoding='utf-8') as f:
    f.write(html)

print(f"Fixed HTML: {len(html)} chars")

# Now validate the JS
import subprocess
result = subprocess.run(['node','-e',
    "const fs=require('fs');"
    "const html=fs.readFileSync('e:\\\\Kethoatngheo\\\\speak2wake\\\\previews\\\\index.html','utf8');"
    "const idx=html.lastIndexOf('<script>');"
    "const end=html.lastIndexOf('</script>');"
    "const js=html.substring(idx+8,end);"
    "try{new Function(js);console.log('JS VALID')}catch(e){console.log('JS ERROR:',e.message)}"
], capture_output=True, text=True)
print(result.stdout.strip())
