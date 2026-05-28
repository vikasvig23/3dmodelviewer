package com.example.a3dmodelviewer

import java.util.UUID

data class ModelItem(
    val id: String = UUID.randomUUID().toString(),
    val assetFile: String
)
