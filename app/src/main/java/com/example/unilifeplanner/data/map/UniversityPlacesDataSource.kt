package com.example.unilifeplanner.data.map

import com.example.unilifeplanner.domain.model.PlaceType
import com.example.unilifeplanner.domain.model.UniversityPlace

object UniversityPlacesDataSource {
    // Demo locali per la mappa universitaria: coordinate indicative su area Campus UniBo Cesena.
    val places: List<UniversityPlace> = listOf(
        UniversityPlace(
            id = 1,
            name = "Campus UniBo Cesena",
            description = "Punto di riferimento del campus per orientarsi tra sedi e servizi.",
            type = PlaceType.OTHER,
            latitude = 44.13910,
            longitude = 12.24315
        ),
        UniversityPlace(
            id = 2,
            name = "Biblioteca universitaria",
            description = "Spazio per studio individuale, prestito e consultazione.",
            type = PlaceType.LIBRARY,
            latitude = 44.13955,
            longitude = 12.24380
        ),
        UniversityPlace(
            id = 3,
            name = "Aula studio campus",
            description = "Aula silenziosa aperta per sessioni di studio di gruppo.",
            type = PlaceType.STUDY_ROOM,
            latitude = 44.13872,
            longitude = 12.24268
        ),
        UniversityPlace(
            id = 4,
            name = "Laboratorio informatico",
            description = "Postazioni PC per esercitazioni, progetti e attivita pratiche.",
            type = PlaceType.LAB,
            latitude = 44.13895,
            longitude = 12.24405
        ),
        UniversityPlace(
            id = 5,
            name = "Segreteria studenti",
            description = "Sportello per pratiche amministrative, iscrizioni e certificati.",
            type = PlaceType.SECRETARIAT,
            latitude = 44.13990,
            longitude = 12.24250
        ),
        UniversityPlace(
            id = 6,
            name = "Mensa e area ristoro",
            description = "Area per pausa pranzo, ristoro veloce e incontro tra studenti.",
            type = PlaceType.CANTEEN,
            latitude = 44.13835,
            longitude = 12.24340
        ),
        UniversityPlace(
            id = 7,
            name = "Fermata bus Campus",
            description = "Fermata principale per i collegamenti con stazione e centro.",
            type = PlaceType.BUS_STOP,
            latitude = 44.14020,
            longitude = 12.24420
        ),
        UniversityPlace(
            id = 8,
            name = "Sede didattica",
            description = "Ingresso alle aule per lezioni, ricevimenti e attivita didattiche.",
            type = PlaceType.OTHER,
            latitude = 44.13845,
            longitude = 12.24205
        )
    )
}
