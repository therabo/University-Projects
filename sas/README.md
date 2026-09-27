# Cat & Ring 2024

Cat & Ring si propone come un’applicazione che permette di gestire una società di catering in tutti i suoi aspetti: organizzazione eventi e cucina.

## 1. Introduzione

La società di catering si occupa di fornire un servizio di pranzo/cena/aperitivo/buffet/coffee break nel contesto di eventi sociali o aziendali.

Essa arruola diverse figure: al livello più alto ci sono gli organizzatori, che gestiscono il personale e gli eventi; ci sono poi gli chef che stabiliscono i menu e supervisionano la cucina; i cuochi, che preparano il cibo, il personale di servizio, che si occupa del servizio durante l’evento stesso, e non si esclude di voler aggiungere in futuro altre figure.

Cat & Ring dovrà permettere agli organizzatori di dettagliare gli eventi e richiedere il personale che serve per realizzarli, assegnando a ciascuno dei compiti specifici. Inoltre, dovranno supervisionare le attività (quindi vedere i dettagli di tutti gli eventi attualmente in corso o terminati) ed inserire i dati del personale. Il personale dovrà poi inserire le proprie disponibilità nei turni definiti dall’organizzatore. Gli chef e i cuochi dovranno gestire un ricettario, creandone le ricette e definendo i menù da usare per i diversi eventi. In particolare, gli chef possono: gestire un ricettario, creare le ricette e definire i menù da usare per i diversi eventi, mentre i cuochi possono solo gestire il ricettario e creare le ricette, ma NON possono definire i menù da usare per gli eventi.

### Gli eventi

Un evento può essere semplice, e prevedere un singolo servizio (ad esempio un pranzo di matrimonio, una cena di festeggiamento, un buffet aziendale) o può essere più complesso, ad esempio durare un giorno intero o più giorni, e prevedere per ciascuna giornata più servizi (pranzo e cena, colazione e pranzo, coffee-break mattino e pomeriggio, ecc). Ciascun servizio avrà una precisa fascia oraria, e naturalmente un proprio menu e un proprio staff di supporto.

Gli eventi possono essere classificati come ricorrenti nel caso in cui si ripetano con una certa regolarità. Nel caso si organizzi un evento ricorrente (es. annuale) si deve tener traccia dei menù precedenti in modo da differenziare l’offerta dei piatti proposti o, se lo si desidera, ripeterla tale e quale. Per ogni evento si deve anche tener traccia del cliente che lo ha commissionato.

Ciascun evento o giornata di un evento, dal punto di vista della società di catering, prevede due momenti diversi di lavoro: il lavoro preparatorio che si svolge in sede, e il servizio, che si svolge nel luogo dell’evento, e che può andare dal semplice buffet all’allestimento di una vera e propria sala ristorante.

### I turni

Gli organizzatori e gli chef non hanno particolari turni, mentre le figure restanti hanno turni ben definiti, in quanto non ci si aspetta che lavorino a tempo pieno per la società.

Un turno è caratterizzato principalmente da una data, un luogo di svolgimento dell’attività e da una fascia oraria.

I turni sono di due tipi: turni preparatori, dove i cuochi lavorano a preparare le pietanze, e turni di servizio, dove lavora principalmente il personale di servizio (camerieri, lavapiatti, sommelier) e potenzialmente anche qualche cuoco, per realizzare il servizio stesso.

Per quanto riguarda i turni preparatori in sede, detti anche turni di cucina, essi sono inseriti dagli organizzatori a monte, a prescindere dagli eventi che si organizzeranno. Tendenzialmente si svolgono tutti nello stesso luogo, che dovrebbe dunque poter essere indicato una volta per tutte, ma non deve essere impossibile modificarlo in caso di necessità.

