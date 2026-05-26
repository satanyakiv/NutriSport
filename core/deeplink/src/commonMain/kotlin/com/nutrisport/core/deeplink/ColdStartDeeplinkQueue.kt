package com.nutrisport.core.deeplink

import org.koin.mp.KoinPlatformTools
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Platform-agnostic cold-start deeplink buffer. Both platforms funnel cold-start
 * links here so a single [DeeplinkBridge.dispatch] path routes every deeplink
 * (Android `MainActivity.onCreate`, iOS [DeeplinkSwiftBridge] `.onOpenURL`).
 *
 * Cold-start ordering problem this object solves: the launch link can arrive
 * BEFORE `SetupNavGraph` attaches its `Router.commands` collector (and, on iOS,
 * before Koin is even initialized). Emitting straight through `Router.replaceWith`
 * then would land in a `MutableSharedFlow` (replay = 0) with no subscriber and be
 * silently dropped.
 *
 * Strategy — a [navReady] latch:
 *  - [navReady] starts `false` and is flipped once per process by
 *    [markReadyAndDrain], invoked from the shared [ColdStartDeeplinkDrainEffect]
 *    once `Router.awaitReady()` resolves — i.e. the instant `SetupNavGraph`
 *    attaches its collector. The same effect fires on both platforms, so neither
 *    needs a platform-specific "nav ready" call.
 *  - While [navReady] is `false` (or the [DeeplinkBridge] is not resolvable yet —
 *    the iOS pre-Koin window), every event is enqueued instead of dispatched.
 *  - [markReadyAndDrain] flips the latch and replays each queued event through
 *    [DeeplinkBridge.handle] / [DeeplinkBridge.handlePush] into the now-subscribed
 *    collector, so the auth-gating in [DeeplinkBridge.dispatch] applies.
 *  - Once the latch is up, later events take the warm path and dispatch immediately.
 *
 * This is an `object` (not a Koin `single`) on purpose: the iOS Swift adapter
 * reaches it before Koin is initialized, so it must survive without DI and resolve
 * [DeeplinkBridge] lazily via [bridgeProvider]. The queue is process-local and is
 * NOT a substitute for [com.nutrisport.shared.domain.deeplink.PendingDeeplinkStorage]
 * (which carries an auth-gated target across the auth screen for post-login replay).
 */
@OptIn(ExperimentalAtomicApi::class)
object ColdStartDeeplinkQueue {

  private sealed interface QueuedEvent {
    data class Uri(val uri: String, val source: DeeplinkSource) : QueuedEvent
    data class Push(val data: Map<String, String>) : QueuedEvent
  }

  private val queue = AtomicReference<List<QueuedEvent>>(emptyList())

  /**
   * `false` until the shared drain effect has seen `Router.awaitReady()` resolve.
   * Flipped once per process by [markReadyAndDrain]; never reset.
   */
  private val navReady = AtomicReference(false)

  /**
   * Resolves the [DeeplinkBridge] when needed. Default looks it up from the global
   * Koin context (the bridge is a Koin `single`); returns `null` while Koin is not
   * yet initialized (the iOS cold-start window). Overridable in tests to inject a
   * real bridge without standing up a Koin container.
   */
  private val koinBridgeProvider: () -> DeeplinkBridge? = {
    KoinPlatformTools.defaultContext().getOrNull()?.get(DeeplinkBridge::class, null, null)
  }

  internal var bridgeProvider: () -> DeeplinkBridge? = koinBridgeProvider

  /** Test-only: reset the process-global latch, queue, and bridge provider so each
   *  test owns its state (this is an `object`, hence shared across tests). Never
   *  called in production. */
  internal fun resetForTest() {
    queue.store(emptyList())
    navReady.store(false)
    bridgeProvider = koinBridgeProvider
  }

  fun handle(uri: String, source: DeeplinkSource) {
    val bridge = bridgeProvider()
    if (navReady.load() && bridge != null) {
      bridge.handle(uri, source)
    } else {
      enqueueAndDrainIfReady(QueuedEvent.Uri(uri = uri, source = source))
    }
  }

  fun handlePush(data: Map<String, String>) {
    val bridge = bridgeProvider()
    if (navReady.load() && bridge != null) {
      bridge.handlePush(data)
    } else {
      enqueueAndDrainIfReady(QueuedEvent.Push(data = data))
    }
  }

  /**
   * Flips the [navReady] latch (so subsequent events take the warm path) and drains
   * anything queued during the cold-start gap. Idempotent: the latch only ever goes
   * `true`, and a second call drains an already-empty queue.
   */
  fun markReadyAndDrain() {
    navReady.store(true)
    drainPendingQueue()
  }

  private fun drainPendingQueue() {
    val bridge = bridgeProvider() ?: return
    val pending = queue.exchange(emptyList())
    for (event in pending) {
      when (event) {
        is QueuedEvent.Uri -> bridge.handle(event.uri, event.source)
        is QueuedEvent.Push -> bridge.handlePush(event.data)
      }
    }
  }

  /**
   * Enqueue, then re-check [navReady]. Closes the race where [markReadyAndDrain]
   * flips the latch and drains between a caller's `navReady` check and this enqueue:
   * the re-check drains the just-added event instead of stranding it. [queue]'s
   * atomic exchange means a concurrent drain still handles each event exactly once.
   */
  private fun enqueueAndDrainIfReady(event: QueuedEvent) {
    enqueue(event)
    if (navReady.load()) {
      drainPendingQueue()
    }
  }

  private fun enqueue(event: QueuedEvent) {
    while (true) {
      val current = queue.load()
      val next = current + event
      if (queue.compareAndSet(current, next)) return
    }
  }
}
