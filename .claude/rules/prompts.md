# Prompts & String Resources Rules

## UI Strings

- CMP string resources in `shared/ui/src/commonMain/composeResources/values/strings.xml`
- Access via `stringResource(Res.string.xxx)` in Composables
- Drawables and fonts in `shared/ui/src/commonMain/composeResources/`
- Android-only strings in `androidApp/src/main/res/values/strings.xml`
- No hardcoded strings in Composable functions

## Error Messages

- Errors are typed via `AppError` (Network, NotFound, Unauthorized, Unknown)
- Wrapped in `DomainResult<T>` (`Either<AppError, T>`) — see [error-handling.md](error-handling.md)
- Never expose raw Firebase/system error messages to UI

## Constants

- App-wide constants in `shared/utils/.../Constants.kt`
- Firebase collection names in repository companion objects
- API keys / client IDs in BuildConfig or environment-specific config