Tipicamente i turni preparatori si ripetono con una certa cadenza, salvo situazioni particolari o festività. Naturalmente è possibile modificare in corsa i turni preparatori, ad esempio cambiare l’orario di inizio o fine di uno o più turni, cancellarne alcuni, o aggiungerne altri inizialmente non previsti. Dovrebbe essere possibile svolgere queste operazioni sul singolo turno, o su tutti i turni con una certa caratteristica (ad esempio, modificare tutti i turni mattutini da una certa data in poi, o modificare tutti i turni del mercoledì pomeriggio, ecc). Le operazioni di modifica, aggiunta e cancellazione sono disponibili solo dalla data corrente.

I turni non sono più modificabili nel momento in cui ci sono disponibilità date dal personale di cucina o di servizio.

Una funzionalità aggiuntiva richiesta dalle aziende di catering su turni preparatori è che sia possibile creare dei raggruppamenti di turni: la logica del raggruppamento è che quando il personale dà la propria disponibilità o è disponibile per tutti i turni del gruppo o non lo è per nessuno.

I raggruppamenti possono essere “singoli” (ad es., raggruppa il turno di sabato 1 maggio mattina con quello di domenica 2 maggio mattina) o “ricorrenti” (ad es., raggruppa il turno di sabato mattina con quello di domenica mattina, per ogni settimana da qui al 31 dicembre).

In entrambi i casi il raggruppamento deve essere modificabile, e nel caso del raggruppamento ricorrente, dovrà essere possibile modificare sia il singolo gruppo (ad es, nell’ipotesi di aver raggruppato tutti i turni del sabato mattina con quelli della domenica mattina fino al 31 dicembre, si potrebbe decidere che sabato 1 maggio non si lavora e quindi solo in quel caso sostituirlo con la domenica pomeriggio) sia tutti i gruppi della ricorrenza (nell’esempio precedente, si potrebbe decidere di modificare tutti i gruppi da qui al 31 dicembre aggiungendo anche la domenica pomeriggio).

Il raggruppamento è modificabile solo finché non iniziano ad esserci disponibilità.

Per quanto riguarda i turni di servizio, vengono inseriti dagli organizzatori in seguito alla presa in carico di un evento coi relativi servizi; deve essere possibile per l’organizzatore indicare un evento già inserito nel sistema ed essere guidato dal sistema stesso nell’inserimento dei turni di servizio corrispondenti (anche qui ci può essere una regolarità se l’evento è ricorrente), eventualmente specificando un tempo aggiuntivo rispetto agli orari del servizio effettivo per la preparazione (prima) e per rigovernare (dopo), tempo aggiuntivo che potrà variare a seconda della circostanza e quindi dovrà essere stabilito sul momento dall’organizzatore. Successivamente all’aggiunta dei turni di servizio, deve essere possibile modificarli se serve, sempre solo finché non ci sono disponibilità date dal personale.

A differenza dei turni preparatori, possono esserci più turni di servizio con orari uguali o sovrapposti, perché essendo un catering i servizi si svolgono in sedi diverse, e dunque possono anche esserci servizi in parallelo.

I cuochi e il personale di servizio hanno accesso al calendario dei turni e possono dare (e in seguito ritirare) le proprie disponibilità. Solo quando sono chiamati per un turno di cucina (dallo chef) o per un turno di servizio (dall’organizzatore) a quel punto sono vincolati alla presenza e non possono più ritirare la disponibilità.

## 2. Organizzazione di un evento

Quando arriva una richiesta per un evento, uno degli organizzatori se ne fa carico. Egli dovrà creare una scheda per l’evento (specificando luogo, date, tipo di servizio per le varie giornate, numero di persone, ed eventuali note particolari), e affidare ciò che riguarda la cucina ad uno chef. A quel punto l’organizzatore segue la gestione del servizio durante l’evento, mentre lo chef è responsabile della preparazione delle ricette in sede.

