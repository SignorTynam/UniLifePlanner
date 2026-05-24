# Import pubblico UniBo

## Scopo

La funzione "Importa da UniBo" consente di cercare un corso di laurea dell'Universita di Bologna e importare nel planner gli insegnamenti pubblici del piano didattico. Quando gli orari sono pubblici e riconoscibili, importa anche le lezioni ricorrenti con aula e edificio.

Questa funzione usa solo dati pubblici accessibili senza autenticazione. Non importa dati personali dello studente come libretto, prenotazioni esami o piano personale individuale.

## Differenza dal connettore account

L'import pubblico non richiede login UniBo, username, password, token o OAuth. Non accede all'area riservata e non legge pagine protette. Se una informazione non e disponibile pubblicamente, l'app mostra un avviso invece di inventare dati.

## Dati importati

- Insegnamenti del corso di laurea selezionato.
- CFU, se presenti nel piano didattico pubblico.
- Docenti, se presenti nella ricerca pubblica degli insegnamenti.
- Link ufficiale UniBo dell'insegnamento, se disponibile.
- Lezioni/orari/aula/edificio, solo quando la pagina pubblica espone righe con giorno e orario chiari.

## Limiti

- Gli appelli d'esame personali non vengono importati.
- Le informazioni dietro area riservata, captcha o login non vengono lette.
- I piani didattici possono variare per curriculum o anno di immatricolazione; questa versione importa i piani pubblici trovati per il codice corso e l'anno accademico scelto.
- Se UniBo cambia HTML o URL, il parser puo smettere di riconoscere alcuni dati.

## Duplicati

I record importati usano `sourceProvider = "UNIBO_PUBLIC"` e un `externalId` stabile. L'import successivo aggiorna i record gia importati con lo stesso provider e ID, senza duplicare insegnamenti o lezioni. I dati creati manualmente dall'utente non hanno questi campi e non vengono sovrascritti.

## Manutenzione parser

I selettori CSS sono centralizzati in `UniboPublicParser`. Le URL pubbliche sono centralizzate in `UniboPublicConfig`. Se UniBo cambia struttura HTML:

1. Verificare la pagina pubblica interessata.
2. Aggiornare solo i selettori o i path nel package public import.
3. Aggiungere o aggiornare fixture HTML in `app/src/test/resources/unibo`.
4. Eseguire i test unitari del parser e `assembleDebug`.

## Test manuali

1. Aprire l'app e fare login all'app.
2. Aprire il drawer.
3. Aprire "Importa UniBo" oppure "Universita" e poi "Importa da UniBo".
4. Verificare il testo "Non richiede username, password o accesso all'area riservata."
5. Selezionare anno accademico `2025/2026`, campus `Tutti` o `Cesena`, tipologia `Laurea`.
6. Cercare `Ingegneria e Scienze Informatiche`.
7. Selezionare il risultato corretto.
8. Verificare anteprima insegnamenti, CFU, docenti e avvisi.
9. Importare nel planner.
10. Aprire "Corsi" e verificare che gli insegnamenti siano presenti.
11. Aprire un insegnamento e verificare note/link ufficiale.
12. Se lezioni disponibili, aprire "Lezioni" e verificare orari e aule.
13. Ripetere l'import e verificare che non crei duplicati.
14. Disattivare internet e verificare un errore pulito.
15. Verificare tema chiaro/scuro.
16. Verificare `versionName = "1.1.2"`.

## TODO futuri

1. Supporto ad altri atenei italiani.
2. Aggiornamento automatico periodico dei dati pubblici.
3. Vista calendario settimanale importata.
4. Esportazione lezioni in Google Calendar.
5. Gestione cambi aula/orario con confronto tra import vecchio e nuovo.
6. Notifica se l'orario pubblico cambia.
7. Selezione curriculum/indirizzo del corso di laurea.
8. Import appelli pubblici se disponibili senza login.
9. Supporto multilingua italiano/inglese.
10. Cache persistente con data ultimo aggiornamento.
