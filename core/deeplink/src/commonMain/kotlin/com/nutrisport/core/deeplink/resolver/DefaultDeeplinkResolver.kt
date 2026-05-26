package com.nutrisport.core.deeplink.resolver

import com.nutrisport.core.deeplink.DeeplinkSource
import com.nutrisport.core.deeplink.ResolvedDeeplink
import com.nutrisport.core.deeplink.registry.DeeplinkRoute

/**
 * Path-template matcher. Walks [routes] in declared order and returns the first
 * row that both matches the URI's path and produces a non-null Screen.
 *
 * Host handling differs by scheme:
 *  - custom scheme (`nutrisport://products/123`): the host segment IS the first
 *    route segment, so it is folded into the matchable path (`/products/123`);
 *  - `http`/`https` Universal Links: the host is the domain and is ignored —
 *    only the path drives resolution.
 */
class DefaultDeeplinkResolver(
  private val routes: List<DeeplinkRoute>,
) : DeeplinkResolver {

  override fun resolveUri(uri: String, source: DeeplinkSource): ResolvedDeeplink? {
    val parts = parseUrl(uri) ?: return null
    val normalizedPath = normalizePath(matchablePath(parts))
    for (route in routes) {
      val pathParams = matchTemplate(route.pathTemplate, normalizedPath) ?: continue
      val screen = route.resolve(pathParams, parts.query) ?: continue
      return ResolvedDeeplink(screen = screen, gate = route.gate)
    }
    return null
  }

  override fun resolvePush(data: Map<String, String>): ResolvedDeeplink? {
    val uri = data["deeplink"]?.takeIf { it.isNotBlank() } ?: return null
    return resolveUri(uri, DeeplinkSource.Push)
  }

  private fun matchablePath(parts: UrlParts): String =
    if (parts.scheme == "http" || parts.scheme == "https") {
      parts.path
    } else {
      "/" + parts.host + if (parts.path == "/") "" else parts.path
    }

  private fun normalizePath(raw: String): String {
    val trimmed = raw.trimEnd('/')
    return trimmed.ifEmpty { "/" }
  }

  private fun matchTemplate(template: String, path: String): Map<String, String>? {
    val tSegs = template.split('/').filter { it.isNotEmpty() }
    val pSegs = path.split('/').filter { it.isNotEmpty() }
    if (tSegs.size != pSegs.size) return null
    val out = mutableMapOf<String, String>()
    for (i in tSegs.indices) {
      val t = tSegs[i]
      val p = pSegs[i]
      if (t.startsWith('{') && t.endsWith('}')) {
        out[t.substring(1, t.length - 1)] = p
      } else if (!t.equals(p, ignoreCase = true)) {
        return null
      }
    }
    return out
  }
}
