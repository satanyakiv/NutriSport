# Prompts & String Resources Rules

## UI Strings

- All user-facing strings in Compose Multiplatform resources
- Path: `shared/src/commonMain/composeResources/values/strings.xml`
- Access via `stringResource(Res.string.xxx)`
- No hardcoded strings in Composable functions

## Error Messages

- Defined as constants in `shared/.../util/ErrorMessages.kt`
- Used in `RequestState.Error(message)` wrapping
- Never expose raw Firebase/system error messages to UI

## Constants

- App-wide constants in `shared/.../util/Constants.kt`
- Firebase collection names in repository companion objects
- API keys / client IDs in BuildConfig or environment-specific config
