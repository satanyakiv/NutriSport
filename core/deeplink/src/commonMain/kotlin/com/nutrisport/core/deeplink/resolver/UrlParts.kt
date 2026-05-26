package com.nutrisport.core.deeplink.resolver

/**
 * Minimal multiplatform URL parser. Captures the four parts the resolver cares
 * about (scheme, host, path, query) and nothing else. Deliberately simpler than
 * full RFC 3986 — deeplinks are flat ASCII paths and a handful of documented
 * query keys; we don't need a general-purpose URI library.
 */
internal data class UrlParts(
  val scheme: String,
  val host: String,
  val path: String,
  val query: Map<String, String>,
)

internal fun parseUrl(uri: String): UrlParts? {
  val schemeEnd = uri.indexOf("://")
  if (schemeEnd <= 0) return null
  val scheme = uri.substring(0, schemeEnd).lowercase()
  val rest = uri.substring(schemeEnd + 3)
  val fragmentStart = rest.indexOf('#').let { if (it == -1) rest.length else it }
  val withoutFragment = rest.substring(0, fragmentStart)
  val queryStart = withoutFragment.indexOf('?').let { if (it == -1) withoutFragment.length else it }
  val hostAndPath = withoutFragment.substring(0, queryStart)
  val querySrc = if (queryStart < withoutFragment.length) withoutFragment.substring(queryStart + 1) else ""

  val firstSlash = hostAndPath.indexOf('/')
  val host: String
  val pathRaw: String
  if (firstSlash == -1) {
    host = hostAndPath
    pathRaw = ""
  } else {
    host = hostAndPath.substring(0, firstSlash)
    pathRaw = hostAndPath.substring(firstSlash)
  }
  val path = if (pathRaw.isEmpty()) "/" else pathRaw

  val query: Map<String, String> = if (querySrc.isEmpty()) {
    emptyMap()
  } else {
    querySrc.split('&').asSequence()
      .filter { it.isNotEmpty() }
      .map { kv ->
        val eq = kv.indexOf('=')
        if (eq < 0) {
          decodePercent(kv) to ""
        } else {
          decodePercent(kv.substring(0, eq)) to decodePercent(kv.substring(eq + 1))
        }
      }
      .toMap()
  }

  return UrlParts(scheme = scheme, host = host.lowercase(), path = path, query = query)
}

private fun decodePercent(s: String): String {
  if ('%' !in s && '+' !in s) return s
  val out = StringBuilder(s.length)
  var i = 0
  while (i < s.length) {
    val c = s[i]
    when {
      c == '+' -> {
        out.append(' ')
        i++
      }
      c == '%' && i + 2 < s.length -> {
        val hex = s.substring(i + 1, i + 3)
        val byte = hex.toIntOrNull(16)
        if (byte != null) {
          out.append(byte.toChar())
          i += 3
        } else {
          out.append(c)
          i++
        }
      }
      else -> {
        out.append(c)
        i++
      }
    }
  }
  return out.toString()
}