Per quanto riguarda il servizio durante l’evento l’organizzatore dovrà scegliere il personale di servizio per ogni turno di servizio associato all’evento, indicando il ruolo che avrà in quella particolare situazione (es. Mario→servire le bevande, Luisa→girare in sala offrendo finger food). Se nel menù servito in quel turno ci sono ricette che prevedono passaggi non banali da svolgere all’ultimo, l’organizzatore può anche decidere di assegnare un cuoco a quel particolare servizio (non necessariamente in tutti i servizi di quell’evento).

Lo chef dal canto suo dovrà individuare uno o più menù adeguati all’evento; può trattarsi di menù già esistenti (ad esempio usati in eventi precedenti), o menù che lo chef compone per l’occasione. L’approvazione dei menù da parte dell’organizzatore dà il via ai lavori, a quel punto l’evento è “in corso” e non può più essere eliminato, ma solo eventualmente annullato. Sarà ancora possibile tuttavia modificarne alcune caratteristiche.

Prima di approvare i menù l’organizzatore può proporre delle modifiche ai menù, suggerendo piatti da aggiungere o togliere, ovviamente questo non modifica i menù originali scelti dagli chef; queste proposte restano visibili come aggiunte o eliminazioni limitate all’evento in questione. Lo chef potrà decidere se “tenere” le proposte dell’organizzatore o rimuoverle.

Al termine di un evento l’organizzatore lo “chiude” aggiungendo eventuali note e allegando documentazione rilevante.

## 3. Assegnamento dei compiti di cucina

Per quanto riguarda la preparazione del cibo in sede, è lo chef ad assegnare i compiti ai cuochi nei diversi turni di preparazione. I compiti includono la realizzazione dei preparati intermedi e delle ricette finali. Più cuochi possono lavorare alla stessa ricetta, ad esempio preparando ciascuno una parte delle porzioni richieste. Non è invece previsto che più cuochi si dividano la procedura da realizzare “verticalmente” (ossia facendo ciascuno solo alcune preparazioni) perché in tal caso ci si aspetta invece che la procedura venga suddivisa a livello di ricettario in preparazioni separate.

Ad esempio: se ci sono da fare 10 teglie di lasagne, è possibile affidarne 5 a un cuoco e 5 ad un altro, ma non è possibile affidare la preparazione della sfoglia a un cuoco e la preparazione del ragù ad un altro, a meno che sfoglia all’uovo e ragù non siano due preparazioni distinte nel ricettario. Quindi se si vogliono dividere i compiti in questo modo, nel ricettario ci dovranno essere tre preparazioni, “sfoglia all’uovo”, “ragù” e “lasagne”, e la ricetta delle lasagne dovrà prevedere come ingredienti la sfoglia all’uovo e il ragù. A quel punto se nel menù ci sono le lasagne, lo chef si troverà a dover assegnare tutte e tre le preparazioni, e potrà assegnarle a persone diverse. Se invece nel ricettario c’è solo la ricetta delle lasagne, lo chef potrà solo assegnarla tutta intera.

Quando assegna un’attività, lo chef deve anche dare (sfruttando le informazioni che accompagnano la ricetta, si veda la sezione relativa) una stima del tempo che l’attività richiede. Poiché un cuoco può svolgere più attività nello stesso turno, è possibile assegnargli un’attività solo se il tempo a sua disposizione glielo permette.

Lo chef e l’organizzatore possono inoltre monitorare lo svolgimento delle attività perché i cuochi, man mano che portano a termine un compito, contrassegnano la ricetta o procedura come “completata”. In questo modo chef e organizzatore possono verificare che tutto stia procedendo come deve ed operare eventuali aggiustamenti in corsa.

## 4. Ricette e Preparazioni

Il ricettario contiene ricette e preparazioni; si tratta di concetti molto simili, la differenza è che una ricetta descrive come preparare un piatto da servire a tavola, mentre una preparazione descrive come realizzare un preparato da utilizzare in un’altra.

Chef e cuochi possono inserire ricette o preparazioni nel ricettario; solo il proprietario di una ricetta o preparazione (chi la ha inserita) può però eliminarla o modificarla, e può farlo solo fintanto che la ricetta non è in uso in alcun menù. Se un utente vuole modificare una propria ricetta attualmente in uso, o una ricetta di un altro proprietario, può crearne una copia da modificare liberamente.

