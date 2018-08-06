package io.ktor.samples.mpp.client

import kotlinx.coroutines.experimental.*

internal actual val ApplicationDispatcher: CoroutineDispatcher = DefaultDispatcher
