package com.example.unilifeplanner.university.publicimport.unibo

import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicImportPreview

class UniboPublicImportCache {
    private val searchCache = mutableMapOf<String, List<PublicDegreeProgram>>()
    private val previewCache = mutableMapOf<String, PublicImportPreview>()

    fun getSearch(key: String): List<PublicDegreeProgram>? = searchCache[key]

    fun putSearch(
        key: String,
        value: List<PublicDegreeProgram>
    ) {
        searchCache[key] = value
    }

    fun getPreview(key: String): PublicImportPreview? = previewCache[key]

    fun putPreview(
        key: String,
        value: PublicImportPreview
    ) {
        previewCache[key] = value
    }
}
