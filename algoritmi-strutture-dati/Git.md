# Uso di Git per il laboratorio di Algoritmi e Strutture Dati

In questo file si forniscono brevi indicazioni su come usare Git per lo
sviluppo del laboratorio di Algoritmi e Strutture Dati. Git è un sistema di
versioning del software maturo e ricco di funzionalità, qui si farà un breve
cenno alle sole funzionalità indispensabili per portare a termine quanto
richiesto dal corso, lo studente è invitato ad approfondire le parti di suo
interesse consultando il libro open source [Pro Git][].


## Configurazione Git

Git dovrebbe essere già installato e pronto per l'uso sulle macchine che
utilizzerete per il laboratorio. Prima di incominciare, solo nel caso non
abbiate  mai effettuato questa operazione in precedenza, è necessario però che
forniate a Git i dati necessari per tracciare i [commit][] che farete. Per
fare questo aprite un terminale e eseguite i seguenti comandi (nel seguito
  si assume che l'utente che digita i comandi si chiami Mario Rossi e abbia
  mail `mario.rossi@di.unito.it`):

```bash
git config --global user.name "Mario Rossi"
git config --global user.email mario.rossi@di.unito.it
```

Vi suggeriamo inoltre di attivare il supporto alla visualizzazione a colori dei
risultati dei comandi git. Per far ciò usate il seguente comando:

```bash
git config --global ui.color true
```

## Configurazione GitLab

GitLab è la piattaforma web adottata dal Dipartimento di Informatica per offrire
agli studenti la possibilità di collaborare via Git usando un'interaccia simile
a quella offerta dalla piattaforma GitHub. Il server GitLab offerto dal dipartimento
è accessibile all'indirizzo:

