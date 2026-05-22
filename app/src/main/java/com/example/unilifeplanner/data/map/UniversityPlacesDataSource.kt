package com.example.unilifeplanner.data.map

import com.example.unilifeplanner.domain.model.PlaceType
import com.example.unilifeplanner.domain.model.UniversityPlace

object UniversityPlacesDataSource {
    val places: List<UniversityPlace> = listOf(
        UniversityPlace(
            id = 1,
            name = "Biblioteca centrale",
            description = "Spazio per studio individuale, prestito libri e consultazione.",
            type = PlaceType.LIBRARY,
            latitude = 45.47812,
            longitude = 9.22786
        ),
        UniversityPlace(
            id = 2,
            name = "Mensa universitaria",
            description = "Area ristoro studenti con servizio pranzo e cena.",
            type = PlaceType.CANTEEN,
            latitude = 45.47743,
            longitude = 9.22918
        ),
        UniversityPlace(
            id = 3,
            name = "Aula studio",
            description = "Aula silenziosa aperta per sessioni di studio di gruppo.",
            type = PlaceType.STUDY_ROOM,
            latitude = 45.47878,
            longitude = 9.22874
        ),
        UniversityPlace(
            id = 4,
            name = "Segreteria studenti",
            description = "Sportello per pratiche amministrative, iscrizioni e certificati.",
            type = PlaceType.SECRETARIAT,
            latitude = 45.47692,
            longitude = 9.22731
        ),
        UniversityPlace(
            id = 5,
            name = "Laboratorio informatico",
            description = "Laboratorio con postazioni PC per esercitazioni e progetti.",
            type = PlaceType.LAB,
            latitude = 45.47794,
            longitude = 9.22671
        ),
        UniversityPlace(
            id = 6,
            name = "Fermata bus Campus",
            description = "Fermata principale per i collegamenti con stazione e centro.",
            type = PlaceType.BUS_STOP,
            latitude = 45.47648,
            longitude = 9.22862
        )
    )
}
