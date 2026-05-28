package com.example.a3dmodelviewer

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class ModelViewModel : ViewModel() {

    companion object {
        private const val MAX_MODELS = 5
    }

    val models = mutableStateListOf<ModelItem>()

    fun addModel(assetFile: String) {
        if (models.size >= MAX_MODELS) return
        models.add(ModelItem(assetFile = assetFile))
    }

    fun removeModel(id: String) {
        models.removeAll { it.id == id }
    }
}
