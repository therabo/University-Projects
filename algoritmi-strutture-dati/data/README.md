# Dataset per gli esercizi

Gli archivi contengono i file originali necessari agli esercizi. `records.csv` è suddiviso in cinque ZIP autonomi, in ordine (`00`–`04`), per mantenere ogni file sotto il limite di GitHub. Il README principale rimane il testo originale della consegna.

Dalla radice del progetto, ricostruire i file in una directory ignorata da Git:

```sh
mkdir -p data/unpacked
for part in 00 01 02 03 04; do
  unzip -p "data/records-part-${part}.zip"
done > data/unpacked/records.csv
for name in dictionary correctme italian_dist_graph; do
  unzip -o "data/${name}.zip" -d data/unpacked
done
```

La somma SHA-256 attesa di `data/unpacked/records.csv` è `2f329b6ccf83e2d588282e0b48c6ef78171da58124ca87b1886d3af746a665de`.

```sh
make all
make test
./ex1/bin/main_ex1 data/unpacked/records.csv data/unpacked/sorted.csv 1 1
./ex2/bin/main_ex2 data/unpacked/dictionary.txt data/unpacked/correctme.txt
java -cp ex3-4/bin Prim.Prim data/unpacked/italian_dist_graph.csv > data/unpacked/forest.csv
```

L’ultimo comando scrive gli archi della foresta in `forest.csv` e il riepilogo sullo standard error.
