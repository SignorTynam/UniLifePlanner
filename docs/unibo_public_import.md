# Import pubblico UniBo

## Scopo

La funzione "Importa da UniBo" consente di cercare un corso di laurea dell'Universita di Bologna e importare nel planner gli insegnamenti pubblici del piano didattico per l'anno di corso scelto dall'utente. Quando gli orari sono pubblici e riconoscibili, importa anche le lezioni ricorrenti con aula e edificio. Importa inoltre gli appelli d'esame pubblici visibili nella pagina pubblica del corso di laurea.

Questa funzione usa solo dati pubblici accessibili senza autenticazione. Non importa dati personali dello studente come libretto, prenotazioni esami o piano personale individuale.

## Differenza dal connettore account

L'import pubblico non richiede login UniBo, username, password, token o OAuth. Non accede all'area riservata e non legge pagine protette. Se una informazione non e disponibile pubblicamente, l'app mostra un avviso invece di inventare dati.

## Dati importati

- Insegnamenti del corso di laurea selezionato.
- Anno di corso dell'insegnamento, quando riconosciuto dalla pagina pubblica.
- CFU, se presenti nel piano didattico pubblico.
- Docenti, se presenti nella ricerca pubblica degli insegnamenti.
- Link ufficiale UniBo dell'insegnamento, se disponibile.
- Lezioni/orari/aula/edificio, solo quando la pagina pubblica espone righe con giorno e orario chiari.
- Appelli d'esame pubblici del corso di laurea selezionato.
- Data, ora, tipo prova e luogo dell'appello, quando presenti pubblicamente.
- Note e lista iscrizioni, solo se esposte nella pagina pubblica.

## Anno di corso

Durante l'import UniBo l'utente sceglie esplicitamente l'anno di corso. Per una laurea triennale vengono proposti 1°, 2° e 3° anno. Per una laurea magistrale vengono proposti 1° e 2° anno. Per una laurea magistrale a ciclo unico vengono proposti gli anni in base alla durata pubblicata da UniBo, fino a 5 o 6 anni quando indicato.

La preview e l'import includono solo insegnamenti, lezioni/laboratori e appelli collegati all'anno scelto. Il refresh manuale e automatico usa lo stesso anno salvato dopo l'ultimo import riuscito.

Se una vecchia importazione non contiene l'anno scelto, il refresh non importa tutto il piano didattico: l'utente deve rifare l'import da "Universita" scegliendo l'anno di corso.

## Limiti

- Vengono importati solo gli appelli pubblici visibili nella pagina pubblica del corso di laurea. Non vengono importati iscrizioni personali, prenotazioni, esiti o dati dell'area riservata AlmaEsami.
- Le informazioni dietro area riservata, captcha o login non vengono lette.
- I piani didattici possono variare per curriculum o anno di immatricolazione; questa versione importa i piani pubblici trovati per il codice corso e l'anno accademico scelto.
- Se UniBo cambia HTML o URL, il parser puo smettere di riconoscere alcuni dati.

## Duplicati

I record importati usano `sourceProvider = "UNIBO_PUBLIC"` per insegnamenti e lezioni, e `source = "UNIBO"` per gli appelli d'esame. Ogni record ha un `externalId` stabile. L'import successivo aggiorna i record gia importati con lo stesso provider/source e ID, senza duplicare insegnamenti, lezioni o appelli. I dati creati manualmente dall'utente non hanno questi campi e non vengono sovrascritti.

## Aggiornamento dati UniBo

Dopo un import riuscito, l'app salva la scelta pubblica UniBo dell'utente: corso di laurea, anno accademico, sito pubblico, curriculum se selezionato e anno di corso scelto.

Il refresh aggiorna corsi, lezioni/laboratori e appelli d'esame pubblici in un unico flusso, limitandosi all'anno di corso salvato. E disponibile dalle schermate "Corsi", "Lezioni" ed "Esami" e viene eseguito anche automaticamente all'apertura dell'app se esiste una importazione UniBo precedente.

Il refresh non richiede login, non usa AlmaEsami e legge solo pagine pubbliche UniBo. I dati manuali non vengono cancellati. Le lezioni e gli appelli UniBo non piu presenti nel preview aggiornato possono essere rimossi, con cancellazione dei relativi promemoria schedulati.