Le ricette o preparazioni inserite sono inizialmente in stato di bozza, visibili solo dal proprio creatore; perché siano visibili a tutti (e quindi usabili o copiabili) devono essere pubblicate da chi le ha create. Una volta pubblicate non sarà più possibile modificarle, a meno di non “ritirarle dalla pubblicazione”, cosa possibile soltanto però se non sono utilizzate (in un menu o, se preparazioni, per un ingrediente utilizzato in un’altra ricetta).

Una ricetta o preparazione è innanzitutto caratterizzata da un nome, da un proprietario (chi l’ha inserita), opzionalmente da un autore (chi l’ha ideata inizialmente), e può essere accompagnata da una descrizione breve di ciò che realizza o da altre note che si ritiene possano essere di interesse.

Gli utenti desiderano poter associare alle ricette tag scelti da loro allo scopo di organizzarle e reperirle con maggior facilità (esempi di tag: crudo, vegetariano, finger food, dessert, pasta).

Poiché per organizzare il lavoro è importante sapere quanto tempo ci vuole a cucinare qualcosa, chi scrive la ricetta o preparazione dovrà anche dare una stima sulle tempistiche.

Per ogni ricetta o preparazione andranno poi specificati gli ingredienti. Gli ingredienti potranno essere ingredienti di base, scelti da un elenco che si immagina predefinito nel software e che dovrà essere il più possibile esaustivo, oppure preparati ottenuti tramite altre preparazioni.

Degli ingredienti si dovrà poter specificare la dose. Inoltre, chi scrive la ricetta dovrà indicare con quelle dosi quante porzioni si realizzano o quale quantità di preparato risulterà.

Naturalmente una ricetta o preparazione non sarebbe tale senza le istruzioni! In Cat & Ring le istruzioni di una ricetta o preparazione sono sempre divise in due sezioni, la parte che può essere realizzata in anticipo e quella che deve essere realizzata all’ultimo sul posto dell’evento. Naturalmente è possibile che una delle due sezioni sia vuota.

Ogni parte contiene un elenco ordinato di istruzioni, in sequenza. L’utente dovrà indicare, quando aggiunge un’istruzione, dove si situa rispetto alle istruzioni già presenti, affinché sia chiaro l’ordine.

## 5. I menù

Lo chef costruisce i suoi menù a partire dalle ricette nel ricettario. Un menù si compone di diverse voci, opzionalmente divise in diverse sezioni (potrebbe anche esserci una sezione sola corrispondente all’intero menù).

Ogni voce fa riferimento ad una ricetta nel ricettario, ma il testo della voce può anche essere diverso dal nome della ricetta (ad esempio, la ricetta potrebbe chiamarsi “Vitello tonnato” mentre la voce di menu essere “girello di fassone con salsa tonnata”).

Un menù è anche caratterizzato da informazioni aggiuntive, quali:

- se è consigliata la presenza di un cuoco durante il servizio per finalizzare le preparazioni
- se prevede solo piatti freddi o anche piatti caldi
- se richiede la disponibilità di una cucina nella sede dell’evento
- se è adeguato a un buffet
- se può essere fruito senza posate (finger food)

Lo chef può modificare i suoi menù liberamente fintanto che non sono utilizzati in nessun evento. Nel momento in cui un menù viene utilizzato per un evento non può più essere modificato, sarà però possibile crearne uno nuovo partendo da una copia di quello esistente. Lo stesso avviene se lo chef desidera creare un menù a partire da uno esistente fatto da un altro chef.

## Appendice: esempi

### Esempi di eventi singoli

- pranzo di laurea 1 menù
- aperitivo aziendale 1 menù

### Esempi di eventi complessi

1) singola giornata composta da più eventi

- matrimonio: aperitivo + cena 2 menù
- fiera aziendale: pranzo + 2 coffee break 2 o più menù

