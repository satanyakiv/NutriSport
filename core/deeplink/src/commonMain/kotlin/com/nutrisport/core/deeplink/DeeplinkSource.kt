package com.nutrisport.core.deeplink

/**
 * Where a deeplink came from. UniversalLink and CustomScheme are platform-link
 * surfaces; Push wraps an FCM / APNs `data` payload. The resolver and bridge use
 * this for logging and to disambiguate identical paths across surfaces.
 */
enum class DeeplinkSource { UniversalLink, CustomScheme, Push }