## Corsi completati

Un corso con stato `COMPLETED` resta visibile nella schermata "Corsi", ma le lezioni e gli appelli collegati non vengono mostrati nelle sezioni "Lezioni" ed "Esami".

I dati non vengono eliminati. Quando un corso viene completato, i promemoria futuri di lezioni e appelli collegati vengono cancellati e disattivati. Se il corso viene riaperto, lezioni e appelli tornano visibili, ma i vecchi promemoria non vengono riattivati automaticamente.

## Feedback post-esame

Se l'utente attiva il promemoria su un appello, l'app puo chiedere dopo l'esame com'e andata. La notifica post-esame apre una schermata in cui registrare esito, voto opzionale e note personali.

Se l'esame e superato, l'utente puo segnare il corso come completato. In quel caso lezioni e appelli collegati spariscono dalle sezioni principali e i relativi promemoria vengono disattivati.

## Manutenzione parser

I selettori CSS sono centralizzati in `UniboPublicParser`. Le URL pubbliche sono centralizzate in `UniboPublicConfig`. Se UniBo cambia struttura HTML:

1. Verificare la pagina pubblica interessata.
2. Aggiornare solo i selettori o i path nel package public import.
3. Aggiungere o aggiornare fixture HTML in `app/src/test/resources/unibo`.
4. Eseguire i test unitari del parser e `assembleDebug`.

## Test manuali

1. Aprire l'app e fare login all'app.
2. Aprire il drawer.
3. Aprire "Universita" e poi "Importa da UniBo".
4. Verificare il testo "Non richiede username, password o accesso all'area riservata."
5. Selezionare anno accademico `2025/2026`, campus `Tutti` o `Cesena`, tipologia `Laurea`.
6. Cercare `Ingegneria e Scienze Informatiche`.
7. Selezionare il risultato corretto.
8. Selezionare l'anno di corso, ad esempio "2° anno".
9. Verificare che l'anteprima mostri solo insegnamenti dell'anno scelto, con CFU, docenti, lezioni, appelli d'esame e avvisi.
10. Importare nel planner.
11. Aprire "Corsi" e verificare che gli insegnamenti siano presenti e mostrino la pill "1° anno", "2° anno" o equivalente quando disponibile.
12. Aprire un insegnamento e verificare note/link ufficiale.
13. Se lezioni disponibili, aprire "Lezioni" e verificare orari e aule solo dei corsi dell'anno scelto.
14. Aprire "Esami" e verificare che gli appelli pubblici importati siano presenti solo per i corsi dell'anno scelto.
15. Ripetere l'import e verificare che non crei duplicati.
16. Verificare che il pulsante "Importa UniBo" non sia piu presente nella schermata "Esami".
17. Verificare che l'import avvenga solo da "Universita".
18. Premere Refresh da "Corsi", "Lezioni" ed "Esami" e verificare che venga aggiornato solo l'anno di corso scelto.
19. Con una vecchia importazione senza anno scelto, verificare che il refresh chieda di rifare l'import da "Universita".
20. Completare un corso e verificare che lezioni e appelli collegati non compaiano nelle sezioni principali.
21. Attivare un promemoria appello futuro e verificare la schedulazione del feedback post-esame.
22. Registrare un esito superato e verificare l'opzione di completamento corso.
23. Disattivare internet e verificare un errore pulito.
24. Verificare tema chiaro/scuro.
25. Verificare `versionName = "1.1.7"`.

## TODO futuri

1. Supporto ad altri atenei italiani.
2. Aggiornamento automatico periodico dei dati pubblici.
3. Vista calendario settimanale importata.
4. Esportazione lezioni in Google Calendar.
5. Gestione cambi aula/orario con confronto tra import vecchio e nuovo.
6. Notifica se l'orario pubblico cambia.
7. Selezione curriculum/indirizzo del corso di laurea.
8. Gestione aggiornamenti o rimozioni degli appelli pubblici non piu presenti.
9. Supporto multilingua italiano/inglese.
10. Cache persistente con data ultimo aggiornamento.
