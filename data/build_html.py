import json

with open(r'e:\Kethoatngheo\speak2wake\data\vocabulary-de-a1a2.json','r',encoding='utf-8') as f:
    vocab = json.load(f)

# Build compact JS vocab string
vocab_lines = []
for w in vocab:
    word = w["word"].replace("'","\\'").replace('"','\\"')
    bare = w["bare"].replace("'","\\'").replace('"','\\"')
    trans = w["translation"].replace("'","\\'").replace('"','\\"')
    pho = w["phonetic"].replace("'","\\'").replace('"','\\"')
    vocab_lines.append(f'{{id:"{w["id"]}",word:"{word}",bare:"{bare}",article:"{w["article"]}",translation:"{trans}",level:"{w["level"]}",phonetic:"{pho}",category:"{w["category"]}"}}')

vocab_js = 'const VOCAB=[' + ',\n'.join(vocab_lines) + '];'

# Get unique categories
cats = sorted(set(w["category"] for w in vocab))
cats_js = json.dumps(cats)

html = f'''<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no">
<title>Speak2Wake — Interactive Demo</title>
<script src="https://cdn.tailwindcss.com"></script>
<link href="https://fonts.googleapis.com/css2?family=Jost:wght@400;500;600;700&display=swap" rel="stylesheet">
<script>tailwind.config={{theme:{{extend:{{fontFamily:{{jost:['Jost','sans-serif']}},borderRadius:{{card:'28px',button:'38px',pill:'24px'}},colors:{{violet:{{DEFAULT:'#9E6DFB',light:'#8A70F8',pink:'#D28AED'}},accent:{{orange:'#F79F40',gold:'#FBF66E'}},success:'#4ADE80',error:'#F87171',warning:'#FBBF24'}}}}}}}}</script>
<style>
body{{font-family:'Jost',sans-serif;margin:0;overflow:hidden;height:100vh}}
.phone{{width:100vw;height:100vh;position:relative;overflow-y:auto;overflow-x:hidden}}
@media(min-width:500px){{
  body{{display:flex;justify-content:center;align-items:center;background:#0a0a14}}
  .phone{{width:390px;height:844px;border-radius:40px;border:3px solid rgba(255,255,255,.1);overflow:hidden}}
}}
.screen{{display:none;min-height:100%;padding:24px;padding-top:50px}}
.screen.active{{display:flex;flex-direction:column}}
.glass{{background:rgba(255,255,255,.15);backdrop-filter:blur(12px);border:1px solid rgba(255,255,255,.2)}}
.glass-dark{{background:rgba(255,255,255,.06);border:1px solid rgba(255,255,255,.08)}}
.btn-primary{{background:linear-gradient(135deg,#F79F40,#FBF66E);color:#1C1721;border:none;cursor:pointer;font-weight:600;font-family:inherit}}
.btn-secondary{{background:linear-gradient(135deg,#8A70F8,#D28AED);color:white;border:none;cursor:pointer;font-weight:500;font-family:inherit}}
.gradient-bg{{background:linear-gradient(160deg,#8A70F8,#D28AED)}}
.gradient-dark{{background:linear-gradient(180deg,#1C1721 0%,#8A70F8 60%,#D28AED 100%)}}
input,select{{font-family:inherit;font-size:16px}}
.toggle{{width:48px;height:26px;border-radius:13px;position:relative;cursor:pointer;transition:all .2s}}
.toggle.on{{background:#9E6DFB}}
.toggle.off{{background:rgba(255,255,255,.2)}}
.toggle::after{{content:'';width:22px;height:22px;border-radius:50%;position:absolute;top:2px;transition:all .2s}}
.toggle.on::after{{left:24px;background:#FBF66E}}
.toggle.off::after{{left:2px;background:rgba(255,255,255,.6)}}
@keyframes pulse-ring{{0%{{transform:scale(.8);opacity:.7}}100%{{transform:scale(1.4);opacity:0}}}}
@keyframes gentle-pulse{{0%,100%{{transform:scale(1)}}50%{{transform:scale(1.05)}}}}
@keyframes shake{{0%,100%{{transform:translateX(0)}}25%{{transform:translateX(-8px)}}75%{{transform:translateX(8px)}}}}
.animate-pulse-ring{{animation:pulse-ring 1.5s ease-out infinite}}
.animate-pulse-ring-d{{animation:pulse-ring 1.5s ease-out .5s infinite}}
.animate-gentle{{animation:gentle-pulse 2s ease-in-out infinite}}
.animate-shake{{animation:shake .3s ease-in-out}}
.modal-overlay{{position:fixed;inset:0;background:rgba(0,0,0,.7);backdrop-filter:blur(4px);display:flex;align-items:center;justify-content:center;z-index:100;padding:20px}}
.modal-content{{background:linear-gradient(160deg,#2a2040,#3a2060);border-radius:28px;padding:24px;max-width:340px;width:100%;border:1px solid rgba(255,255,255,.15)}}
.status-bar{{display:flex;justify-content:space-between;font-size:12px;color:rgba(255,255,255,.5);padding:8px 4px}}
.day-btn{{width:36px;height:36px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:12px;cursor:pointer;border:none;font-family:inherit}}
.day-btn.active{{background:#9E6DFB;color:white}}
.day-btn.inactive{{background:rgba(255,255,255,.15);color:rgba(255,255,255,.5)}}
.cat-pill{{padding:4px 12px;border-radius:24px;font-size:13px;cursor:pointer;border:none;font-family:inherit}}
.cat-pill.active{{background:#9E6DFB;color:white}}
.cat-pill.inactive{{background:rgba(255,255,255,.1);color:rgba(255,255,255,.6)}}
.waveform-bar{{width:4px;border-radius:2px;background:rgba(247,159,64,.7);transition:height .1s}}
.alarm-card{{cursor:pointer;transition:transform .1s}}
.alarm-card:active{{transform:scale(.98)}}
</style>
</head>
<body>
<div class="phone gradient-bg" id="phone">
<!-- STATUS BAR -->
<div class="status-bar" style="position:sticky;top:0;z-index:10;background:rgba(138,112,248,.8);backdrop-filter:blur(8px)">
<span id="clock">9:41</span><span>⚡ 87%</span>
</div>

<!-- HOME SCREEN -->
<div class="screen active" id="s-home">
<div class="flex items-center justify-between mb-5">
<div class="w-10 h-10 rounded-full bg-white/15 flex items-center justify-center text-lg cursor-pointer" onclick="showScreen('settings')">⚙</div>
<h2 class="text-xl font-semibold text-white">Speak2Wake</h2>
<div class="w-10 h-10 rounded-full bg-white/15 flex items-center justify-center text-2xl cursor-pointer" onclick="showScreen('add')">+</div>
</div>
<div id="next-alarm-banner" class="glass rounded-card p-5 mb-5"></div>
<div id="alarm-list" class="space-y-3 flex-1"></div>
<button onclick="simulateAlarm()" class="btn-primary rounded-button py-4 text-lg mt-5 w-full" style="font-size:18px">🔔 Simulate Alarm</button>
<p class="text-center text-white/30 text-xs mt-3">500 German words • Phase 1 MVP Demo</p>
</div>

<!-- ADD/EDIT ALARM SCREEN -->
<div class="screen" id="s-add">
<div class="flex items-center justify-between mb-5">
<div class="w-10 h-10 rounded-full bg-white/15 flex items-center justify-center text-lg cursor-pointer" onclick="showScreen('home')">✕</div>
<h2 class="text-xl font-semibold text-white" id="add-title">New Alarm</h2>
<div class="w-10"></div>
</div>
<div class="glass rounded-card p-6 mb-4 flex items-center justify-center gap-4">
<div class="text-center"><p class="text-white/40 text-xl cursor-pointer" onclick="adjTime('h',1)">▲</p><input id="inp-h" type="text" value="07" class="text-6xl font-semibold text-white bg-transparent text-center w-24 border-none outline-none" maxlength="2"><p class="text-white/40 text-xl cursor-pointer" onclick="adjTime('h',-1)">▼</p></div>
<p class="text-5xl font-semibold text-white">:</p>
<div class="text-center"><p class="text-white/40 text-xl cursor-pointer" onclick="adjTime('m',1)">▲</p><input id="inp-m" type="text" value="30" class="text-6xl font-semibold text-white bg-transparent text-center w-24 border-none outline-none" maxlength="2"><p class="text-white/40 text-xl cursor-pointer" onclick="adjTime('m',-1)">▼</p></div>
</div>
<div class="glass rounded-card p-4 mb-4">
<p class="text-white/50 text-sm mb-2">Label</p>
<input id="inp-label" class="w-full bg-transparent text-white border-b border-white/20 pb-2 outline-none" placeholder="Wake German" value="Wake German">
</div>
<div class="glass rounded-card p-4 mb-4">
<p class="text-white/50 text-sm mb-3">Repeat</p>
<div class="flex gap-2" id="day-picker"></div>
</div>
<div class="glass rounded-card p-4 mb-4">
<div class="flex items-center justify-between mb-3"><span class="text-white">Voice Challenge</span><div class="toggle on" id="tog-voice" onclick="toggleEl(this)"></div></div>
<div class="flex items-center justify-between mb-3"><span class="text-white">Snooze</span><div class="toggle on" id="tog-snooze" onclick="toggleEl(this)"></div></div>
<div class="flex items-center justify-between"><span class="text-white">Vibration</span><div class="toggle on" id="tog-vib" onclick="toggleEl(this)"></div></div>
</div>
<button onclick="saveAlarm()" class="btn-primary rounded-button py-4 text-lg w-full">Save Alarm</button>
</div>

<!-- RING SCREEN -->
<div class="screen" id="s-ring" style="justify-content:space-between;min-height:100%">
<div class="text-center mt-8">
<p class="text-white/40 text-lg">Wake Up</p>
<p class="text-7xl font-semibold text-white mt-2 animate-gentle" id="ring-time">07:30</p>
<p class="text-white/60 mt-2" id="ring-label">Wake German</p>
</div>
<div class="flex items-center justify-center my-8">
<div class="w-40 h-40 rounded-full bg-violet/20 flex items-center justify-center">
<div class="w-28 h-28 rounded-full bg-violet/30 flex items-center justify-center">
<span class="text-5xl animate-gentle">🔔</span>
</div>
</div>
</div>
<div class="space-y-4 mb-8">
<div class="glass rounded-card p-3"><button onclick="snoozeAlarm()" class="btn-secondary rounded-button py-3 w-full text-base">Snooze 5m</button></div>
<button onclick="startChallenge()" class="btn-primary rounded-button py-4 w-full text-lg">Start Challenge</button>
</div>
</div>

<!-- VOICE CHALLENGE SCREEN -->
<div class="screen" id="s-challenge">
<p class="text-center text-white/40 text-sm mb-4">Read this word to dismiss alarm</p>
<div class="glass rounded-card p-6 text-center mb-6" id="word-card">
<p class="text-white/50 text-lg" id="ch-article"></p>
<p class="text-4xl font-semibold text-white mt-1" id="ch-word"></p>
<p class="text-white/40 mt-2" id="ch-trans"></p>
<p class="text-violet-pink text-sm mt-1" id="ch-phonetic"></p>
<button onclick="speakWord()" class="mt-4 bg-white/10 rounded-full px-5 py-2 text-sm text-white border-none cursor-pointer flex items-center gap-2 mx-auto" style="font-family:inherit">🔊 Listen</button>
</div>
<div class="flex flex-col items-center mb-4">
<div class="relative" style="width:160px;height:160px;display:flex;align-items:center;justify-content:center">
<div class="absolute inset-0 rounded-full border-2 border-accent-orange animate-pulse-ring" id="pulse1" style="display:none"></div>
<div class="absolute inset-0 rounded-full border-2 border-accent-orange animate-pulse-ring-d" id="pulse2" style="display:none"></div>
<button id="mic-btn" onclick="toggleMic()" class="w-20 h-20 rounded-full flex items-center justify-center text-3xl border-none cursor-pointer" style="background:radial-gradient(circle,#FBF66E,#F79F40)">🎤</button>
</div>
<p class="text-white/50 text-sm mt-2" id="mic-status">Tap to speak</p>
</div>
<div class="flex items-center justify-center gap-1 mb-4" id="waveform" style="height:40px"></div>
<p class="text-center text-white/40 text-sm mb-4" id="partial-text"></p>
<div id="score-card" class="glass rounded-card p-4" style="display:none">
<div class="flex items-center justify-between">
<div><p class="text-2xl font-semibold" id="score-result"></p><p class="text-white/60 text-sm mt-1" id="score-feedback"></p></div>
<p class="text-white/40 text-xs" id="score-attempt"></p>
</div>
<div class="flex gap-3 mt-3">
<div class="bg-white/10 rounded-pill px-3 py-1"><p class="text-white/40 text-xs">Text</p><p class="text-sm font-medium text-white" id="sc-lev"></p></div>
<div class="bg-white/10 rounded-pill px-3 py-1"><p class="text-white/40 text-xs">Sound</p><p class="text-sm font-medium text-white" id="sc-pho"></p></div>
<div class="bg-white/10 rounded-pill px-3 py-1"><p class="text-white/40 text-xs">Conf</p><p class="text-sm font-medium text-white" id="sc-conf"></p></div>
</div>
</div>
</div>

<!-- SETTINGS SCREEN -->
<div class="screen" id="s-settings">
<div class="flex items-center justify-between mb-5">
<div class="w-10 h-10 rounded-full bg-white/15 flex items-center justify-center cursor-pointer" onclick="showScreen('home')">←</div>
<h2 class="text-xl font-semibold text-white">Settings</h2>
<div class="w-10"></div>
</div>
<div class="glass rounded-card p-4 mb-4">
<p class="font-semibold text-white mb-2">Voice Recognition</p>
<div class="flex items-center justify-between mb-2"><span class="text-white/60 text-sm">Web Speech API</span><span class="text-sm font-medium" id="stt-status" style="color:#4ADE80">Checking...</span></div>
<div class="flex items-center justify-between"><span class="text-white/60 text-sm">Language</span><span class="text-white/80 text-sm">German (de-DE)</span></div>
</div>
<div class="glass rounded-card p-4 mb-4">
<p class="font-semibold text-white mb-2">Scoring Engine</p>
<div class="text-white/50 text-xs space-y-1">
<p>• Levenshtein distance: 40%</p>
<p>• Cologne Phonetic: 30%</p>
<p>• STT Confidence: 30%</p>
<p>• Dynamic thresholds: Short≤3→80%, Med 4-8→70%, Long>8→60%</p>
</div>
</div>
<div class="glass rounded-card p-4 mb-4">
<p class="font-semibold text-white mb-2">Vocabulary</p>
<p class="text-white/60 text-sm" id="vocab-count"></p>
<div class="flex flex-wrap gap-2 mt-2" id="cat-pills"></div>
</div>
<button onclick="showScreen('vocab')" class="btn-secondary rounded-button py-3 w-full mb-4 text-base" style="border:none;font-family:inherit">Browse Vocabulary →</button>
<div class="glass rounded-card p-4">
<p class="font-semibold text-white mb-2">About</p>
<p class="text-white/40 text-sm">Speak2Wake v1.0.0 — Interactive Demo</p>
<p class="text-white/40 text-xs mt-1">Scoring: Levenshtein + Cologne Phonetic + STT Confidence</p>
</div>
</div>

<!-- VOCABULARY BROWSER -->
<div class="screen" id="s-vocab">
<div class="flex items-center justify-between mb-4">
<div class="w-10 h-10 rounded-full bg-white/15 flex items-center justify-center cursor-pointer" onclick="showScreen('settings')">←</div>
<h2 class="text-xl font-semibold text-white">Vocabulary</h2>
<div class="w-10"></div>
</div>
<div class="flex flex-wrap gap-2 mb-4" id="vocab-filters"></div>
<div id="vocab-list" class="space-y-2 flex-1 overflow-y-auto" style="max-height:calc(100vh - 200px)"></div>
</div>

<!-- SUCCESS SCREEN -->
<div class="screen" id="s-success" style="justify-content:center;align-items:center;text-align:center">
<div style="font-size:80px;margin-bottom:16px">🎉</div>
<h2 class="text-3xl font-bold text-white mb-2">Challenge Complete!</h2>
<p class="text-white/60 mb-2" id="success-word"></p>
<p class="text-white/40 text-sm mb-8" id="success-score"></p>
<button onclick="showScreen('home')" class="btn-primary rounded-button py-4 px-12 text-lg" style="border:none;font-family:inherit">Done ✓</button>
</div>
</div>

<!-- FAILSAFE MODAL -->
<div class="modal-overlay" id="failsafe-modal" style="display:none">
<div class="modal-content">
<h3 class="text-xl font-semibold text-white mb-2">🔓 Fail-safe</h3>
<p class="text-white/60 text-sm mb-4">5 attempts used. Choose an alternative:</p>
<div class="flex gap-2 mb-4">
<button onclick="showFailsafe('type')" class="flex-1 btn-secondary rounded-pill py-2 text-sm" style="border:none;font-family:inherit">Type Word</button>
<button onclick="showFailsafe('math')" class="flex-1 btn-secondary rounded-pill py-2 text-sm" style="border:none;font-family:inherit">Solve Math</button>
</div>
<div id="fs-type" style="display:none">
<p class="text-white/50 text-sm mb-2">Type: <strong class="text-white" id="fs-expected"></strong></p>
<input id="fs-input" class="w-full p-3 rounded-xl bg-white/10 text-white border border-white/20 outline-none mb-3" placeholder="Type the word..." style="font-family:inherit">
<button onclick="checkFailsafe('type')" class="btn-primary rounded-pill py-2 w-full text-sm" style="border:none;font-family:inherit">Submit</button>
</div>
<div id="fs-math" style="display:none">
<p class="text-white text-2xl font-semibold text-center mb-3" id="fs-question"></p>
<input id="fs-math-input" type="number" class="w-full p-3 rounded-xl bg-white/10 text-white border border-white/20 outline-none mb-3 text-center text-xl" placeholder="?" style="font-family:inherit">
<button onclick="checkFailsafe('math')" class="btn-primary rounded-pill py-2 w-full text-sm" style="border:none;font-family:inherit">Submit</button>
</div>
<p id="fs-error" class="text-error text-sm mt-2" style="display:none"></p>
</div>
</div>

<script>
// === VOCABULARY DATA (500 words) ===
{vocab_js}
const CATEGORIES = {cats_js};

// === CONSTANTS (from constants/index.ts) ===
const SCORING_WEIGHTS = {{LEVENSHTEIN:0.4, PHONETIC:0.3, CONFIDENCE:0.3}};
const DYNAMIC_THRESHOLDS = {{
  SHORT:{{maxLength:3, threshold:0.80}},
  MEDIUM:{{maxLength:8, threshold:0.70}},
  LONG:{{maxLength:Infinity, threshold:0.60}}
}};
const MAX_VOICE_ATTEMPTS = 5;
const DAY_LABELS = ['S','M','T','W','T','F','S'];

// === SCORING ENGINE (ported from engine/*.ts) ===
function levenshteinDistance(a,b){{
  const m=a.length,n=b.length;
  const dp=Array.from({{length:m+1}},()=>Array(n+1).fill(0));
  for(let i=0;i<=m;i++)dp[i][0]=i;
  for(let j=0;j<=n;j++)dp[0][j]=j;
  for(let i=1;i<=m;i++)for(let j=1;j<=n;j++){{
    if(a[i-1]===b[j-1])dp[i][j]=dp[i-1][j-1];
    else dp[i][j]=1+Math.min(dp[i-1][j],dp[i][j-1],dp[i-1][j-1]);
  }}
  return dp[m][n];
}}
function levenshteinScore(a,b){{
  const mx=Math.max(a.length,b.length);
  return mx===0?1:1-levenshteinDistance(a,b)/mx;
}}

// Cologne Phonetic (German-optimized, from engine/phonetic.ts)
function colognePhonetic(word){{
  if(!word)return'';
  const inp=word.toUpperCase().replace(/Ä/g,'AE').replace(/Ö/g,'OE').replace(/Ü/g,'UE').replace(/ß/g,'SS').replace(/[^A-Z]/g,'');
  if(!inp)return'';
  const codes=[];
  for(let i=0;i<inp.length;i++){{
    const c=inp[i],p=i>0?inp[i-1]:'',n=i<inp.length-1?inp[i+1]:'';
    let code='';
    if('AEIOU'.includes(c))code='0';
    else if(c==='H')code='';
    else if('BP'.includes(c))code=n==='H'?'3':'1';
    else if('DT'.includes(c))code='CSZ'.includes(n)?'8':'2';
    else if('FVW'.includes(c))code='3';
    else if('GKQ'.includes(c))code='4';
    else if(c==='X')code='48';
    else if(c==='L')code='5';
    else if('MN'.includes(c))code='6';
    else if(c==='R')code='7';
    else if('SZ'.includes(c))code='8';
    else if(c==='C'){{
      if(i===0)code='AHKLOQRUX'.includes(n)?'4':'8';
      else if('AEIOU'.includes(p)&&'AHKOQUX'.includes(n))code='4';
      else if('SZ'.includes(p))code='8';
      else code='AHKOQUX'.includes(n)?'4':'8';
    }}
    else if('JY'.includes(c))code='0';
    codes.push(code);
  }}
  let r=codes.join(''),d='';
  for(let i=0;i<r.length;i++)if(i===0||r[i]!==r[i-1])d+=r[i];
  return d.length>0?d[0]+d.substring(1).replace(/0/g,''):'';
}}
function phoneticScore(a,b){{
  const c1=colognePhonetic(a),c2=colognePhonetic(b);
  if(!c1&&!c2)return 1;if(!c1||!c2)return 0;if(c1===c2)return 1;
  const mx=Math.max(c1.length,c2.length);
  return 1-levenshteinDistance(c1,c2)/mx;
}}

// Normalizer (from engine/normalizer.ts)
const ARTICLES=['der','die','das','ein','eine','einem','einen','einer','eines'];
function normalize(text){{
  let r=text.toLowerCase().trim().replace(/[.,!?;:'"()\\-]/g,'');
  for(const a of ARTICLES)if(r.startsWith(a+' ')){{r=r.substring(a.length+1).trim();break;}}
  return r.trim();
}}

// Dynamic threshold (from engine/scoring.ts)
function getDynamicThreshold(len){{
  if(len<=DYNAMIC_THRESHOLDS.SHORT.maxLength)return DYNAMIC_THRESHOLDS.SHORT.threshold;
  if(len<=DYNAMIC_THRESHOLDS.MEDIUM.maxLength)return DYNAMIC_THRESHOLDS.MEDIUM.threshold;
  return DYNAMIC_THRESHOLDS.LONG.threshold;
}}

// Main scoring (from engine/scoring.ts)
function evaluateChallenge(expected,spoken,sttConfidence){{
  const ne=normalize(expected),ns=normalize(spoken);
  const lev=levenshteinScore(ne,ns);
  const pho=phoneticScore(ne,ns);
  const conf=Math.max(0,Math.min(1,sttConfidence));
  const combined=lev*SCORING_WEIGHTS.LEVENSHTEIN+pho*SCORING_WEIGHTS.PHONETIC+conf*SCORING_WEIGHTS.CONFIDENCE;
  const threshold=getDynamicThreshold(ne.length);
  const passed=combined>=threshold;
  let feedback;
  if(passed)feedback=combined>=0.9?'Perfect! 🎉':'Correct! Well done. ✓';
  else if(combined>=threshold-0.1)feedback='Almost! Try once more.';
  else feedback='Not quite. Listen and try again.';
  return {{levenshteinScore:lev,phoneticScore:pho,confidenceScore:conf,combinedScore:combined,threshold,passed,feedback}};
}}

// Failsafe (from engine/failsafe.ts)
function generateMathProblem(){{
  const ops=['+','-'];const op=ops[Math.floor(Math.random()*2)];
  let a,b,ans;
  if(op==='+'){{a=Math.floor(Math.random()*90)+10;b=Math.floor(Math.random()*90)+10;ans=a+b;}}
  else{{a=Math.floor(Math.random()*90)+10;b=Math.floor(Math.random()*(a-1))+1;ans=a-b;}}
  return{{question:`${{a}} ${{op}} ${{b}} = ?`,answer:ans}};
}}

// === APP STATE ===
let currentScreen='home';
let alarms=JSON.parse(localStorage.getItem('s2w_alarms')||'[]');
let editingId=null;
let challenge={{word:null,attempts:0,listening:false}};
let mathProblem=null;
let recognition=null;
let selectedDays=[1,2,3,4,5]; // Mon-Fri default
let vocabFilter='all';

// === NAVIGATION ===
function showScreen(name){{
  document.querySelectorAll('.screen').forEach(s=>s.classList.remove('active'));
  document.getElementById('s-'+name).classList.add('active');
  const phone=document.getElementById('phone');
  if(name==='ring')phone.className='phone gradient-dark';
  else if(name==='challenge')phone.className='phone gradient-dark';
  else phone.className='phone gradient-bg';
  currentScreen=name;
  if(name==='home')renderHome();
  if(name==='settings')renderSettings();
  if(name==='vocab')renderVocab();
  if(name==='add')renderAddScreen();
}}

// === ALARM CRUD ===
function renderHome(){{
  const list=document.getElementById('alarm-list');
  const banner=document.getElementById('next-alarm-banner');
  if(alarms.length===0){{
    list.innerHTML='<p class="text-center text-white/40 mt-8">No alarms yet. Tap + to add one.</p>';
    banner.innerHTML='<p class="text-white/50 text-sm">No upcoming alarm</p><p class="text-3xl font-semibold text-white mt-1">--:--</p>';
    return;
  }}
  const active=alarms.filter(a=>a.enabled).sort((a,b)=>a.time.localeCompare(b.time));
  if(active.length>0){{
    const next=active[0];
    banner.innerHTML=`<p class="text-white/50 text-sm">Next Alarm</p><p class="text-5xl font-semibold text-white mt-1">${{next.time}}</p><p class="text-white/40 text-sm mt-1">${{next.label}}</p>`;
  }}else{{
    banner.innerHTML='<p class="text-white/50 text-sm">All alarms off</p><p class="text-3xl font-semibold text-white/30 mt-1">--:--</p>';
  }}
  list.innerHTML=alarms.map(a=>`
    <div class="glass rounded-card p-4 alarm-card" onclick="editAlarm('${{a.id}}')">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-4xl font-semibold ${{a.enabled?'text-white':'text-white/40'}}">${{a.time}}</p>
          <div class="flex items-center gap-2 mt-1">
            <span class="text-white/60 text-sm">${{a.label}}</span>
            <span class="text-white/40 text-xs">${{a.days.length===7?'Every day':a.days.length===5?'Weekdays':a.days.map(d=>DAY_LABELS[d]).join(',')}}</span>
          </div>
          ${{a.voiceChallenge?'<div class="mt-2"><span class="bg-violet/30 text-violet-pink text-xs px-3 py-1 rounded-pill">Voice Challenge</span></div>':''}}
        </div>
        <div class="toggle ${{a.enabled?'on':'off'}}" onclick="event.stopPropagation();toggleAlarmEnabled('${{a.id}}')"></div>
      </div>
    </div>`).join('');
}}

function renderAddScreen(){{
  const title=document.getElementById('add-title');
  title.textContent=editingId?'Edit Alarm':'New Alarm';
  const dp=document.getElementById('day-picker');
  dp.innerHTML=DAY_LABELS.map((d,i)=>`<button class="day-btn ${{selectedDays.includes(i)?'active':'inactive'}}" onclick="toggleDay(${{i}})">${{d}}</button>`).join('');
  if(editingId){{
    const alarm=alarms.find(a=>a.id===editingId);
    if(alarm){{
      document.getElementById('inp-h').value=alarm.time.split(':')[0];
      document.getElementById('inp-m').value=alarm.time.split(':')[1];
      document.getElementById('inp-label').value=alarm.label;
      selectedDays=alarm.days;
      dp.innerHTML=DAY_LABELS.map((d,i)=>`<button class="day-btn ${{selectedDays.includes(i)?'active':'inactive'}}" onclick="toggleDay(${{i}})">${{d}}</button>`).join('');
    }}
  }}
}}

function adjTime(unit,dir){{
  const el=document.getElementById(unit==='h'?'inp-h':'inp-m');
  let v=parseInt(el.value)||0;
  const max=unit==='h'?23:59;
  v+=dir;if(v<0)v=max;if(v>max)v=0;
  el.value=String(v).padStart(2,'0');
}}

function toggleDay(i){{
  const idx=selectedDays.indexOf(i);
  if(idx>=0)selectedDays.splice(idx,1);else selectedDays.push(i);
  renderAddScreen();
}}

function saveAlarm(){{
  const h=document.getElementById('inp-h').value.padStart(2,'0');
  const m=document.getElementById('inp-m').value.padStart(2,'0');
  const label=document.getElementById('inp-label').value||'Alarm';
  const voice=document.getElementById('tog-voice').classList.contains('on');
  if(editingId){{
    const alarm=alarms.find(a=>a.id===editingId);
    if(alarm){{alarm.time=h+':'+m;alarm.label=label;alarm.days=[...selectedDays];alarm.voiceChallenge=voice;}}
    editingId=null;
  }}else{{
    alarms.push({{id:Date.now().toString(),time:h+':'+m,label,days:[...selectedDays],enabled:true,voiceChallenge:voice}});
  }}
  localStorage.setItem('s2w_alarms',JSON.stringify(alarms));
  selectedDays=[1,2,3,4,5];editingId=null;
  showScreen('home');
}}

function editAlarm(id){{editingId=id;showScreen('add');}}
function toggleAlarmEnabled(id){{
  const a=alarms.find(x=>x.id===id);if(a)a.enabled=!a.enabled;
  localStorage.setItem('s2w_alarms',JSON.stringify(alarms));renderHome();
}}
function toggleEl(el){{el.classList.toggle('on');el.classList.toggle('off');}}

// === ALARM SIMULATION ===
function simulateAlarm(){{
  const active=alarms.filter(a=>a.enabled);
  const alarm=active.length>0?active[0]:{{time:new Date().getHours().toString().padStart(2,'0')+':'+new Date().getMinutes().toString().padStart(2,'0'),label:'Demo Alarm'}};
  document.getElementById('ring-time').textContent=alarm.time;
  document.getElementById('ring-label').textContent=alarm.label;
  showScreen('ring');
  // Play sound
  try{{const ac=new AudioContext();const o=ac.createOscillator();const g=ac.createGain();o.connect(g);g.connect(ac.destination);o.frequency.value=800;g.gain.value=0.3;o.start();setTimeout(()=>{{o.stop();ac.close();}},1000);}}catch(e){{}}
}}
function snoozeAlarm(){{showScreen('home');}}

// === VOICE CHALLENGE ===
function startChallenge(){{
  const word=VOCAB[Math.floor(Math.random()*VOCAB.length)];
  challenge={{word,attempts:0,listening:false}};
  document.getElementById('ch-article').textContent=word.article||'';
  document.getElementById('ch-word').textContent=word.bare;
  document.getElementById('ch-trans').textContent=word.translation;
  document.getElementById('ch-phonetic').textContent='/'+word.phonetic+'/';
  document.getElementById('score-card').style.display='none';
  document.getElementById('partial-text').textContent='';
  document.getElementById('mic-status').textContent='Tap to speak';
  document.getElementById('pulse1').style.display='none';
  document.getElementById('pulse2').style.display='none';
  showScreen('challenge');
  initWaveform();
}}

function speakWord(){{
  if(!challenge.word)return;
  const u=new SpeechSynthesisUtterance(challenge.word.word);
  u.lang='de-DE';u.rate=0.8;
  speechSynthesis.speak(u);
}}

function initWaveform(){{
  const wf=document.getElementById('waveform');
  wf.innerHTML='';
  for(let i=0;i<20;i++){{
    const bar=document.createElement('div');
    bar.className='waveform-bar';
    bar.style.height='4px';
    wf.appendChild(bar);
  }}
}}
function animateWaveform(active){{
  const bars=document.querySelectorAll('.waveform-bar');
  bars.forEach(b=>{{b.style.height=active?(Math.random()*30+4)+'px':'4px';}});
}}

function toggleMic(){{
  if(challenge.listening){{stopListening();return;}}
  if(challenge.attempts>=MAX_VOICE_ATTEMPTS){{openFailsafe();return;}}
  startListening();
}}

function startListening(){{
  const SR=window.SpeechRecognition||window.webkitSpeechRecognition;
  if(!SR){{alert('Speech Recognition not supported. Use Chrome or Edge.');return;}}
  recognition=new SR();
  recognition.lang='de-DE';
  recognition.continuous=false;
  recognition.interimResults=true;
  recognition.maxAlternatives=1;
  challenge.listening=true;
  document.getElementById('mic-status').textContent='Listening...';
  document.getElementById('mic-btn').style.background='radial-gradient(circle,#F87171,#DC2626)';
  document.getElementById('pulse1').style.display='block';
  document.getElementById('pulse2').style.display='block';
  const wfInterval=setInterval(()=>{{if(challenge.listening)animateWaveform(true);else{{clearInterval(wfInterval);animateWaveform(false);}}}},100);
  recognition.onresult=(e)=>{{
    let transcript='',confidence=0;
    for(let i=0;i<e.results.length;i++){{
      transcript=e.results[i][0].transcript;
      confidence=e.results[i][0].confidence;
      if(e.results[i].isFinal){{
        stopListening();
        processResult(transcript,confidence);
        return;
      }}
    }}
    document.getElementById('partial-text').textContent='"'+transcript+'..."';
  }};
  recognition.onerror=(e)=>{{
    console.log('STT error:',e.error);
    stopListening();
    if(e.error==='no-speech')document.getElementById('mic-status').textContent='No speech detected. Try again.';
    else document.getElementById('mic-status').textContent='Error: '+e.error+'. Try again.';
  }};
  recognition.onend=()=>{{if(challenge.listening)stopListening();}};
  try{{recognition.start();}}catch(e){{stopListening();document.getElementById('mic-status').textContent='Mic error. Try again.';}}
}}

function stopListening(){{
  challenge.listening=false;
  if(recognition)try{{recognition.stop();}}catch(e){{}}
  document.getElementById('mic-btn').style.background='radial-gradient(circle,#FBF66E,#F79F40)';
  document.getElementById('pulse1').style.display='none';
  document.getElementById('pulse2').style.display='none';
  animateWaveform(false);
}}

function processResult(transcript,confidence){{
  challenge.attempts++;
  const result=evaluateChallenge(challenge.word.bare,transcript,confidence);
  const card=document.getElementById('score-card');
  card.style.display='block';
  const pct=Math.round(result.combinedScore*100);
  document.getElementById('score-result').textContent=(result.passed?'✓ ':'✗ ')+pct+'%';
  document.getElementById('score-result').style.color=result.passed?'#4ADE80':'#F87171';
  document.getElementById('score-feedback').textContent=result.feedback;
  document.getElementById('score-attempt').textContent='Attempt '+challenge.attempts+'/'+MAX_VOICE_ATTEMPTS;
  document.getElementById('sc-lev').textContent=Math.round(result.levenshteinScore*100)+'%';
  document.getElementById('sc-pho').textContent=Math.round(result.phoneticScore*100)+'%';
  document.getElementById('sc-conf').textContent=Math.round(result.confidenceScore*100)+'%';
  document.getElementById('partial-text').textContent='"'+transcript+'"';
  document.getElementById('mic-status').textContent=result.passed?'Correct!':'Tap to try again';
  if(result.passed){{
    setTimeout(()=>{{
      document.getElementById('success-word').textContent=challenge.word.article+' '+challenge.word.bare+' — '+challenge.word.translation;
      document.getElementById('success-score').textContent='Score: '+pct+'% (Threshold: '+Math.round(result.threshold*100)+'%)';
      showScreen('success');
    }},1500);
  }}else if(challenge.attempts>=MAX_VOICE_ATTEMPTS){{
    document.getElementById('mic-status').textContent='Max attempts reached';
    setTimeout(openFailsafe,1000);
  }}else{{
    document.getElementById('word-card').classList.add('animate-shake');
    setTimeout(()=>document.getElementById('word-card').classList.remove('animate-shake'),400);
  }}
}}

// === FAILSAFE ===
function openFailsafe(){{
  document.getElementById('failsafe-modal').style.display='flex';
  document.getElementById('fs-type').style.display='none';
  document.getElementById('fs-math').style.display='none';
  document.getElementById('fs-error').style.display='none';
}}
function showFailsafe(mode){{
  document.getElementById('fs-type').style.display=mode==='type'?'block':'none';
  document.getElementById('fs-math').style.display=mode==='math'?'block':'none';
  document.getElementById('fs-error').style.display='none';
  if(mode==='type'){{
    document.getElementById('fs-expected').textContent=challenge.word.bare;
    document.getElementById('fs-input').value='';
    document.getElementById('fs-input').focus();
  }}
  if(mode==='math'){{
    mathProblem=generateMathProblem();
    document.getElementById('fs-question').textContent=mathProblem.question;
    document.getElementById('fs-math-input').value='';
    document.getElementById('fs-math-input').focus();
  }}
}}
function checkFailsafe(mode){{
  if(mode==='type'){{
    const typed=document.getElementById('fs-input').value;
    const expected=challenge.word.bare;
    if(normalize(typed)===normalize(expected)){{
      document.getElementById('failsafe-modal').style.display='none';
      showScreen('home');
    }}else{{
      document.getElementById('fs-error').textContent='Incorrect. Try again.';
      document.getElementById('fs-error').style.display='block';
    }}
  }}
  if(mode==='math'){{
    const ans=parseInt(document.getElementById('fs-math-input').value);
    if(ans===mathProblem.answer){{
      document.getElementById('failsafe-modal').style.display='none';
      showScreen('home');
    }}else{{
      document.getElementById('fs-error').textContent='Wrong answer. Try again.';
      document.getElementById('fs-error').style.display='block';
    }}
  }}
}}

// === SETTINGS ===
function renderSettings(){{
  const SR=window.SpeechRecognition||window.webkitSpeechRecognition;
  document.getElementById('stt-status').textContent=SR?'✓ Ready':'✗ Not available';
  document.getElementById('stt-status').style.color=SR?'#4ADE80':'#F87171';
  document.getElementById('vocab-count').textContent=VOCAB.length+' German words (A1-A2), '+CATEGORIES.length+' categories';
  const pills=document.getElementById('cat-pills');
  pills.innerHTML=CATEGORIES.map(c=>{{
    const count=VOCAB.filter(w=>w.category===c).length;
    return `<span class="cat-pill inactive">${{c}} (${{count}})</span>`;
  }}).join('');
}}

// === VOCABULARY BROWSER ===
function renderVocab(){{
  const filters=document.getElementById('vocab-filters');
  filters.innerHTML='<button class="cat-pill ${{vocabFilter==="all"?"active":"inactive"}}" onclick="setVocabFilter(\'all\')">All ('+VOCAB.length+')</button>'+
    CATEGORIES.map(c=>`<button class="cat-pill ${{vocabFilter===c?"active":"inactive"}}" onclick="setVocabFilter('${{c}}')">${{c}} (${{VOCAB.filter(w=>w.category===c).length}})</button>`).join('');
  const words=vocabFilter==='all'?VOCAB:VOCAB.filter(w=>w.category===vocabFilter);
  const list=document.getElementById('vocab-list');
  list.innerHTML=words.slice(0,100).map(w=>`
    <div class="glass-dark rounded-xl p-3 flex items-center justify-between">
      <div>
        <span class="text-white/50 text-xs">${{w.article}}</span>
        <span class="text-white font-medium"> ${{w.bare}}</span>
        <span class="text-white/40 text-sm ml-2">${{w.translation}}</span>
      </div>
      <button onclick="event.stopPropagation();speakVocab('${{w.word.replace(/'/g,"\\\\'")}}')" class="text-sm bg-white/10 rounded-full w-8 h-8 flex items-center justify-center border-none cursor-pointer">🔊</button>
    </div>`).join('')
    +(words.length>100?`<p class="text-center text-white/40 text-xs mt-2">Showing 100 of ${{words.length}}. Use categories to filter.</p>`:'');
}}
function setVocabFilter(cat){{vocabFilter=cat;renderVocab();}}
function speakVocab(word){{const u=new SpeechSynthesisUtterance(word);u.lang='de-DE';u.rate=0.8;speechSynthesis.speak(u);}}

// === CLOCK ===
function updateClock(){{
  const now=new Date();
  document.getElementById('clock').textContent=now.getHours().toString().padStart(2,'0')+':'+now.getMinutes().toString().padStart(2,'0');
}}
setInterval(updateClock,10000);updateClock();

// === INIT ===
if(alarms.length===0){{
  alarms=[
    {{id:'1',time:'07:30',label:'Wake German',days:[1,2,3,4,5],enabled:true,voiceChallenge:true}},
    {{id:'2',time:'22:00',label:'Evening Review',days:[0,6],enabled:true,voiceChallenge:true}}
  ];
  localStorage.setItem('s2w_alarms',JSON.stringify(alarms));
}}
renderHome();
</script>
</body>
</html>'''

with open(r'e:\Kethoatngheo\speak2wake\previews\index.html', 'w', encoding='utf-8') as f:
    f.write(html)

print(f"HTML demo written! Size: {len(html)} chars")
