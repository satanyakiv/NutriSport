package com.nutrisport.data

import com.nutrisport.shared.domain.PlatformFile
import dev.gitlive.firebase.storage.File

internal expect fun PlatformFile.toStorageFile(): File
