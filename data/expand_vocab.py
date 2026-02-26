import json

# Load existing
with open(r'e:\Kethoatngheo\speak2wake\data\vocabulary-de-a1a2.json', 'r', encoding='utf-8') as f:
    existing = json.load(f)

next_id = len(existing) + 1

new_words = [
    # === NUMBERS 11-20 (10 words) ===
    ("elf","","elf","eleven","A1","elf","numbers"),
    ("zwölf","","zwölf","twelve","A1","tsverlf","numbers"),
    ("dreizehn","","dreizehn","thirteen","A1","dry-tsayn","numbers"),
    ("vierzehn","","vierzehn","fourteen","A1","feer-tsayn","numbers"),
    ("fünfzehn","","fünfzehn","fifteen","A1","fewnf-tsayn","numbers"),
    ("zwanzig","","zwanzig","twenty","A1","tsvahn-tsig","numbers"),
    ("dreißig","","dreißig","thirty","A1","dry-sig","numbers"),
    ("vierzig","","vierzig","forty","A1","feer-tsig","numbers"),
    ("hundert","","hundert","hundred","A1","hoon-dert","numbers"),
    ("tausend","","tausend","thousand","A2","tow-zent","numbers"),

    # === MORE GREETINGS (5) ===
    ("Wie geht's","","Wie geht's","how are you","A1","vee gayts","greetings"),
    ("Guten Abend","","Guten Abend","good evening","A1","goo-ten ah-bent","greetings"),
    ("Gute Nacht","","Gute Nacht","good night","A1","goo-teh nakht","greetings"),
    ("Willkommen","","Willkommen","welcome","A1","vil-koh-men","greetings"),
    ("Prost","","Prost","cheers","A2","prohst","greetings"),

    # === MORE FAMILY (5) ===
    ("die Frau","Frau","die","the woman / wife","A1","frow","family"),
    ("die Großmutter","Großmutter","die","the grandmother","A1","grohs-moo-ter","family"),
    ("der Großvater","Großvater","der","the grandfather","A1","grohs-fah-ter","family"),
    ("der Onkel","Onkel","der","the uncle","A2","ohn-kel","family"),
    ("die Tante","Tante","die","the aunt","A2","tahn-teh","family"),

    # === MORE FOOD (20) ===
    ("das Fleisch","Fleisch","das","the meat","A1","flysh","food"),
    ("der Reis","Reis","der","the rice","A1","rice","food"),
    ("die Kartoffel","Kartoffel","die","the potato","A1","kar-toh-fel","food"),
    ("die Tomate","Tomate","die","the tomato","A1","toh-mah-teh","food"),
    ("die Gurke","Gurke","die","the cucumber","A1","goor-keh","food"),
    ("die Zwiebel","Zwiebel","die","the onion","A2","tsvee-bel","food"),
    ("der Salat","Salat","der","the salad","A1","zah-laht","food"),
    ("die Suppe","Suppe","die","the soup","A1","zoo-peh","food"),
    ("das Hähnchen","Hähnchen","das","the chicken","A2","hayn-khen","food"),
    ("die Wurst","Wurst","die","the sausage","A1","voorst","food"),
    ("der Kuchen","Kuchen","der","the cake","A1","koo-khen","food"),
    ("das Eis","Eis","das","the ice cream","A1","ice","food"),
    ("die Schokolade","Schokolade","die","the chocolate","A1","shoh-koh-lah-deh","food"),
    ("der Saft","Saft","der","the juice","A1","zahft","food"),
    ("das Bier","Bier","das","the beer","A1","beer","food"),
    ("der Wein","Wein","der","the wine","A2","vine","food"),
    ("die Banane","Banane","die","the banana","A1","bah-nah-neh","food"),
    ("die Orange","Orange","die","the orange","A1","oh-rahn-zheh","food"),
    ("die Erdbeere","Erdbeere","die","the strawberry","A2","airt-bay-reh","food"),
    ("die Karotte","Karotte","die","the carrot","A2","kah-roh-teh","food"),

    # === MORE ANIMALS (10) ===
    ("der Elefant","Elefant","der","the elephant","A2","eh-leh-fahnt","animals"),
    ("der Löwe","Löwe","der","the lion","A2","ler-veh","animals"),
    ("der Affe","Affe","der","the monkey","A2","ah-feh","animals"),
    ("der Hase","Hase","der","the rabbit","A1","hah-zeh","animals"),
    ("die Ente","Ente","die","the duck","A1","en-teh","animals"),
    ("der Schmetterling","Schmetterling","der","the butterfly","A2","shmet-ter-ling","animals"),
    ("die Biene","Biene","die","the bee","A2","bee-neh","animals"),
    ("der Wolf","Wolf","der","the wolf","A2","volf","animals"),
    ("die Schildkröte","Schildkröte","die","the turtle","A2","shilt-krer-teh","animals"),
    ("der Frosch","Frosch","der","the frog","A2","frosh","animals"),

    # === MORE BODY (5) ===
    ("der Rücken","Rücken","der","the back","A2","rew-ken","body"),
    ("der Hals","Hals","der","the neck","A2","hahls","body"),
    ("die Schulter","Schulter","die","the shoulder","A2","shool-ter","body"),
    ("das Knie","Knie","das","the knee","A2","k-nee","body"),
    ("der Zahn","Zahn","der","the tooth","A1","tsahn","body"),

    # === MORE HOME (5) ===
    ("der Spiegel","Spiegel","der","the mirror","A2","shpee-gel","home"),
    ("der Schrank","Schrank","der","the closet","A2","shrahnk","home"),
    ("die Treppe","Treppe","die","the stairs","A2","trep-peh","home"),
    ("das Dach","Dach","das","the roof","A2","dahkh","home"),
    ("der Keller","Keller","der","the basement","A2","kel-ler","home"),

    # === MORE WEATHER (5) ===
    ("der Nebel","Nebel","der","the fog","A2","nay-bel","weather"),
    ("das Gewitter","Gewitter","das","the thunderstorm","A2","geh-vit-ter","weather"),
    ("der Regenbogen","Regenbogen","der","the rainbow","A2","ray-gen-boh-gen","weather"),
    ("der Frost","Frost","der","the frost","A2","frosst","weather"),
    ("die Hitze","Hitze","die","the heat","A2","hit-seh","weather"),

    # === MORE VERBS (30) ===
    ("sprechen","","sprechen","to speak","A1","shprekh-en","verbs"),
    ("lesen","","lesen","to read","A1","lay-zen","verbs"),
    ("schreiben","","schreiben","to write","A1","shry-ben","verbs"),
    ("hören","","hören","to hear","A1","her-en","verbs"),
    ("sehen","","sehen","to see","A1","zay-en","verbs"),
    ("verstehen","","verstehen","to understand","A1","fer-shtay-en","verbs"),
    ("wissen","","wissen","to know","A1","vis-sen","verbs"),
    ("denken","","denken","to think","A2","den-ken","verbs"),
    ("glauben","","glauben","to believe","A2","glow-ben","verbs"),
    ("helfen","","helfen","to help","A1","hel-fen","verbs"),
    ("brauchen","","brauchen","to need","A1","brow-khen","verbs"),
    ("bekommen","","bekommen","to get","A2","beh-koh-men","verbs"),
    ("bringen","","bringen","to bring","A1","bring-en","verbs"),
    ("nehmen","","nehmen","to take","A1","nay-men","verbs"),
    ("geben","","geben","to give","A1","gay-ben","verbs"),
    ("fahren","","fahren","to drive","A1","fah-ren","verbs"),
    ("fliegen","","fliegen","to fly","A2","flee-gen","verbs"),
    ("schwimmen","","schwimmen","to swim","A1","shvim-men","verbs"),
    ("laufen","","laufen","to run","A1","low-fen","verbs"),
    ("sitzen","","sitzen","to sit","A1","zit-sen","verbs"),
    ("stehen","","stehen","to stand","A1","shtay-en","verbs"),
    ("liegen","","liegen","to lie down","A2","lee-gen","verbs"),
    ("öffnen","","öffnen","to open","A1","erf-nen","verbs"),
    ("schließen","","schließen","to close","A2","shlee-sen","verbs"),
    ("beginnen","","beginnen","to begin","A2","beh-gin-nen","verbs"),
    ("vergessen","","vergessen","to forget","A2","fer-ges-sen","verbs"),
    ("erinnern","","erinnern","to remember","A2","er-in-nern","verbs"),
    ("versuchen","","versuchen","to try","A2","fer-zoo-khen","verbs"),
    ("warten","","warten","to wait","A1","var-ten","verbs"),
    ("zeigen","","zeigen","to show","A2","tsy-gen","verbs"),

    # === CLOTHING (25) ===
    ("das Hemd","Hemd","das","the shirt","A1","hemt","clothing"),
    ("die Hose","Hose","die","the pants","A1","hoh-zeh","clothing"),
    ("der Rock","Rock","der","the skirt","A1","rok","clothing"),
    ("das Kleid","Kleid","das","the dress","A1","klite","clothing"),
    ("die Jacke","Jacke","die","the jacket","A1","yah-keh","clothing"),
    ("der Mantel","Mantel","der","the coat","A1","mahn-tel","clothing"),
    ("der Schuh","Schuh","der","the shoe","A1","shoo","clothing"),
    ("die Socke","Socke","die","the sock","A1","zoh-keh","clothing"),
    ("der Hut","Hut","der","the hat","A1","hoot","clothing"),
    ("die Mütze","Mütze","die","the cap","A2","mew-tseh","clothing"),
    ("der Schal","Schal","der","the scarf","A2","shahl","clothing"),
    ("die Handschuhe","Handschuhe","die","the gloves","A2","hahnt-shoo-eh","clothing"),
    ("der Gürtel","Gürtel","der","the belt","A2","gewr-tel","clothing"),
    ("die Brille","Brille","die","the glasses","A1","bril-leh","clothing"),
    ("der Pullover","Pullover","der","the sweater","A1","pool-oh-ver","clothing"),
    ("das T-Shirt","T-Shirt","das","the t-shirt","A1","tee-shirt","clothing"),
    ("die Bluse","Bluse","die","the blouse","A2","bloo-zeh","clothing"),
    ("der Anzug","Anzug","der","the suit","A2","ahn-tsook","clothing"),
    ("die Krawatte","Krawatte","die","the tie","A2","krah-vah-teh","clothing"),
    ("der Stiefel","Stiefel","der","the boot","A2","shtee-fel","clothing"),
    ("die Unterwäsche","Unterwäsche","die","the underwear","A2","oon-ter-veh-sheh","clothing"),
    ("der Regenschirm","Regenschirm","der","the umbrella","A2","ray-gen-shirm","clothing"),
    ("die Tasche","Tasche","die","the bag","A1","tah-sheh","clothing"),
    ("der Ring","Ring","der","the ring","A2","ring","clothing"),
    ("die Uhr","Uhr","die","the watch","A1","oor","clothing"),

    # === TRANSPORT (20) ===
    ("das Auto","Auto","das","the car","A1","ow-toh","transport"),
    ("der Bus","Bus","der","the bus","A1","boos","transport"),
    ("der Zug","Zug","der","the train","A1","tsook","transport"),
    ("das Fahrrad","Fahrrad","das","the bicycle","A1","fahr-raht","transport"),
    ("das Flugzeug","Flugzeug","das","the airplane","A1","flook-tsoyk","transport"),
    ("das Schiff","Schiff","das","the ship","A2","shif","transport"),
    ("die Straße","Straße","die","the street","A1","shtrah-seh","transport"),
    ("die U-Bahn","U-Bahn","die","the subway","A1","oo-bahn","transport"),
    ("die Haltestelle","Haltestelle","die","the stop","A1","hahl-teh-shtel-leh","transport"),
    ("der Bahnhof","Bahnhof","der","the train station","A1","bahn-hohf","transport"),
    ("der Flughafen","Flughafen","der","the airport","A1","flook-hah-fen","transport"),
    ("das Taxi","Taxi","das","the taxi","A1","tahk-see","transport"),
    ("die Ampel","Ampel","die","the traffic light","A2","ahm-pel","transport"),
    ("der Parkplatz","Parkplatz","der","the parking lot","A2","park-plahts","transport"),
    ("der Führerschein","Führerschein","der","the driver's license","A2","few-rer-shine","transport"),
    ("das Benzin","Benzin","das","the gasoline","A2","ben-tseen","transport"),
    ("die Brücke","Brücke","die","the bridge","A2","brew-keh","transport"),
    ("der Weg","Weg","der","the way / path","A1","vayk","transport"),
    ("die Kreuzung","Kreuzung","die","the intersection","A2","kroy-tsoong","transport"),
    ("der Stau","Stau","der","the traffic jam","A2","shtow","transport"),

    # === TIME & DATES (20) ===
    ("die Stunde","Stunde","die","the hour","A1","shtoon-deh","time"),
    ("die Minute","Minute","die","the minute","A1","mee-noo-teh","time"),
    ("die Sekunde","Sekunde","die","the second","A2","zeh-koon-deh","time"),
    ("der Tag","Tag","der","the day","A1","tahk","time"),
    ("die Woche","Woche","die","the week","A1","voh-kheh","time"),
    ("der Monat","Monat","der","the month","A1","moh-naht","time"),
    ("das Jahr","Jahr","das","the year","A1","yahr","time"),
    ("heute","","heute","today","A1","hoy-teh","time"),
    ("gestern","","gestern","yesterday","A1","ges-tern","time"),
    ("morgen","","morgen","tomorrow","A1","mor-gen","time"),
    ("Montag","","Montag","Monday","A1","mohn-tahk","time"),
    ("Dienstag","","Dienstag","Tuesday","A1","deens-tahk","time"),
    ("Mittwoch","","Mittwoch","Wednesday","A1","mit-vokh","time"),
    ("Donnerstag","","Donnerstag","Thursday","A1","don-ners-tahk","time"),
    ("Freitag","","Freitag","Friday","A1","fry-tahk","time"),
    ("Samstag","","Samstag","Saturday","A1","zahms-tahk","time"),
    ("Sonntag","","Sonntag","Sunday","A1","zon-tahk","time"),
    ("der Morgen","Morgen","der","the morning","A1","mor-gen","time"),
    ("der Abend","Abend","der","the evening","A1","ah-bent","time"),
    ("die Nacht","Nacht","die","the night","A1","nakht","time"),

    # === SCHOOL & WORK (20) ===
    ("die Schule","Schule","die","the school","A1","shoo-leh","school"),
    ("der Lehrer","Lehrer","der","the teacher (m)","A1","lay-rer","school"),
    ("die Lehrerin","Lehrerin","die","the teacher (f)","A1","lay-reh-rin","school"),
    ("der Schüler","Schüler","der","the student (m)","A1","shew-ler","school"),
    ("das Buch","Buch","das","the book","A1","bookh","school"),
    ("der Stift","Stift","der","the pen","A1","shtift","school"),
    ("das Heft","Heft","das","the notebook","A1","heft","school"),
    ("die Tafel","Tafel","die","the blackboard","A1","tah-fel","school"),
    ("der Computer","Computer","der","the computer","A1","kohm-pyoo-ter","school"),
    ("das Papier","Papier","das","the paper","A1","pah-peer","school"),
    ("die Aufgabe","Aufgabe","die","the task","A2","owf-gah-beh","school"),
    ("die Prüfung","Prüfung","die","the exam","A2","prew-foong","school"),
    ("die Arbeit","Arbeit","die","the work","A1","ar-bite","school"),
    ("das Büro","Büro","das","the office","A1","bew-roh","school"),
    ("der Chef","Chef","der","the boss","A2","shef","school"),
    ("die Firma","Firma","die","the company","A2","feer-mah","school"),
    ("das Geld","Geld","das","the money","A1","gelt","school"),
    ("der Beruf","Beruf","der","the profession","A2","beh-roof","school"),
    ("die Universität","Universität","die","the university","A2","oo-nee-ver-zee-tayt","school"),
    ("die Klasse","Klasse","die","the class","A1","klah-seh","school"),

    # === ADJECTIVES (30) ===
    ("groß","","groß","big / tall","A1","grohs","adjectives"),
    ("klein","","klein","small","A1","kline","adjectives"),
    ("gut","","gut","good","A1","goot","adjectives"),
    ("schlecht","","schlecht","bad","A1","shlekht","adjectives"),
    ("schön","","schön","beautiful","A1","shern","adjectives"),
    ("hässlich","","hässlich","ugly","A2","hes-likh","adjectives"),
    ("schnell","","schnell","fast","A1","shnel","adjectives"),
    ("langsam","","langsam","slow","A1","lahng-zahm","adjectives"),
    ("alt","","alt","old","A1","ahlt","adjectives"),
    ("jung","","jung","young","A1","yoong","adjectives"),
    ("neu","","neu","new","A1","noy","adjectives"),
    ("teuer","","teuer","expensive","A1","toy-er","adjectives"),
    ("billig","","billig","cheap","A1","bil-lig","adjectives"),
    ("schwer","","schwer","heavy / difficult","A1","shvair","adjectives"),
    ("leicht","","leicht","light / easy","A1","lykht","adjectives"),
    ("lang","","lang","long","A1","lahng","adjectives"),
    ("kurz","","kurz","short","A1","koorts","adjectives"),
    ("heiß","","heiß","hot","A1","hice","adjectives"),
    ("kalt","","kalt","cold","A1","kahlt","adjectives"),
    ("richtig","","richtig","correct","A1","rikh-tig","adjectives"),
    ("falsch","","falsch","wrong","A1","fahlsh","adjectives"),
    ("müde","","müde","tired","A1","mew-deh","adjectives"),
    ("hungrig","","hungrig","hungry","A1","hoong-rig","adjectives"),
    ("durstig","","durstig","thirsty","A2","door-stig","adjectives"),
    ("lustig","","lustig","funny","A2","loos-tig","adjectives"),
    ("traurig","","traurig","sad","A1","trow-rig","adjectives"),
    ("glücklich","","glücklich","happy","A1","glewk-likh","adjectives"),
    ("wichtig","","wichtig","important","A2","vikh-tig","adjectives"),
    ("einfach","","einfach","simple / easy","A1","ine-fahkh","adjectives"),
    ("schwierig","","schwierig","difficult","A2","shvee-rig","adjectives"),

    # === CITY & PLACES (20) ===
    ("die Stadt","Stadt","die","the city","A1","shtaht","places"),
    ("das Dorf","Dorf","das","the village","A2","dorf","places"),
    ("der Markt","Markt","der","the market","A1","markt","places"),
    ("die Kirche","Kirche","die","the church","A2","keer-kheh","places"),
    ("das Museum","Museum","das","the museum","A2","moo-zay-oom","places"),
    ("der Park","Park","der","the park","A1","park","places"),
    ("die Bank","Bank","die","the bank","A1","bahnk","places"),
    ("die Post","Post","die","the post office","A1","post","places"),
    ("die Apotheke","Apotheke","die","the pharmacy","A2","ah-poh-tay-keh","places"),
    ("das Krankenhaus","Krankenhaus","das","the hospital","A2","krahnk-en-hows","places"),
    ("das Restaurant","Restaurant","das","the restaurant","A1","res-toh-rahng","places"),
    ("das Hotel","Hotel","das","the hotel","A1","hoh-tel","places"),
    ("der Supermarkt","Supermarkt","der","the supermarket","A1","zoo-per-markt","places"),
    ("die Bäckerei","Bäckerei","die","the bakery","A2","bek-er-eye","places"),
    ("die Bibliothek","Bibliothek","die","the library","A2","bib-lee-oh-tayk","places"),
    ("das Kino","Kino","das","the cinema","A1","kee-noh","places"),
    ("das Theater","Theater","das","the theater","A2","tay-ah-ter","places"),
    ("der Spielplatz","Spielplatz","der","the playground","A2","shpeel-plahts","places"),
    ("die Polizei","Polizei","die","the police","A2","poh-lee-tsy","places"),
    ("der Laden","Laden","der","the shop","A1","lah-den","places"),

    # === NATURE (20) ===
    ("der Baum","Baum","der","the tree","A1","bowm","nature"),
    ("die Blume","Blume","die","the flower","A1","bloo-meh","nature"),
    ("der Berg","Berg","der","the mountain","A1","bairk","nature"),
    ("der Fluss","Fluss","der","the river","A2","floos","nature"),
    ("der See","See","der","the lake","A2","zay","nature"),
    ("das Meer","Meer","das","the sea","A1","mair","nature"),
    ("der Wald","Wald","der","the forest","A1","vahlt","nature"),
    ("die Wiese","Wiese","die","the meadow","A2","vee-zeh","nature"),
    ("der Strand","Strand","der","the beach","A2","shtrahnt","nature"),
    ("die Insel","Insel","die","the island","A2","in-zel","nature"),
    ("der Stein","Stein","der","the stone","A2","shtine","nature"),
    ("die Erde","Erde","die","the earth","A2","air-deh","nature"),
    ("der Himmel","Himmel","der","the sky","A1","him-mel","nature"),
    ("die Luft","Luft","die","the air","A2","looft","nature"),
    ("das Feuer","Feuer","das","the fire","A2","foy-er","nature"),
    ("das Gras","Gras","das","the grass","A2","grahs","nature"),
    ("die Rose","Rose","die","the rose","A2","roh-zeh","nature"),
    ("der Stern","Stern","der","the star","A1","shtern","nature"),
    ("der Mond","Mond","der","the moon","A1","mohnt","nature"),
    ("die Sonne","Sonne","die","the sun","A1","zon-neh","nature"),

    # === EMOTIONS & EXPRESSIONS (20) ===
    ("die Liebe","Liebe","die","the love","A2","lee-beh","emotions"),
    ("die Angst","Angst","die","the fear","A2","ahngst","emotions"),
    ("die Freude","Freude","die","the joy","A2","froy-deh","emotions"),
    ("der Spaß","Spaß","der","the fun","A1","shpahs","emotions"),
    ("die Hoffnung","Hoffnung","die","the hope","A2","hof-noong","emotions"),
    ("die Sorge","Sorge","die","the worry","A2","zor-geh","emotions"),
    ("der Traum","Traum","der","the dream","A2","trowm","emotions"),
    ("das Glück","Glück","das","the luck","A2","glewk","emotions"),
    ("die Ruhe","Ruhe","die","the calm","A2","roo-eh","emotions"),
    ("lachen","","lachen","to laugh","A1","lah-khen","emotions"),
    ("weinen","","weinen","to cry","A1","vy-nen","emotions"),
    ("lieben","","lieben","to love","A1","lee-ben","emotions"),
    ("hassen","","hassen","to hate","A2","hah-sen","emotions"),
    ("fühlen","","fühlen","to feel","A2","few-len","emotions"),
    ("wünschen","","wünschen","to wish","A2","vewn-shen","emotions"),
    ("hoffen","","hoffen","to hope","A2","hoh-fen","emotions"),
    ("freuen","","freuen","to be happy","A2","froy-en","emotions"),
    ("ärgern","","ärgern","to annoy","A2","air-gern","emotions"),
    ("überraschen","","überraschen","to surprise","A2","ew-ber-rah-shen","emotions"),
    ("danken","","danken","to thank","A1","dahn-ken","emotions"),

    # === DAILY ROUTINE (20) ===
    ("aufstehen","","aufstehen","to get up","A1","owf-shtay-en","daily"),
    ("aufwachen","","aufwachen","to wake up","A1","owf-vah-khen","daily"),
    ("duschen","","duschen","to shower","A1","doo-shen","daily"),
    ("frühstücken","","frühstücken","to have breakfast","A1","frew-shtew-ken","daily"),
    ("kochen","","kochen","to cook","A1","kokh-en","daily"),
    ("putzen","","putzen","to clean","A1","poot-sen","daily"),
    ("waschen","","waschen","to wash","A1","vah-shen","daily"),
    ("schlafen","","schlafen","to sleep","A1","shlah-fen","daily"),
    ("träumen","","träumen","to dream","A2","troy-men","daily"),
    ("anziehen","","anziehen","to get dressed","A2","ahn-tsee-en","daily"),
    ("ausziehen","","ausziehen","to get undressed","A2","ows-tsee-en","daily"),
    ("einkaufen","","einkaufen","to shop","A1","ine-kow-fen","daily"),
    ("spazieren","","spazieren","to walk","A2","shpah-tsee-ren","daily"),
    ("fernsehen","","fernsehen","to watch TV","A1","fern-zay-en","daily"),
    ("telefonieren","","telefonieren","to phone","A2","teh-leh-foh-nee-ren","daily"),
    ("spielen","","spielen","to play","A1","shpee-len","daily"),
    ("lernen","","lernen","to learn","A1","ler-nen","daily"),
    ("arbeiten","","arbeiten","to work","A1","ar-by-ten","daily"),
    ("essen","","essen","to eat","A1","es-sen","daily"),
    ("trinken","","trinken","to drink","A1","trin-ken","daily"),

    # === HEALTH (15) ===
    ("der Arzt","Arzt","der","the doctor (m)","A1","artst","health"),
    ("die Ärztin","Ärztin","die","the doctor (f)","A1","airts-tin","health"),
    ("die Medizin","Medizin","die","the medicine","A2","meh-dee-tseen","health"),
    ("der Schmerz","Schmerz","der","the pain","A2","shmairts","health"),
    ("das Fieber","Fieber","das","the fever","A2","fee-ber","health"),
    ("der Husten","Husten","der","the cough","A2","hoo-sten","health"),
    ("der Schnupfen","Schnupfen","der","the cold","A2","shnoop-fen","health"),
    ("die Tablette","Tablette","die","the pill","A2","tah-blet-teh","health"),
    ("gesund","","gesund","healthy","A1","geh-zoont","health"),
    ("krank","","krank","sick","A1","krahnk","health"),
    ("der Kopfschmerz","Kopfschmerz","der","the headache","A2","kopf-shmairts","health"),
    ("die Erkältung","Erkältung","die","the cold (illness)","A2","er-kel-toong","health"),
    ("der Termin","Termin","der","the appointment","A2","ter-meen","health"),
    ("die Apotheke","Apotheke","die","the pharmacy","A2","ah-poh-tay-keh","health"),
    ("das Rezept","Rezept","das","the prescription","A2","reh-tsept","health"),
]

for w in new_words:
    word, bare, article, translation, level, phonetic, category = w
    # Handle article format
    if article in ('der','die','das'):
        display = f"{article} {bare}" if bare else word
    else:
        display = word
        bare = word if not bare else bare
        article = article if article else ""

    entry = {
        "id": f"de-{next_id:03d}",
        "word": display if article in ('der','die','das') else word,
        "bare": bare if bare else word,
        "article": article,
        "translation": translation,
        "level": level,
        "phonetic": phonetic,
        "category": category
    }
    existing.append(entry)
    next_id += 1

# Fix entries where article handling was wrong
for e in existing:
    if not e.get('bare'):
        e['bare'] = e['word']

print(f"Total words: {len(existing)}")

# Count by category
cats = {}
for w in existing:
    c = w['category']
    cats[c] = cats.get(c, 0) + 1
for c, n in sorted(cats.items()):
    print(f"  {c}: {n}")

with open(r'e:\Kethoatngheo\speak2wake\data\vocabulary-de-a1a2.json', 'w', encoding='utf-8') as f:
    json.dump(existing, f, ensure_ascii=False, indent=2)

print("Done! File saved.")