2) evento che si sviluppa in più giornate con le stesse caratteristiche

- 3 giorni di conferenza nella quale vengono offerti pranzo + 2 coffee break al giorno
  - 2 o più menù al giorno, i menù possono variare con i giorni

3) evento che si sviluppa in più giornate con caratteristiche diverse

- 3 giorni di fiera aziendale
  - giorno 1 pranzo + coffee break (pomeriggio)
  - giorno 2 pranzo + 2 coffee break (mattina e pomeriggio) + aperitivo
  - giorno 3 pranzo + coffee break (mattina)

a questo evento sono associati più menù, alcuni di essi possono essere usati più volte (es il coffee break)

### Esempi di eventi ricorrenti

1) Evento singolo ricorrente:

- cena aziendale annuale
- aperitivo promozionale organizzato ogni 3 mesi

2) Eventi complessi ricorrenti

- 3 giorni di conferenza con cadenza annuale nella quale vengono offerti pranzo + 2 coffee break al giorno
  - 2 o più menù al giorno, i menù possono variare con i giorni e di anno in anno.
- 3 giorni di showroom aperto ai compratori ogni 6 mesi
  - giorno 1 pranzo + coffee break (pomeriggio)
  - giorno 2 pranzo + 2 coffee break (mattina e pomeriggio) + aperitivo
  - giorno 3 pranzo + coffee break (mattina)

a questo evento sono associati più menù, alcuni di essi possono essere usati più volte (es il coffee break). In generale i menù possono variare con i giorni e di anno in anno.

### Esempi di gestione dei turni

**Turni preparatori (luogo di default: cucina)**

L’organizzatore può voler inserire in agenda:

- *un turno preparatorio in cucina ogni lunedì dalle 9 alle 13*
- *un turno preparatorio in cucina ogni lunedì dalle 9 alle 13 da ottobre a dicembre nella cucina di Villa Antinori*

Esempi di modifiche apportate successivamente:

- Il turno del lunedì 9-13 diventa lunedì 9-12 (per tutti i lunedì)
- Il turno di lunedì 9 marzo 9-13 diventa lunedì 9 marzo 9-12
- per i mesi di marzo aprile e maggio 2021 il turno del giovedì 9-13 diventa 9-12

Esempi di raggruppamento:

- raggruppamento singolo: il turno di sabato 1 maggio mattina con quello di domenica 2 maggio mattina
- raggruppamento “ricorrente“:
  - raggruppa il turno di sabato mattina con quello di domenica mattina, per ogni settimana da qui al 31 dicembre
  - raggruppa il turno di venerdì pomeriggio e sabato mattina per 10 settimane a partire dal 26 marzo

**Turni di servizio**

Consideriamo il seguente evento: 3 giorni di conferenza nella quale vengono offerti pranzo + 2 coffee break al giorno. Le fasce orarie indicate per i servizi sono le seguenti: 12.30-14 per il pranzo, 10-10.30 per il coffee break del mattino, 16-16.30 per il coffee break del pomeriggio. La conferenza si ripete ogni 6 mesi.

L’organizzatore, prevedendo dei tempi aggiuntivi di preparazione e rigoverno, inserirà nell’agenda questi turni di servizio:

- 1 turno 11.30-15 nelle 3 date e nel luogo della conferenza (ripetuti ogni 6 mesi)
- 1 turno 9.30-11 nelle 3 date e nel luogo della conferenza (ripetuti ogni 6 mesi)
- 1 turno 15.30-17 nelle 3 date e nel luogo della conferenza (ripetuti ogni 6 mesi)

L’organizzatore può poi decidere di modificare alcuni aspetti dei turni inseriti e effettuare questa modifica su:

- ogni turno di servizio relativo a questo evento ricorrente
- solo alcuni turni fra quelli che ne fanno parte:
  - solo nei pranzi
  - solo nel primo evento
  - solo in eventi appartenenti a un certo intervallo di tempo