[https://gitlab2.educ.di.unito.it](https://gitlab2.educ.di.unito.it)

**Nota**: Il server gitlab.educ.di.unito.it è in via di dismissione e *non* deve essere
usato per nuovi progetti.

Al fine di poter creare copie locali dei repository presenti su GitLab è
necessario configurare la propria macchina in modo tale che possa accedere al
repository remoto. Git permette di accedere al repository remoto sia tramite ssh,
sia tramite https.

# Accesso via ssh (laboratorio Dijkstra)

Per configurare l'accesso via ssh è necessario creare una coppia di chiavi
crittografiche e inserire la chiave pubblica tra quelle accettate dal vostro
utente su GitLab.

Per far questo accedere alla vostra pagina su GitLab e cliccate sull'icona
del vostro profilo (in alto a destra) e quindi su "Settings".

Sulla pagina che si aprirà cliccate su "SSH Keys" e seguite le istruzioni
a video (in particolare siate certi di consultare la sezione che spiega come
[generare le chiavi ssh](https://gitlab2.educ.di.unito.it/help/ssh/README).
Nota: in GitLab esistono due tipi di chiavi crittografiche che si possono
caricare. La chiave generica per accedere e modificare il repository e una
chiave di "Deploy" che può venire usata in sistemi di "Continuous Integration".
A voi serve seguire le istruzioni per creare quelle che sono chiamate
semplicemente chiavi "SSH".

# Accesso via https (laboratorio Dijkstra e Von Neumann)

Per accedere via https, è necessario istruire git a utilizzare il proxy dei
laboratori usando il seguente comando:

```
git config --global http.proxy 172.16.0.254:3128
```

## Clonazione del progetto

Ognuno di voi ha a disposizione un utente su un'istanza della piattaforma gitlab
ospitata dai server del Dipartimento di Informatica. Alla piattaforma si può
accedere tramite browser aprendo l'URL:
[gitlab2.educ.di.unito.it](gitlab2.educ.di.unito.it).

Una volta effettuato il login, è possibile utilizzare l'interfaccia web per
creare una copia personale del repository su cui lavorerete. Un membro per ogni
gruppo acceda all'url:
  [https://gitlab2.educ.di.unito.it/audrito/laboratorio-algoritmi-2023-2024](https://gitlab2.educ.di.unito.it/audrito/laboratorio-algoritmi-2023-2024)
e clicchi sull'icona "Fork repository".

Di default, l'interfaccia web proporrà di impostare i permessi del repository
creato in modo tale da non permettere l'accesso a nessun altro utente che non
sia esplicitamente un membro del gruppo di sviluppatori del repository.  *Non*
cambiate questa impostazione, pena la possibilità di vedere l'elaborato annullato
perché qualcuno ne ha copiato il contenuto.

Aggiungete ora i membri del gruppo *e i docenti di laboratorio* al gruppo di
sviluppo del progetto con ruolo "developer" (trovate l'opzione seguendo i menù
Settings -> Members a partire dalla pagina del vostro repository). I nickname
dei docenti di laboratorio sono "pozzato", "magro", "audrito" e "micalizi".

L'avere a disposizione una copia del laboratorio non è ancora sufficiente per
poter iniziare a lavorare: è infatti necessario creare una copia locale alla
vostra macchina. Ogni membro del gruppo di lavoro si annoti la stringa di
accesso al repository che è stato clonato: è la stringa che potete leggere sulla pagina
del repository che avete creato; notate che potete scegliere tra l'accesso
via ssh e via https. Si effettui un clone locale alla propria macchina aprendo un
terminale e eseguendo il seguente comando:

```bash
  git clone GIT_REPO_REF
```

dove GIT_REPO_REF è la stringa di accesso menzionata.

*Nota accesso https:* la versione di git installata nei laboratori richiede di
inserire il proprio login seguito dal simbolo @ subito dopo l'indicazione del
protocollo. A titolo di esempio, riportiamo la stringa di accesso per il
repository proposto dai docenti: (nel vostro caso dovrete
sostituire l'URL con quello che vi siete annotati precedentemente):

```
 git clone https://audrito@gitlab2.educ.di.unito.it/audrito/laboratorio-algoritmi-2023-2024
```

## Comandi di base per lavorare

Git gvi permetterà di salvare il vostro lavoro e manterrà la storia delle  varie
versioni che avrete creato insieme all'indicazione di chi ha effettuato ogni
aggiornamento. Segue un elenco dei comandi principali che dovrete utilizzare:

- `status`: mostra lo stato del progetto. File  identici all'ultima versione
  salvata non vengono mostrati. File modificati, cancellati, o nuovi vengono
  visualizzati (con colori diversi se avete abilitato questa opzione).

- `log`: mostra un log dell'evoluzione del progetto. Nel caso il progetto
  sia stato clonato le informazioni includeranno la storia del progetto
  originale.

- `add`: aggiunge uno o più file alla "staging area". Il contenuto di questa
  area determinerà quali modifiche andranno a fare parte del prossimo commit. È
  necessario aggiungere esplicitamente tutti i file che sono stati modificati o
  creati.

- `rm`: rimuove uno o più file dalla copia locale e dal repository al prossimo commit.
È necessario aggiungere esplicitamente tutti i file che sono stati cancellati.

- `commit`: salva lo stato del progetto.

- `pull`: recupera lo stato del progetto "remoto" associato alla copia corrente
  e lo fonde con la copia attuale.

- `push`: salva le modifiche fatte al repository locale integrandole nel
  repository remoto.

Esempio di ciclo di lavoro sul repository del progetto:

```bash
git pull # mi accerto che la copia locale sia sincronizzata con quella remota

... modifiche ai file del progetto ...

git add <lista dei file modificati>
git commit -m "messaggio che descrive le modifiche apportate"
git push # aggiornamento della copia remota
```


[Pro Git]: https://progit.org
[commit]: http://git-scm.com/book/en/v2/Git-Basics-Recording-Changes-to-the-Repository

## Aggiornamento della propria versione del progetto

Il progetto fornito dai docenti può subire variazioni e miglioramenti. Per
poter aggiornare la propria versione all'ultima pubblicata dai docenti è
necessario procedere come segue. Si apra una shell e ci si posizioni nella
directory che contiene la copia locale del proprio repository. Vogliamo
effettuare le due seguenti operazioni (l'ordine è importante):

1. aggiornare la propria copia locale a partire dalla copia del docente;
2. aggiornare la nostra copia remota (su GitLab) a partire dalla nostra copia
   locale.

Passo 1: effettuare un pull del progetto dei docenti utilizzando il seguente
         comando:

```bash
git pull git@gitlab2.educ.di.unito.it:audrito/laboratorio-algoritmi-2023-2024.git master
```

Passo 2: effettuare il push della propria versione locale sul proprio repository
         GitLab:

```bash
git push
```

## Branching e Merging con Git

Un branch è una linea di sviluppo di un certo repository. Git rende molto
semplice lavorare su più branch e effettuare il merge (i.e., la fusione) di
quanto sviluppato in due branch distinti.

L'utilizzo di branch è molto utile per diverse ragioni, ad esempio:
- permette di non interferire con il lavoro dei colleghi; fino a quando non
  effettuerò il merge delle mie modifiche sul branch "master" gli altri colleghi
  potranno continuare a lavorare come se io non avessi modificato nulla;
- permette di lavorare in modo più "atomico", la linea di sviluppo principale
  viene aggiornata solo quando una feature è pronta per essere integrata;
- permette di organizzare meglio il lavoro; ad esempio molti progetti mantengono
  un branch "stabile" (di solito chiamato master), uno di sviluppo e uno per
  le idee che si vogliono esplorare.

Prima di vedere i comandi per creare e modificare i branch è utile capire che
Git salva i commit come file contenenti il puntatore (in genere uno solo, ma
possono essere più di uno) al commit "genitore" insieme a qualche informazione
di contesto (come la mail di chi fa il commit e il messaggio di commit). Un
branch è semplicemente un "puntatore" a un commit. In Git, a  differenza di
altri sistemi di versioning, le operazioni di creazione, modifica e
cancellazione di un branch consistono nell'aggiornamento di tale puntatore e
sono pertanto operazioni poco onerose.

Segue un elenco dei comandi più utili per lavorare proficuamente con i branch.

Quando invocato senza ulteriori argomenti `git branch` riporta la lista dei
branch noti:

```
$ git branch
```

produce un output simile al seguente:

```
* branching
  master
  unity_tests
```

L'output mostrato indica che vi sono attualmente tre branch e che quello in uso è
"branching". Il branch in uso determina il contenuto della working directory. In
effetti, nel momento in cui un branch viene attivato (si fa il "checkout" del
branch), Git fa sì che la working directory sia aggiornata in modo da riflettere
il contenuto dell'ultimo commit effettuato sul branch attivato. Se questo non è
possibile (ad esempio perché l'operazione genera un conflitto che farebbe
perdere modifiche non ancora salvate con un commit) Git interrompe l'operazione
con un messaggio di errore. Un altro effetto di un checkout è di spostare la
HEAD in modo tale che punti al branch corrente. Questo implica che da questo
momento in poi un commit andrà a modificare il branch corrente piuttosto che
quello precedente.

Come menzionato, per lavorare su un particolare branch si usa il comando
"checkout" seguito dal nome del branch che si vuole attivare. Ad esempio:

```
$ git checkout master
```

attiva il branch 'master' e produce un output simile a quanto segue:

```
M	Git.md
Switched to branch 'master'
Your branch is up-to-date with 'origin/master'.
```

il messaggio ci informa che il checkout ha avuto successo e che però il file
`Git.md` era stato modificato ed è stato pertanto copiato nella working
directory in sostituzione di quello salvato nel branch master. Questo
comportamento è utile nei casi si inizi a lavorare su un particolare branch e
ci si accorga strada facendo che le modifiche sono più sostanziose di quanto
preventivato e che il lavoro merita di proseguire su un branch apposito. In
questo caso creando un nuovo branch e facendone il checkout ci ritroveremo
con tutte le modifiche già pronte per essere aggiunte al prossimo commit.

 Volendo controllare che il checkout abbia avuto successo, potremmo usare
 nuovamente `git branch` per vedere lo stato dei vari branch. Procedendo con
 questa operazione otterremo il seguente risultato (l'asterisco accanto al
 branch master indica che, correttamente, il branch attivo non è più
 `branching`):


```
branching
* master
unity_tests
```

Supponiamo ora di lavorare su un nuovo progetto su cui sono stati effettuati tre
commit. Il comando `git log --decorate --oneline --graph --all` mostra la storia
del progetto indicando dove is trova la HEAD e quali sono i branch attivi. In
particolare, l'opzione `--graph` indica a git di mostrare il grafo di come
si è evoluta la storia del progetto (git log usa per questo una forma di
ascii-art dove il simbolo `*` indica un commit e i simboli `/`, `\` e `|` sono
usati per mostrare le biforcazioni nella storia del progetto). Sul
nostro repository di prova il comando restituisce il seguente output:

```
* bbcbc23 (HEAD -> master) commit 2
* 59b95df commit 1
* 4f49020 initial commit
```

L'output mostra che sono stati effettuati tre commit, che il branch master punta
a "commit 2" e che la HEAD è agganciata al branch master. Le stringhe di lettere
e numeri accanto alle descrizioni contengono i primi otto caratteri della firma
sha1 dei commit: in Git ogni commit è identificato dalla sua firma sha1, è
possibile effettuare il checkout di un particolare commit indicando tale sigla,
es. `git checkout 59b95df` effettuerebbe il checkout del commit 1.
Supponiamo ora di voler lavorare su una feature e creiamo un nuovo branch usando
il comando:

```
git checkout -b feature1
```

Effettuiamo un commit ottenendo la seguente situazione:

```
* f252716 (HEAD -> feature1) commit 3
* bbcbc23 (master) commit 2
* 59b95df commit 1
* 4f49020 initial commit
```

*Nota*: il comando `checkout -b feature1` combina in un comando unico l'effetto
dei comandi `git branch feature1` (crea il branch `feature1`) e `git checkout
feature1`.

In questo momento il branch master non ha subito ulteriori commit e perciò è
collegato in linea diretta con il branch 'feature1'. Nel caso volessimo
effettuare il merge di master e feature1 Git semplicemente sposterebbe il
puntatore 'master' in modo da farlo puntare al 'commit 3'. Si dice in questo
caso che il merge si può fare in modo 'fast forward'.

Invece di effettuare il merge assumiamo invece che ci sia del lavoro da fare
su master. Lasciamo in sospeso quanto stiamo facendo su feature1 e Effettuiamo
il checkout di master seguito da un commit. Otterremo la seguente situazione:

```
* 884d3b5 (HEAD -> master) commit 4
| * f252716 (feature1) commit 3
|/
* bbcbc23 commit 2
* 59b95df commit 1
* 4f49020 initial commit
```

L'output mostra che feature1 e master contengono entrambi delle modifiche e
che pertanto un merge fast forward non è possibile. Se a questo punto volessimo
includere le modifiche apportate in feature1 dentro il branch master potremmo
procedere usando il comando `git merge feature1`. Questo comando effettuerà
un 'merge a tre vie'. In particolare discenderà nel grafo a partire dal branch
corrente (master) e dal branch indicato (feature1) fino a trovare un antenato
comune (commit2). A questo punto tenterà di fondere il contenuto di master, di
feature1 e del commit 2 e (in caso di successo) creerà un nuovo commit che avrà
come genitori sia master che feature1:

```
*   7464f65 (HEAD -> master) Merge branch 'feature1'
|\
| * f252716 (feature1) commit 3
* | 884d3b5 commit 4
|/
* bbcbc23 commit 2
* 59b95df commit 1
* 4f49020 initial commit
```

Si noti che feature1 è fermo al commit3 mentre il branch master è stato
spostato per farlo puntare al commit contenente il merge. Questo è corretto,
infatti il merge ha incluso in master le modifiche in feature1, mentre
feature1 è rimasto invariato. Si noti comunque che a questo punto feature1
è direttamente collegato con master e che quindi un merge feature1 produrrebbe
un merge fast forward:

```
*   7464f65 (HEAD -> feature1, master) Merge branch 'feature1'
|\
| * f252716 commit 3
* | 884d3b5 commit 4
|/
* bbcbc23 commit 2
* 59b95df commit 1
* 4f49020 initial commit
```

## Risoluzione dei conflitti

Se due branch modificano entrambi, in modo differente, la stessa porzione
di un file l'operazione di merge fallirà segnalando un conflitto. Assumiamo ad
esempio che, diversamente da prima, i commit 3 e 4 abbiano introdotto un
conflitto su uno dei file del progetto. In particolare assumiamo che gli
sviluppatori stessero modificando la funzione 'swap' inizialmente definita
come:

```java
/**
 * Swaps the element at index p1 with the element at index p2
 * @param array the array containing the elements
 * @param p1    position of the first element
 * @param p2    position of the second element
 */
void swap(ArrayList<T> array, int p1, int p2) {
  T tmp = array.get(p1);
  array.set(p1, array.get(p2));
  array.set(p2, tmp);
}
```

e che nel branch `feature1` lo sviluppatore abbia pensato che il nome delle
variabili fosse troppo criptico e abbia deciso quindi di modificarli nel
seguente modo:

```java
/**
 * Swaps the element at index p1 with the element at index p2
 * @param array the array containing the elements
 * @param pointer1    position of the first element
 * @param pointer2    position of the second element
 */
void swap(ArrayList<T> array, int pointer1, int pointer2) {
  T temporary = array.get(pointer1);
  array.set(pointer1, array.get(pointer2));
  array.set(pointer2, temporary);
}
```

mentre nel branch master lo sviluppatore abbia invece pensato che fosse
opportuno salvare qualche byte di prezioso spazio disco e abbia quindi
eliminato il commento javadoc e modificato la definizione come segue:

```java
void swap(ArrayList<T> a, int p, int q) {
  T t = a.get(p);
  a.set(p, array.get(p));
  a.set(p, t);
}
```

giustamente Git evita di prendere parte alla diatriba circa quale sia lo stile
migliore e pertanto nel momento del merge si rifiuta di completare l'operazione
emettendo il seguente messaggio:

```
Auto-merging f1.txt
CONFLICT (content): Merge conflict in f1.txt
Automatic merge failed; fix conflicts and then commit the result.
```

Prima di abortire Git modifica il file che contiene il conflitto inserendo
dei marcatori per indicare le porzioni in conflitto. Nel caso in esempio, il
testo della funzione swap è stato sostituito con:

```
<<<<<<< HEAD
  void swap(ArrayList<T> a, int p, int q) {
    T t = a.get(p);
    a.set(p, array.get(p));
    a.set(p, t);
=======
  /**
   * Swaps the element at index p1 with the element at index p2
   * @param array the array containing the elements
   * @param pointer1    position of the first element
   * @param pointer2    position of the second element
   */
  void swap(ArrayList<T> array, int pointer1, int pointer2) {
    T temporary = array.get(pointer1);
    array.set(pointer1, array.get(pointer2));
    array.set(pointer2, temporary);
>>>>>>> feature1
  }
```

Questa marcatura informa l'utente che la parte tra `<<<<<<< HEAD` e `=======` è
la versione "corrente" (i.e., quella presente nel branch in uso), mentre la
parte tra `=======` e `>>>>>>> feature1` è la versione presente nel branch
`feature1`. Per risolvere il conflitto è necessario modificare il file in
questione scegliendo la parte che si vuole mantenere (o effettuando una fusione
delle due parti). In questo caso noi decideremo di mantenere la versione nel
branch 'feature1' e modificheremo tale porzione del file eliminando le marcature
introdotte da Git e tutto ciò che compare nella parte 'HEAD', a fine lavoro
avremo sostituito il testo sopra riportato in modo da leggere:

```
  /**
   * Swaps the element at index p1 with the element at index p2
   * @param array the array containing the elements
   * @param p1    position of the first element
   * @param p2    position of the second element
   */
  void swap(ArrayList<T> array, int pointer1, int pointer2) {
    T temporary = array.get(pointer1);
    array.set(pointer1, array.get(pointer2));
    array.set(pointer2, temporary);
  }
```

Una volta salvato il file, per risolvere il conflitto è necessario effettuare
un nuovo commit che contiene le modifiche apportate (per effettuare il commit,
come sempre, dovremo effettuare lo staging del file tramite `git add` e quindi
effettuare il commit con `git commit`).

La situazione a valle del commit sarà:

```
*   1e9a44d (HEAD -> master) Fixed merge conflicts
|\
| * c34dd0d (feature1) commit 3
* | 17e2067 commit 4
|/
* f4d7159 commit 2
* 37c00b7 commit 1
* fb39147 Initial import
```

*Nota*: il processo sopra descritto è parzialmente automatizzato dal comando
`git mergetool`. Se invocato il comando aprirà un "diff" grafico su ognuno
dei file in conflitto e, una volta risolti tutti i conflitti, ci proporrà
una finestra per effettuare il commit.
