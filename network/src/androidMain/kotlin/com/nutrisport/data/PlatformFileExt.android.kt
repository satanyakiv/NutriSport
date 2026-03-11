package com.nutrisport.data

import com.nutrisport.shared.domain.PlatformFile
import dev.gitlive.firebase.storage.File

internal actual fun PlatformFile.toStorageFile(): File = File(uri)
